package com.zshield.stream.violation.proc;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zshield.httpServer.common.ViolationShared;
import com.zshield.stream.violation.detection.Detection;
import com.zshield.stream.violation.metric.Metric;
import com.zshield.stream.violation.metric.MetricBin;
import com.zshield.stream.violation.metric.MetricFactory;
import com.zshield.util.TimeUtil;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.To;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ViolationProcessing extends AbstractProcessor<String, String> {
    private ProcessorContext context;
    private KeyValueStore<String,String> kv;
    private final JsonParser jp = new JsonParser();
    private final Map<String, String> newSensorTime = new HashMap<>();
    private MetricFactory metricFactory;
    private final static Logger logger = LoggerFactory.getLogger(ViolationProcessing.class);



    @Override
    public void process(String key, String value) {
        try {
            JsonObject obj = verification(jp.parse(value).getAsJsonObject());
            if (obj != null) {
                String sensorId = obj.get("SENSOR_ID").getAsString();
                String dayTime = TimeUtil.parseToEastEightZoneTime(obj.get("TIME").getAsString()).plusDays(-1).format(TimeUtil.DATE_FORMATTER);
                String oldSensorTime = kv.get(sensorId);
                if (oldSensorTime == null) {
                    kv.put(sensorId, dayTime);
                } else {
                    if (dayTime.compareTo(oldSensorTime) > 0) {
                        newSensorTime.put(sensorId, dayTime);
                    }
                }
                //根据日志生成相应的Metric集合
                Set<Metric> metrics = metricFactory.build(value);

                //与违规定义的Metric集合比较，获取满足违规定义的Metric集合。
                ViolationShared.getInstance().retainAll(metrics);
                if (metrics == null || metrics.size() == 0) {
                    return;
                }
                logger.warn("-- generate metrics : {}", metrics);
                //根据Metric集合生成MetricBin集合
                Set<MetricBin> metricBins = MetricBin.create(obj, metrics);
                for (MetricBin metricBin : metricBins) {
                    //更新MetricBin
                    metricBin.update(kv, obj);
                    //下发到违规检测节点（直接到所有节点就可以了）
                    logger.info(metricBin.toString());
                    context.forward(null, metricBin, To.child("vioDection"));
                }
            }
        } catch (Exception e) {
            logger.error("[violation stream preproc exception]", e);
        }
    }
    private JsonObject verification(JsonObject jsonObject) {
        if (jsonObject.get("SENSOR_ID") == null || jsonObject.get("SENSOR_ID").getAsString().equals("")) {
            return null;
        }
        String logTime = jsonObject.get("TIME").getAsString();
        //检查日志时间是否比服务器时间大一天
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(logTime).withOffsetSameInstant(ZoneOffset.of("+08:00"));
        LocalDateTime nowAddOneDay = LocalDateTime.now().plusDays(1);
        if (nowAddOneDay.isBefore(ChronoLocalDateTime.from(offsetDateTime))) {
            logger.warn("[log time exceeds server time by more than one day , log {} ]", jsonObject);
            return null;
        }
        return jsonObject;
    }

    @Override
    public void init(ProcessorContext context){
        metricFactory = new MetricFactory();
        this.context = context;
        kv = (KeyValueStore<String, String>) context.getStateStore("vioQstore");
        //每个三十分钟清理一次KeyValueStore
        this.context.schedule(Duration.ofMinutes(30), PunctuationType.WALL_CLOCK_TIME,timestamp ->{
            clearKV();
        });
        this.context.schedule(Duration.ofSeconds(3), PunctuationType.WALL_CLOCK_TIME, timestamp -> {

        });
    }

    private void clearKV() {
        Set<String> sensorIds = newSensorTime.keySet();
        KeyValueIterator<String, String> it = kv.all();
        int i = 0;
        while (it.hasNext()) {
            KeyValue<String, String> kvEntry = it.next();
            String key = kvEntry.key;
            String[] keyElement = key.split("-");
            if (keyElement[0].equals("metric")) {
                String sensorId = keyElement[1];
                if (sensorIds.contains(sensorId)) {
                    String metricId = key.substring(key.indexOf('-') + 1, key.lastIndexOf('-'));
                    String hourTime = keyElement[keyElement.length - 1];//kv里面存储的时间
                    if (hourTime.compareTo(getEarlistDependedMetricHourTime(metricId, newSensorTime.get(sensorId))) < 0) {
                        i++;
                        logger.info("Delete data %s in key-value store", kvEntry);
                        kv.delete(key);
                    }
                }
            }
        }
        logger.info("[Clean memory complete, the number of metric: {}]", i);
        for (Map.Entry<String, String> entry : newSensorTime.entrySet()) {
            kv.put(entry.getKey(), entry.getValue());
        }
        newSensorTime.clear();
    }

    public String getEarlistDependedMetricHourTime(String metricId, String newHourTime) {
        String earlistTime = newHourTime;
        //violationshared获取到Detection的Set集合。
        Set<Detection> metricDetections = getMetrictions(metricId);
        if (metricDetections != null) {
            for (Detection detection : metricDetections) {
                String curDetectEarliestHourTime = detection.getEarliestMetricHourTime(newHourTime);

            }
        }
        return earlistTime;
    }

    public Set<Detection> getMetrictions(String metricId) {
        return ViolationShared.getInstance().getDetections(metricId);
    }
}
