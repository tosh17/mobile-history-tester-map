package ru.mhistory.bus.event;

import android.support.annotation.NonNull;

public class TrackPlaybackEndedEvent {
    public final String trackName;

    public TrackPlaybackEndedEvent(@NonNull String trackName) {
        this.trackName = trackName;
    }
}
