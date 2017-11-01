package ru.mhistory.playback;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Arrays;

import ru.mhistory.log.Logger;

public class AndroidAudioPlayer implements
        AudioPlayer,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    private final Callbacks callbacks;
    private @State int state = State.IDLE;

    public AndroidAudioPlayer(@NonNull Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    private void createMediaPlayerIfNeeded() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        }
    }

    @Override
    public void play() {
        checkState(State.READY, State.PAUSED);
        state = State.PLAYING;
        mediaPlayer.start();
    }

    @Override
    public void prepareAsync(@NonNull String playbackUrl) {
        checkState(State.IDLE);
        doPrepare(playbackUrl);
    }

    @Override
    public void prepareAsync(@NonNull FileDescriptor fd, long offset, long length) {
        checkState(State.IDLE);
        doPrepare(fd, offset, length);
    }

    @Override
    public void prepareAsync(@NonNull FileDescriptor fd) {
        checkState(State.IDLE);
        doPrepare(fd, 0, 0x7ffffffffffffffL);
    }

    private void doPrepare(FileDescriptor fd, long offset, long length) {
        try {
            createMediaPlayerIfNeeded();
            state = State.PREPARING;
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fd, offset, length);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            // TODO:
            state = State.IDLE;
            callbacks.onError();
        }
    }

    private void doPrepare(@NonNull String playbackUrl) {
        Logger.d("Preparing for %s", playbackUrl);
        try {
            createMediaPlayerIfNeeded();
            state = State.PREPARING;
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(playbackUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            // TODO:
            state = State.IDLE;
            callbacks.onError();
        }
    }

    @Override
    public int getPlaybackState() {
        return state;
    }

    @Override
    public void pause() {
        checkState(State.PLAYING);
        Logger.d("Pausing %s", mediaPlayer.getTrackInfo());
        state = State.PAUSED;
        mediaPlayer.pause();
    }

    public void stop() {
        checkState(State.READY, State.PLAYING, State.PAUSED, State.ENDED);
        Logger.d("Stopping %s", mediaPlayer.getTrackInfo());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void release() {
        Logger.d("Releasing %s", mediaPlayer.getTrackInfo());
        state = State.IDLE;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    private void checkState(@State Integer... expectedStates) {
        if (expectedStates != null && expectedStates.length > 0
                && !Arrays.asList(expectedStates).contains(state)) {
            throw new IllegalStateException(
                    "Current state: " + state + ", allowed states: "
                            + Arrays.toString(expectedStates));
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        checkState(State.PREPARING);
        Logger.d("Media player is prepared %s", mediaPlayer);
        state = State.READY;
        callbacks.onReady();
    }

    @Override
    public void onCompletion(@NonNull MediaPlayer mp) {
        checkState(State.PLAYING);
        Logger.d("On medial player completion %s", mediaPlayer);
        state = State.ENDED;
        callbacks.onEnded();
    }

    @Override
    public boolean onError(@NonNull MediaPlayer mp, int what, int extra) {
        state = State.IDLE;
        return callbacks.onError();
    }
}
