package ParamAnalysis;

import com.aa.run.CmdLineParams;
import org.apache.commons.cli.*;
import org.junit.Test;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Demo {
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("b","bootstrap_server",true, "kafka httpServer");
        options.addOption("n","es_number_of",true, "es number of replica");
        CommandLineParser parser = new PosixParser();
        CommandLine cmdLine = parser.parse(options, args);
        CmdLineParams.setLine(cmdLine);
        String bootstrapSever = CmdLineParams.getBootstrapSever();
        System.out.println(bootstrapSever);
    }

    @Test
    public void test01(){
        Integer a = Integer.valueOf("72", 8);int limit = -Integer.MAX_VALUE;
        System.out.println(limit / 8);
        System.out.println(a);

    }
    
    @Test
    public void test02(){
        LocalTime a = LocalTime.of(3, 33);
        LocalTime b = LocalTime.of(4, 23);
        System.out.println(a.getHour());
        System.out.println(a.compareTo(b));
    }

    @Test
    public void test03(){
        Map<String, Son> map = new HashMap<>();
        Son son = new Son();
        map.put("1", son);
        son.id = "b";
        System.out.println(map.get("1").id);
    }
}
