package ru.mhistory.playback;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;

/**
 * Created by shcherbakov on 21.11.2017.
 */

public class TtsToWav implements TextToSpeech.OnInitListener{
    TextToSpeech textToSpeech;
    TTSCallback callback;
    HashMap<String, String> map = new HashMap<String, String>();
    private long x;
    private String text;
    private String path;
    private boolean isInit=false;

    public TtsToWav(Context context, TTSCallback callback) {
        textToSpeech=new TextToSpeech(context,this);
        this.callback = callback;
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                x=System.currentTimeMillis();
                Logger.d(LogType.Player, "Start TTS generate size "+ text.length());
            }

            @Override
            public void onDone(String s) {
                x=System.currentTimeMillis()-x;
                Logger.d(LogType.Player, "Done TTS generate size "+ text.length()+ " time " + x);
                callback.ttsDone("temp.wav");
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    public void generator(String text,String path){
        this.text=text;
        this.path=path;
        textToSpeech.synthesizeToFile(text,map,path);
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
            } else textToSpeech.synthesizeToFile(text,map,path);
        } else {
            Log.e("TTS", "Ошибка!");
        }
    }
}
