package ru.mhistory.playback;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;

public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
    private final AudioManager audioManager;
    private final AudioFocusable audioFocusable;

    public AudioFocusHelper(@NonNull Context ctx,
                            @NonNull AudioFocusable focusable) {
        audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        audioFocusable = focusable;
    }

    /**
     * Requests audio focus. Returns whether request was successful or not.
     */
    public boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * Abandons audio focus. Returns whether request was successful or not.
     */
    public boolean abandonFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    /**
     * Called by AudioManager on audio focus changes. We implement this by calling our
     * AudioFocusable appropriately to relay the message.
     */
    public void onAudioFocusChange(int focusChange) {
        if (audioFocusable == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                audioFocusable.onGainedAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                audioFocusable.onLostAudioFocus(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                audioFocusable.onLostAudioFocus(true);
                break;
            default:
        }
    }
}