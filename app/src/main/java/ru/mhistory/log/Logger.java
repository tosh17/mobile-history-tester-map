package ru.mhistory.log;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class Logger {
    private static volatile boolean loggingEnable = false;
    private static volatile boolean logToFile = false;
    private static volatile FileLogger fileLogger;

    public static void d(String msg, Object... args) {
        if (isLoggingEnable()) {
            try {
                d(String.format(msg, args));
            } catch (Exception e) {
                d(msg);
            }
        }
    }

    public static void e(String msg, Object... args) {
        if (isLoggingEnable()) {
            try {
                e(String.format(msg, args));
            } catch (Exception e) {
                e(msg);
            }
        }
    }

    public static void e(Throwable e, String msg, Object... args) {
        if (isLoggingEnable()) {
            try {
                e(e, String.format(msg, args));
            } catch (Exception ee) {
                e(e, msg);
            }
        }
    }

    public static void w(String msg, Object... args) {
        if (isLoggingEnable()) {
            try {
                w(String.format(msg, args));
            } catch (Exception e) {
                w(msg);
            }
        }
    }

    public static void w(Throwable e, String msg, Object... args) {
        if (isLoggingEnable()) {
            try {
                w(e, String.format(msg, args));
            } catch (Exception ee) {
                w(e, msg);
            }
        }
    }

    public static final String METHOD_E = "e";
    public static final String METHOD_D = "d";
    public static final String METHOD_W = "w";
    public static final String METHOD_V = "v";

    public static void setLoggingEnabled(boolean isLoggingEnabled) {
        Logger.loggingEnable = isLoggingEnabled;
    }

    public static boolean isLoggingEnable() {
        return loggingEnable;
    }

    public static void setLogToFile(boolean logToFile, Context context) {
        synchronized (Logger.class) {
            Logger.logToFile = logToFile;
            if (logToFile && fileLogger == null) {
                fileLogger = FileLogger.from();
            } else if (!logToFile && fileLogger != null) {
                fileLogger = null;
            }
        }
    }

    public static boolean isLogToFile() {
        return logToFile;
    }

    public static void e(Throwable e) {
        if (isLoggingEnable()) {
            e(e, "error");
        }
    }

    public static void e(int message) {
        if (isLoggingEnable()) {
            e(null, "int value = " + message);
        }
    }

    public static void e(float message) {
        if (isLoggingEnable()) {
            e(null, "float value = " + message);
        }
    }

    public static void e(boolean message) {
        if (isLoggingEnable()) {
            e(null, "boolean value = " + message);
        }
    }

    public static void e(String message) {
        if (isLoggingEnable()) {
            e(null, message);
        }
    }

    public static void d(String message) {
        if (isLoggingEnable()) {
            final String tag = extractClassName(METHOD_D);
            final String msg = buildMessageString(METHOD_D, message);
            Log.d(tag, msg);
            if (isLogToFile()) {
                fileLogger.d(tag, msg);
            }
        }
    }

    public static void w(String message) {
        if (isLoggingEnable()) {
            final String tag = extractClassName(METHOD_W);
            final String msg = buildMessageString(METHOD_W, message);
            Log.w(tag, msg);
            if (isLogToFile()) {
                fileLogger.w(tag, msg);
            }
        }
    }

    public static void w(Throwable e, String message) {
        if (isLoggingEnable()) {
            final String tag = extractClassName(METHOD_W);
            final String msg = buildMessageString(METHOD_W, message);
            Log.w(tag, msg, e);
            if (isLogToFile()) {
                fileLogger.w(tag, msg, e);
            }
        }
    }

    public static void e(Throwable e, String message) {
        if (isLoggingEnable()) {
            final String tag = extractClassName(METHOD_E);
            final String msg = buildMessageString(METHOD_E, message);
            Log.e(tag, msg, e);
            if (isLogToFile()) {
                fileLogger.e(tag, msg, e);
            }
        }
    }

    public static void v(String message) {
        if (isLoggingEnable()) {
            final String tag = extractClassName(METHOD_V);
            final String msg = buildMessageString(METHOD_V, message);

            Log.v(tag, msg);
            if (isLogToFile()) {
                fileLogger.v(tag, msg);
            }
        }
    }

    public static void v(String msg, Object... args) {
        if (isLoggingEnable()) {
            try {
                v(String.format(msg, args));
            } catch (Exception e) {
                v(msg);
            }
        }
    }

    private static String extractClassName(String methodName) {
        return trace(methodName).getClassName();
    }

    private static String buildMessageString(String methodName, String message) {
        StackTraceElement element = trace(methodName);
        return element.getMethodName() + " (" + element.getLineNumber() + "): " + message;
    }

    private static StackTraceElement trace(String method) {
        StackTraceElement[] e = Thread.currentThread().getStackTrace();
        int i;
        boolean methodFound = false;
        for (i = 0; i < e.length; i++) {
            boolean isNamesEquals = e[i].getMethodName().equals(method);
            if (methodFound && !isNamesEquals)
                break;
            methodFound = isNamesEquals;
        }
        return e[i];
    }

    public static void start() {
        fileLogger.start();
    }

    public static void stop() {
        fileLogger.stop();
    }
}
