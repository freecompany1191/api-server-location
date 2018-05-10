package com.o2osys.location.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;

public class logUtil {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    public static long startlog(Logger log) {
        long startTime = System.currentTimeMillis();
        log.info("====================================================");
        log.info("The time is now " + dateFormat.format(new Date()));
        return startTime;
    }

    public static void endlog(Logger log, long startTime) {
        long endTime = System.currentTimeMillis();
        log.info("start time:" + startTime + ".end time:" + endTime + ".diff:" + (endTime - startTime));
        log.info("====================================================");
    }
}
