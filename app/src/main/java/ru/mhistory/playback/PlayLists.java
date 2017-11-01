package ru.mhistory.playback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayLists {
    private final List<PlayList> playLists = new ArrayList<>();
    private final Map<String, PlayList> plMap = new HashMap<>();
    private int totalLength = 0;
    private PlayListsDelegate delegate;

    private PlayLists() {
    }

    public void setDelegate(@Nullable PlayListsDelegate delegate) {
        this.delegate = delegate;
    }

    private void checkDelegate() {
        if (delegate == null) {
            throw new IllegalStateException("Delegate is not set");
        }
    }

    public static PlayLists from(@NonNull PlayList playList) {
        PlayLists pls = new PlayLists();
        pls.addPlayListIfNotExists(playList);
        return pls;
    }

    public boolean addPlayListIfNotExists(@NonNull PlayList playList) {
        if (plMap.containsKey(playList.getName())) {
            return false;
        }
        plMap.put(playList.getName(), playList);
        playLists.add(playList);
        totalLength += playList.getLength();
        return true;
    }

    public void setCurrentPlayList(@NonNull String playListName) {
        checkDelegate();
        delegate.setCurrentPlayList(plMap.get(playListName));
    }

    public void next() {
        checkDelegate();
        delegate.next();
    }

    public boolean hasNextTrack() {
        checkDelegate();
        return delegate.hasNextTrack();
    }

    @NonNull
    public String getPlaybackUrl() {
        checkDelegate();
        PlayList pl = delegate.getCurrentPlayList();
        return pl.getPlaybackUrl();
    }

    @NonNull
    public PlayList getByIndex(int index) {
        return playLists.get(index);
    }

    public void reset() {
        checkDelegate();
        delegate.reset();
    }

    public int getSize() {
        return playLists.size();
    }

    public int getLength() {
        return totalLength;
    }

    public int getTrackSequence() {
        checkDelegate();
        return delegate.getTrackSequence();
    }

    public boolean contains(@NonNull String plName) {
        return plMap.containsKey(plName);
    }

    @Nullable
    public PlayList getCurrentPlayList() {
        checkDelegate();
        return delegate.getCurrentPlayList();
    }
}
