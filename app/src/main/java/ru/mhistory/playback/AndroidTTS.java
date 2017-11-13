package ru.mhistory.playback;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by shcherbakov on 12.11.2017.
 */

public class AndroidTTS implements AudioPlayer,TextToSpeech.OnInitListener {
    private final Callbacks callbacks;
    private @AudioPlayer.State
    int state = AudioPlayer.State.IDLE;
    TextToSpeech textToSpeech;
    String strTTS="";

    public AndroidTTS(Context context, @NonNull Callbacks callbacks) {
        textToSpeech = new TextToSpeech(context,this);
        this.callbacks = callbacks;
    }

    @Override
    public void prepareAsync(@NonNull String url) {
        checkState(State.IDLE);
        strTTS=url;
    }

    @Override
    public void prepareAsync(@NonNull FileDescriptor fd, long offset, long length) {

    }

    @Override
    public void prepareAsync(@NonNull FileDescriptor fd) {

    }

    @Override
    public int getPlaybackState() {
        return state;
    }

    @Override
    public void play() {
        checkState(State.READY, State.PAUSED);
        state = State.PLAYING;
        textToSpeech.speak(strTTS, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void pause() {

    }

    @Override
    public void toPosition(int position) {
        // TODO: сделать перемещение (stop), обрезать текст, отправить в tts
    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        return 0;
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
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            Locale locale = new Locale("ru");

            int result = textToSpeech.setLanguage(locale);
            //int result = mTTS.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Извините, этот язык не поддерживается");
            } else {


            }

        } else {
            Log.e("TTS", "Ошибка!");
        }
    }

    private void onComplete(){

        while (textToSpeech.isSpeaking());
        state = State.ENDED;
        callbacks.onEnded();
    }
}