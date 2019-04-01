package com.hexmeet.hjt.utils;

import android.os.Environment;

import org.apache.log4j.Level;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class ConfigureLog4J {
    private static LogConfigurator logConfigurator = null;

    public static void configure() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "crash"
                    + File.separator);
            String fileName = dir.toString() + File.separator + "hjt_app.log";
            logConfigurator.setFileName(fileName);
            logConfigurator.setRootLevel(Level.DEBUG);
            logConfigurator.setLevel("org.apache", Level.DEBUG);
            logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
            logConfigurator.setLogCatPattern("%m%n");
            logConfigurator.setMaxFileSize(1024 * 1024 * 4);
            logConfigurator.setMaxBackupSize(2);
            logConfigurator.setImmediateFlush(true);
            logConfigurator.setUseLogCatAppender(true);
            logConfigurator.setUseFileAppender(true);
            logConfigurator.configure();
        }

    }

    public static LogConfigurator getLogConfigurator() {
        return logConfigurator;
    }

    public static void setLogConfigurator(LogConfigurator logConfigurator) {
        ConfigureLog4J.logConfigurator = logConfigurator;
    }

}
