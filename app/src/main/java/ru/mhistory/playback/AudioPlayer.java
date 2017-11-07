package ru.mhistory.playback;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.FileDescriptor;

public interface AudioPlayer {


    @IntDef({
            State.IDLE,
            State.PREPARING,
            State.READY,
            State.PLAYING,
            State.PAUSED,
            State.ENDED
    })
    @interface State {
        int IDLE = 0; // player is idle, it's neither prepared or being prepared
        int PREPARING = 1; // player is preparing...
        int READY = 2;
        int PLAYING = 3; // playback active (but the media player may actually be paused in this
        // state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        int PAUSED = 4; // playback paused (media player ready!)
        int ENDED = 5;
    }

    interface Callbacks {
        void onReady();

        void onStopped();

        void onPaused();

        void onEnded();

        boolean onError();
    }

    void prepareAsync(@NonNull String url);

    void prepareAsync(@NonNull FileDescriptor fd, long offset, long length);

    void prepareAsync(@NonNull FileDescriptor fd);

    @State int getPlaybackState();

    void play();

    void pause();

    void toPosition(int position);

    void stop();

    void release();

    long getDuration();

    long getCurrentPosition();
}