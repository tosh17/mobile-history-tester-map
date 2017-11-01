package ru.mhistory.playback;

import android.support.annotation.NonNull;

public class PlayList {
    private final String name;
    private final String[] playbackUrls;
    private int currentTrackIndex = -1;

    public PlayList(@NonNull String name, @NonNull String[] playbackUrls) {
        checkCount(playbackUrls);
        this.name = name;
        this.playbackUrls = playbackUrls;
    }

    private void checkCount(@NonNull String[] playbackUrls) {
        if (playbackUrls.length == 0) {
            throw new IllegalArgumentException("Can't create empty play list");
        }
    }

    public void next() {
        currentTrackIndex++;
    }

    public void reset() {
        currentTrackIndex = -1;
    }

    public int getLength() {
        return playbackUrls.length;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getPlaybackUrl() {
        checkTrackIndex();
        return playbackUrls[currentTrackIndex];
    }

    private void checkTrackIndex() {
        if (currentTrackIndex == -1 || currentTrackIndex >= playbackUrls.length) {
            throw new IllegalStateException("Can't call method before next() or when no more urls");
        }
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public boolean hasNextTrack() {
        return currentTrackIndex < playbackUrls.length - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayList playList = (PlayList) o;
        return name != null ? name.equals(playList.name) : playList.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
