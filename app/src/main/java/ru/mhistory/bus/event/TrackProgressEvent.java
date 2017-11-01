package ru.mhistory.bus.event;

public class TrackProgressEvent {
    public final long currentDurationMs;
    public final long totalDurationMs;

    public TrackProgressEvent(long currentDurationMs, long totalDurationMs) {
        this.currentDurationMs = currentDurationMs;
        this.totalDurationMs = totalDurationMs;
    }
}