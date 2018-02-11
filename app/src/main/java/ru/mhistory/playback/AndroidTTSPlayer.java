package ru.mhistory.playback;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.AppPrepareTTSCompleteEvent;
import ru.mhistory.common.util.ThreadUtil;
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
    boolean isFlip=false;
    HashMap<String, String> map = new HashMap<String, String>();
    HashMap<String, String> mapP = new HashMap<String, String>();
    private final String typePlayTrack = "Track";
    private final String typePlayPreambula = "Preambula";
    private Handler mHandler = new Handler();


    private float LetterPerSecond = 12.8f; //количество знаков в секунду
    private int minDuration = 10;
    private int currentSleep=1000;

    private long timeStartPlay;
    private int progress;

    public AndroidTTSPlayer(Context context, @NonNull Callbacks callbacks, @NonNull PreambulaCallback preambulaCallback) {
        this.context = context;
        this.preambulaCallback = preambulaCallback;
        textToSpeech = new TextToSpeech(context, this, "com.google.android.tts");
        this.callbacks = callbacks;
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, typePlayTrack);
        //map.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true");
        mapP.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, typePlayPreambula);
        textToSpeech.setOnUtteranceProgressListener(this);
        Logger.d(LogType.Player, textToSpeech.getDefaultEngine());

    }

    @Override
    public void prepareAsync(@NonNull String url) {
        Logger.d(LogType.Player, "prepareAsync from TTS %s", url);
        checkState(State.IDLE);
        if(textToSpeech==null)  textToSpeech = new TextToSpeech(context, this, "com.google.android.tts");
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
        if(state == State.PAUSED)toPosition((int) (100*progress/getDuration()));
        else toPlay(strTTS);

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
        checkState(State.PLAYING);
        state = State.PAUSED;
        mHandler.removeCallbacks(timer);
        textToSpeech.stop();


    }

    @Override
    public void flip(String nextTrack) {
        isFlip=true;
        strTTS=nextTrack;
        textToSpeech.stop();
        state = State.READY;
        callbacks.onReady();
    }

    @Override
    public void toPosition(int position) {
        state = State.PAUSED;
        int start = position * strTTS.length() / 100;
        int end = strTTS.length();
        char[] buf = new char[end - start];
        strTTS.getChars(start, end, buf, 0);
        String str = new String(buf);
        toPlay(str);
    }

    @Override
    public void stop() {


        textToSpeech.stop();
    }

    @Override
    public void release() {
        state = State.IDLE;
    }

    @Override
    public long getDuration() {
        long duration = (long) (1000*strTTS.length() / LetterPerSecond);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21(locale);
            } else {
                ttsUnder20();
            }

            textToSpeech.setSpeechRate(0.7f);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Извините, этот язык не поддерживается");
            } else {
                initStatus = true;
                            }
        } else {
            Log.e("TTS", "Ошибка!");
        }
        ThreadUtil.runOnUiThread(() -> {
            BusProvider.getInstance().post(new AppPrepareTTSCompleteEvent(initStatus));
        });
    }

    private void ttsUnder20() {
    }


    private void ttsGreater21(Locale locale) {
        String[] ruVoice={"ru-ru-x-dfc#female_3-local","ru-ru-x-dfc#male_2-local","ru-ru-x-dfc#male_1-local",
        "ru-ru-x-dfc-local","ru-ru-x-dfc#female_1-local","ru-ru-x-dfc#female_2-local","ru-RU-language",
                "ru-ru-x-dfc-network","ru-ru-x-dfc#male_3-local"};

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            for (Voice tmpVoice : textToSpeech.getVoices()) {
//                 if (tmpVoice.getName().contains(ruVoice[1])) {
//                     Logger.d(LogType.Tester, "find voice ");
//                     textToSpeech.setVoice(tmpVoice);
//                     break;
//                 }}
//            Logger.d(LogType.Tester, textToSpeech.getVoice().getName());
//        }


    }

    private void onComplete() {

       if(!isFlip) state = State.ENDED;
//        try {
//            Thread.sleep(currentSleep);
//        } catch (InterruptedException e) {
//
//        }
        callbacks.onEnded(isFlip);
        isFlip=false;
    }


    Runnable timer = new Runnable() {
        @Override
        public void run() {
            int delta = strTTS.length() - strToPlay.length();
            int deltatime = (int) ((System.currentTimeMillis() - timeStartPlay));
            int d= (int) (delta*1000/LetterPerSecond);
            progress = (deltatime + d);
            mHandler.postDelayed(timer, 100);
        }
    };

    @Override
    public void onStart(String s) {
        Logger.d(LogType.Player, "TTS play %s", s);
        isFlip=false;
        switch (s) {
            case typePlayPreambula:
                mHandler.removeCallbacks(timer);
                break;
            default:
                state = State.PLAYING;
                timeStartPlay = System.currentTimeMillis();
                mHandler.removeCallbacks(timer);
                mHandler.postDelayed(timer, 1);
                break;

        }

    }

    @Override
    public void onDone(String s) {
        Logger.d(LogType.Player, "tts finish %s", s);
       switch (s) {
            case typePlayTrack:
                if(state == State.PAUSED || state== State.IDLE) return;
                if(progress/1000<minDuration && isFlip){ isFlip=false;return;}
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
