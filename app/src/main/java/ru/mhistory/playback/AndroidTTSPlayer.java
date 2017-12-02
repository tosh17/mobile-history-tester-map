package ru.mhistory.playback;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;

/**
 * Created by shcherbakov on 12.11.2017.
 */

public class AndroidTTSPlayer extends UtteranceProgressListener implements AudioPlayer, TextToSpeech.OnInitListener {
    private final Callbacks callbacks;
    private final PreambulaCallback preambulaCallback;
    private final Context context;
    private @State
    int lastState;
    private @State
    int state = State.IDLE;
    TextToSpeech textToSpeech;
    private boolean initStatus = false;
    String strTTS = "";
    String strToPlay = "";
    HashMap<String, String> map = new HashMap<String, String>();
    HashMap<String, String> mapP = new HashMap<String, String>();
    private final String typePlayTrack = "Track";
    private final String typePlayPreambula = "Preambula";
    private Handler mHandler = new Handler();


    private final int LonS = 13;
    //todo вынести в конфиг?
    private long timeStartPlay;
    private int progress;

    public AndroidTTSPlayer(Context context, @NonNull Callbacks callbacks, @NonNull PreambulaCallback preambulaCallback) {
        this.context = context;
        this.preambulaCallback = preambulaCallback;
        textToSpeech = new TextToSpeech(context, this, "com.google.android.tts");
        this.callbacks = callbacks;
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, typePlayTrack);
        mapP.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, typePlayPreambula);
        textToSpeech.setOnUtteranceProgressListener(this);
        Logger.d(LogType.Player, textToSpeech.getDefaultEngine());

    }

    @Override
    public void prepareAsync(@NonNull String url) {
        Logger.d(LogType.Player, "prepareAsync from TTS %s", url);
        checkState(State.IDLE);
        //Todo textToSpeech==null
        strTTS = url;
        state = State.READY;
        callbacks.onReady();

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
        Logger.d(LogType.Player, "play tts to " + strTTS);
        toPlay(strTTS);

    }

    private void toPlay(String strToPlay) {
        this.strToPlay = strToPlay;
        textToSpeech.speak(strToPlay, TextToSpeech.QUEUE_FLUSH, map);
    }

    public void toPlayPreambula(String strToPlay) {
        this.strToPlay = strToPlay;
        textToSpeech.speak(strToPlay, TextToSpeech.QUEUE_FLUSH, mapP);
    }

    @Override
    public void pause() {

    }

    @Override
    public void toPosition(int position) {
        // TODO: сделать перемещение (stop), обрезать текст, отправить в tts
        int start = position * strTTS.length() / 100;
        int end = strTTS.length();
        char[] buf = new char[end - start];
        strTTS.getChars(start, end, buf, 0);
        String str = new String(buf);
        toPlay(str);
    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

        state = State.IDLE;
    }

    @Override
    public long getDuration() {
        long duration = strTTS.length() / LonS;
        return duration;
    }

    @Override
    public long getCurrentPosition() {
        return progress;
    }

    @Override
    public void block(boolean isBlock) {
        if (isBlock) {
            state = State.BLOCK;
            lastState = state;
        } else state = lastState;
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
        Logger.d(LogType.Player, "TTS init ");
        if (status == TextToSpeech.SUCCESS) {

            Locale locale = new Locale("ru");
            int result = textToSpeech.setLanguage(locale);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Извините, этот язык не поддерживается");
            } else {
                initStatus = true;

            }
        } else {
            Log.e("TTS", "Ошибка!");
        }
    }

    private void onComplete() {

        state = State.ENDED;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        callbacks.onEnded();
    }


    Runnable timer = new Runnable() {
        @Override
        public void run() {
            int delta = strTTS.length() - strToPlay.length();
            int deltaprogress = 100 * delta / strTTS.length();
            int deltatime = (int) ((System.currentTimeMillis() - timeStartPlay) / 1000);
            int d = (int) (100 * deltatime * LonS / strToPlay.length());
            progress = deltaprogress + d;
            mHandler.postDelayed(timer, 100);
        }
    };

    @Override
    public void onStart(String s) {
        Logger.d(LogType.Player, "TTS play %s", s);
        switch (s) {
            case typePlayTrack:
                state = State.PLAYING;
                timeStartPlay = System.currentTimeMillis();
                mHandler.removeCallbacks(timer);
                mHandler.postDelayed(timer, 1);
                break;
            case typePlayPreambula:
                break;
        }

    }

    @Override
    public void onDone(String s) {
        Logger.d(LogType.Player, "tts finish %s", s);
        switch (s) {
            case typePlayTrack:
                onComplete();
                break;
            case typePlayPreambula:
                preambulaCallback.preambulaEnded();
                break;
        }
    }

    /**
     * @param s
     * @deprecated
     */
    @Override
    public void onError(String s) {

    }
}
