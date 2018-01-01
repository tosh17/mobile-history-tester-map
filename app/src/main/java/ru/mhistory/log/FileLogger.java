package ru.mhistory.log;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class FileLogger {

    private static volatile FileLogger instance;
    public static String patchLogDir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    public static FileLogger from() {
        if (instance == null) {
            synchronized (FileLogger.class) {
                if (instance == null) {
                    instance = new FileLogger();
                }
            }
        }
        return instance;
    }

    private final LineAppender appender;

    private FileLogger() {
        String sdcardFilename = getLogFilePath();
        appender = new SDCardFileAppender(sdcardFilename);
    }

    public static String getLogsDirPath() {
        return patchLogDir;
    }

    public static String getLogFilePath() {
        return getLogsDirPath() + "/mh.log";
    }

    public void d(String tag, String message) {
        appender.append(buildLogLine(System.currentTimeMillis(), Log.DEBUG, tag, message));
    }

    public void v(String tag, String message) {
        appender.append(buildLogLine(System.currentTimeMillis(), Log.VERBOSE, tag, message));
    }

    public void i(String tag, String message) {
        appender.append(buildLogLine(System.currentTimeMillis(), Log.INFO, tag, message));
    }

    public void w(String tag, String message) {
        appender.append(buildLogLine(System.currentTimeMillis(), Log.WARN, tag, message));
    }

    public void e(String tag, String message) {
        appender.append(buildLogLine(System.currentTimeMillis(), Log.ERROR, tag, message));
    }

    public void a(String tag, String message) {
        appender.append(buildLogLine(System.currentTimeMillis(), Log.ASSERT, tag, message));
    }

    public void w(String tag, String message, Throwable e) {
        final long time = System.currentTimeMillis();
        Holder holder = getHolder();
        format(holder, time);
        appender.append(buildLogLine(holder, Log.WARN, tag, message));
        log(holder, Log.WARN, tag, e);
    }

    public void e(String tag, String message, Throwable e) {
        final long time = System.currentTimeMillis();
        Holder holder = getHolder();
        format(holder, time);
        appender.append(buildLogLine(holder, Log.WARN, tag, message));
        log(holder, Log.WARN, tag, e);
    }

    void flush() {
        appender.flush();
    }

    private void log(Holder holder, int level, String tag, Throwable e) {
        boolean isNested = false;
        do {
            appender.append(buildLogLine(holder.lastTime, level, tag,
                    (isNested ? "Caused by: " : "") + e));
            StackTraceElement[] stackTraceElements = e == null ? null : e.getStackTrace();
            if (stackTraceElements != null) {
                for (StackTraceElement element : stackTraceElements) {
                    appender.append(buildLogLine(holder.lastTime, level, tag, element.toString()));
                }
            }

            if (e != null) {
                e = e.getCause();
                isNested = true;
            }
        } while (e != null);
    }

    public void start() {
        appender.start();
    }

    public void stop() {
        appender.stop();
    }

    private static class Holder {
        final Time date = new Time();
        final StringBuilder sb = new StringBuilder();
        long lastTime;
        String lastTimeFormatted;
    }

    private ThreadLocal<Holder> holderThreadLocal = new ThreadLocal<>();

    // Use holder in order to minimize synchronization overhead
    private Holder getHolder() {
        Holder holder = holderThreadLocal.get();
        if (holder == null) {
            holder = new Holder();
            holderThreadLocal.set(holder);
        }
        return holder;
    }

    private String format(Holder holder, long time) {
        if (holder.lastTime == time && holder.lastTimeFormatted != null) {
            return holder.lastTimeFormatted;
        }
        final Time date = holder.date;
        date.set(time);
        final String timeFormatted = formatForLog(date, time, holder.sb);
        holder.lastTimeFormatted = timeFormatted;
        holder.lastTime = time;
        return timeFormatted;
    }

    private String buildLogLine(long time, int level, String tag, String message) {
        Holder holder = getHolder();
        format(holder, time);
        return buildLogLine(holder, level, tag, message);
    }

    private String buildLogLine(Holder holder, int level, String tag, String message) {
        StringBuilder sb = holder.sb;
        sb.setLength(0);
        sb.append(holder.lastTimeFormatted);
        switch (level) {
            default:
            case Log.DEBUG:
                sb.append(" D/");
                break;
            case Log.ASSERT:
                sb.append(" A/");
                break;
            case Log.ERROR:
                sb.append(" E/");
                break;
            case Log.INFO:
                sb.append(" I/");
                break;
            case Log.VERBOSE:
                sb.append(" V/");
                break;
            case Log.WARN:
                sb.append(" W/");
                break;
        }
        sb.append(tag).append(": ").append(message);
        return sb.toString();
    }

    public static String formatForLog(Time time, long timeMs, StringBuilder sb) {
        if (time == null) {
            return "";
        }
        if (sb == null) {
            sb = new StringBuilder();
        }
        sb.setLength(0);
        final int millis = (int) (timeMs % 1000L);
        sb.append(time.year).append('-');
        append2Digits(sb, time.month + 1).append('-');
        append2Digits(sb, time.monthDay).append(' ');
        append2Digits(sb, time.hour).append(':');
        append2Digits(sb, time.minute).append(':');
        append2Digits(sb, time.second).append('.');
        append3Digits(sb, millis);
        return sb.toString();
    }

    private final static ThreadLocal<Time> timeLocal = new ThreadLocal<Time>();

    private static Time obtainTimeInstance() {
        Time time = timeLocal.get();
        if (time == null) {
            time = new Time();
            timeLocal.set(time);
        }
        return time;
    }

    /**
     * Parses time in default time zone from format used for logging to file.
     */
    public static long parseTimeFromLog(String time) {
        try {
            final int year = Integer.parseInt(time.substring(0, 4));
            final int month = Integer.parseInt(time.substring(5, 7)) - 1;
            final int day = Integer.parseInt(time.substring(8, 10));
            final int hour = Integer.parseInt(time.substring(11, 13));
            final int minute = Integer.parseInt(time.substring(14, 16));
            final int second = Integer.parseInt(time.substring(17, 19));
            final int millis = Integer.parseInt(time.substring(20, 23));

            final Time t = obtainTimeInstance();
            t.set(second, minute, hour, day, month, year);
            return t.toMillis(false) + millis;

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse time: " + new String(time.substring(0, 23)), e);
        }
    }

    private static StringBuilder append2Digits(StringBuilder sb, int value) {
        if (value < 10) {
            sb.append('0');
        }
        sb.append(value);
        return sb;
    }

    private static StringBuilder append3Digits(StringBuilder sb, int value) {
        if (value < 10) {
            sb.append('0');
        }
        if (value < 100) {
            sb.append('0');
        }
        sb.append(value);
        return sb;
    }
}
