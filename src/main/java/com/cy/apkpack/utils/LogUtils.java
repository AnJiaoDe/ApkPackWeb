package com.cy.apkpack.utils;


import org.apache.juli.logging.LogFactory;

/**
 * Created by lenovo on 2017/8/20.
 */

public class LogUtils {
//    public static void error(String tag, Object content) {
//        if (tag == null) tag = "LOG_E";
//
//        LogFactory.getLog(tag).error(tag + "----------------------------------->>>>" + content);
//    }
//
    public static void log(Object tag, Object content) {
        if (tag == null) tag = "LOG_E";

        LogFactory.getLog(String.valueOf(tag)).error(tag+ "----------------------------------->>>>" + content);
    }
//
//    public static void log(String tag) {
//        if (tag == null) tag = "LOG_E";
//
//        System.out.println(tag + "----------------------------------->>>>");
//
//    }

    public static void log(Object obj) {
        LogFactory.getLog("error").error("---------------------------------->>>>"+obj);

    }
}
