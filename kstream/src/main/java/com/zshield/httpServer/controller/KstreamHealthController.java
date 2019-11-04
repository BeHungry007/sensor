package com.zshield.httpServer.controller;

import com.zshield.httpServer.common.BaseController;

import javax.ws.rs.Path;

//@Path("/healthmonitor")
public class KstreamHealthController extends BaseController {
    public static long streamChangingTimeMillis = -1l;
}
