package ru.mhistory.bus.event;

import android.support.annotation.NonNull;

public class NextTrackInfoEvent {
    public final String track;
    public final String trackName;
    public final long duration;
    public final int trackSequence;
    public final int totalTrackCount;

    public NextTrackInfoEvent(@NonNull String trackName, String track,
                              long duration,
                              int trackSequence,
                              int totalTrackCount) {
        this.track=track;
        this.trackName = trackName;
        this.duration = duration;
        this.trackSequence = trackSequence;
        this.totalTrackCount = totalTrackCount;
    }
}
