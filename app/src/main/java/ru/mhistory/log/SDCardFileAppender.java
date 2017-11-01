package ru.mhistory.log;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Appender writes to a file on SD card.
 * This class's methods are thread safe.
 */
public class SDCardFileAppender implements LineAppender {
    private static final String LOG_TAG = SDCardFileAppender.class.getSimpleName();

    private static final long FLUSH_INTERVAL_MS = 15000;
    private static final int MAX_LINES_IN_BUFFER = 100000;

    private static volatile int instanceCount = 0;
    private int instanceNum = ++instanceCount;

    private final File file;
    private final ConcurrentLinkedQueue<String> linesBuffer = new ConcurrentLinkedQueue<String>();
    private Thread flusherThread;

    /**
     * @param sdCardRelativePathName pathname of a file on sdcard where the output should be writted to.
     *                               Normally, it should be "Android/data/package-name/logs/logfile.txt"
     */
    public SDCardFileAppender(String sdCardRelativePathName) {
        File externalRoot = Environment.getExternalStorageDirectory();
        file = new File(externalRoot, sdCardRelativePathName);
    }

    @Override
    public void append(String line) {
        if (linesBuffer.size() < MAX_LINES_IN_BUFFER) {
            linesBuffer.add(line);
        }
    }

    public synchronized void flush() {
        Log.d(LOG_TAG, String.format("Flush, lines buffer = (%d)", linesBuffer.size()));
        if (linesBuffer.isEmpty()) {
            return;
        }

        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(LOG_TAG, "Failed to create directory: " + dir.getPath());
            return;
        }

        PrintWriter out = null;
        try {
            FileOutputStream outStream = new FileOutputStream(file, true);
            out = new PrintWriter(outStream);
            while (!linesBuffer.isEmpty()) {
                String line = linesBuffer.peek();
                out.println(line);
                linesBuffer.poll();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to flush to file: " + e, e);

        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Throwable ignored) {
                }
            }
        }

    }

    @Override
    public void start() {
        flusherThread = new Thread("SDCardFileAppender-" + instanceNum) {
            @Override
            public void run() {
                while (true) {
                    try {
                        flush();
                        if (Thread.currentThread().isInterrupted()) {
                            Log.d(LOG_TAG, "Has pending interrupt request");
                            break;
                        }
                        Thread.sleep(FLUSH_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        flush();
                        Log.e(LOG_TAG, "Interrupted: " + e, e);
                        break;
                    }
                }
            }
        };
        flusherThread.setPriority(Thread.MIN_PRIORITY);
        flusherThread.start();
    }

    @Override
    public void stop() {
        if (flusherThread != null && flusherThread.isAlive()) {
            flusherThread.interrupt();
            flusherThread = null;
        }
    }
}
