package ru.mhistory.log;

public interface LineAppender {
    void append(String line);

    void flush();

    void start();

    void stop();
}
