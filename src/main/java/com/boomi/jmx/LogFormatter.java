package com.boomi.jmx;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(record.getMillis());
        String methodName = record.getSourceMethodName();
        String logMessage = record.getMessage();

        // Custom log format with timestamp, method name, and message
        return timestamp + " " + methodName + " " + record.getLevel() + " " + logMessage + "\n";
    }

}
