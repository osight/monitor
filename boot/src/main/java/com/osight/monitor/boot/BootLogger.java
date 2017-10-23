package com.osight.monitor.boot;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class BootLogger {
    private final String messagePattern;
    private final PrintStream out;
    private final PrintStream err;

    private BootLogger(String loggerName, PrintStream out, PrintStream err) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName must not be null");
        }
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss} [{1}](" + loggerName + ") {2}{3}";
        this.out = out;
        this.err = err;
    }

    private BootLogger(String loggerName) {
        this(loggerName, System.out, System.err);
    }

    static BootLogger getLogger(String loggerName) {
        return new BootLogger(loggerName);
    }

    public void info(String msg) {
        String formatMessage = format("INFO ", msg, "");
        this.out.println(formatMessage);
    }


    private String format(String logLevel, String msg, String exceptionMessage) {
        exceptionMessage = defaultString(exceptionMessage, "");

        MessageFormat messageFormat = new MessageFormat(messagePattern);
        final long date = System.currentTimeMillis();
        Object[] parameter = {date, logLevel, msg, exceptionMessage};
        return messageFormat.format(parameter);
    }

    public void warn(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String formatMessage = format("WARN ", msg, exceptionMessage);
        this.err.println(formatMessage);
    }
    public void warn(String msg) {
        warn(msg, null);
    }

    private String toString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }



    private String defaultString(String exceptionMessage, String defaultValue) {
        if (exceptionMessage == null) {
            return defaultValue;
        }
        return exceptionMessage;
    }
}
