package ru.mhistory.playback;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import ru.mhistory.R;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.CanPauseEvent;
import ru.mhistory.bus.event.CanPlayEvent;
import ru.mhistory.bus.event.NextTrackInfoEvent;
import ru.mhistory.bus.event.PlaybackStopEvent;
import ru.mhistory.bus.event.TrackPlaybackEndedEvent;
import ru.mhistory.bus.event.TrackProgressEvent;
import ru.mhistory.common.util.FileUtil;
import ru.mhistory.common.util.ThreadUtil;
import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;

public class AudioService extends Service implements AudioPlayer.Callbacks, AudioPlayer.PreambulaCallback, AudioFocusable {

    private boolean isDebug = false;
    public static final String KEY_AUDIO_ID = "CONTENT_ID";
    public static final String KEY_AUDIO_URL = "AUDIO_URL";
    public static final String KEY_AUDIO_TRACK_NAME ="TRACK_NAME" ;
    public static final String KEY_IS_PREAMBULA = "IS_PREAMBULA";
    public static final String KEY_PREAMBULA = "PREAMBULA";
    public static final String KEY_AUDIO_POSITION = "AUDIO_POSITION";
    private static final int NOTIFICATION_ID = 1;


    @IntDef({
            AudioFocus.NO_FOCUS_NO_DUCK,
            AudioFocus.NO_FOCUS_CAN_DUCK,
            AudioFocus.FOCUSED,
    })
    public @interface AudioFocus {
        int NO_FOCUS_NO_DUCK = 0;
        int NO_FOCUS_CAN_DUCK = 1;
        int FOCUSED = 2;
    }

    @StringDef({
            Action.INIT,
            Action.PLAY_OR_PAUSE,
            Action.PLAY_TO_POSITION,
            Action.STOP,
            Action.ENDED,
            Action.NEXT,
            Action.URL,
            Action.TEXT,
    })
    public @interface Action {
        String INIT = "ru.mhistory.audioservice.action.INIT";
        String PLAY_OR_PAUSE = "ru.mhistory.audioservice.action.PLAY_OR_PAUSE";
        String PLAY_TO_POSITION = "ru.mhistory.audioservice.action.PLAY_TO_POSITION";
        String STOP = "ru.mhistory.audioservice.action.STOP";
        String ENDED = "ru.mhistory.audioservice.action.ENDED";
        String NEXT = "ru.mhistory.audioservice.action.NEXT";
        String URL = "ru.mhistory.audioservice.action.URL";
        String TEXT = "ru.mhistory.audioservice.action.TEXT";

    }

    private Runnable audioTrackProgressRunnable = new Runnable() {
        public void run() {
            sendTrackDurationsUpdateEvent(getNotBlockPlayer().getCurrentPosition(),
                    getNotBlockPlayer().getDuration());
            trackProgressHandler.postDelayed(this, 100);
        }
    };

    private PowerManager.WakeLock powerLock;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private AudioPlayer audioPlayer, ttsPlayer;
    private AudioFocusHelper audioFocusHelper;
    private Handler trackProgressHandler;
    @AudioFocus
    private int audioFocus;
    private File externalRootDir;
    private String currentAudioTrackUrl;
    private String currentAutioTrackName;
    private long currentAudioContentId;
    private boolean isCurrentFlip;
    private String AfterPreambule;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        if (isDebug) android.os.Debug.waitForDebugger();


        audioPlayer = new AndroidAudioPlayer(this);
        ttsPlayer = new AndroidTTSPlayer(getBaseContext(), this, this);
        notificationManager = getNotificationManager();
        audioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        trackProgressHandler = new Handler();
        powerLock = getPowerLock();
        externalRootDir = getRootDir();
    }

    private AudioPlayer getNotBlockPlayer() {
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.BLOCK)
            return ttsPlayer;
        return audioPlayer;

    }

    @Nullable
    private File getRootDir() {
        final File externalRoot = Environment.getExternalStorageDirectory();
        if (externalRoot == null) {
            Log.e(getClass().getSimpleName(), "External storage root not found");
            return null;
        }
        return externalRoot;
    }

    @NonNull
    private PowerManager.WakeLock getPowerLock() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                this.getClass().getName());
        wakeLock.setReferenceCounted(true);
        return wakeLock;
    }

    @Override
    public void onDestroy() {
        releaseResources(true);
        giveUpAudioFocus();
        if (powerLock.isHeld()) {
            powerLock.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case Action.INIT:
                break;
            case Action.PLAY_OR_PAUSE:
                onPlayOrPauseAction();
                break;
            case Action.PLAY_TO_POSITION:
                int position = intent.getExtras().getInt(KEY_AUDIO_POSITION);
                toPosition(position);
                break;
            case Action.STOP:
                onStopAction();
                break;
            case Action.ENDED:
                onEnded(false);
                break;
            case Action.NEXT:
                currentAudioTrackUrl = intent.getExtras().getString(KEY_AUDIO_URL);
                currentAutioTrackName= intent.getExtras().getString(KEY_AUDIO_TRACK_NAME);
                onNextAction(currentAudioTrackUrl);
                break;
            case Action.URL:
                String url = intent.getExtras().getString(KEY_AUDIO_URL);
                currentAudioContentId = intent.getExtras().getLong(KEY_AUDIO_ID);
                currentAutioTrackName= intent.getExtras().getString(KEY_AUDIO_TRACK_NAME);
                Logger.d(LogType.Player, "intent to mp3-> " + url);
                if (!TextUtils.isEmpty(url)) {
                    ttsPlayer.block(true);
                    if (intent.getExtras().getBoolean(KEY_IS_PREAMBULA)) {
                        AfterPreambule = url;
                        onTrackPreambula(intent.getExtras().getString(KEY_PREAMBULA));
                    } else onAudioTrackUrlAvailable(url);
                }
                break;
            case Action.TEXT:
                String text = intent.getExtras().getString(KEY_AUDIO_URL);
                currentAutioTrackName= intent.getExtras().getString(KEY_AUDIO_TRACK_NAME);
                currentAudioContentId = intent.getExtras().getLong(KEY_AUDIO_ID);
                Logger.d(LogType.Player, "intent to tts-> " + text);
                if (!TextUtils.isEmpty(text)) {
                    audioPlayer.block(true);
                    if (intent.getExtras().getBoolean(KEY_IS_PREAMBULA)) {
                        AfterPreambule = text;
                        onTrackPreambula(intent.getExtras().getString(KEY_PREAMBULA));
                    } else onAudioTrackUrlAvailable(text);
                }
                break;
        }
        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }

    private void onTrackPreambula(String string) {
        ((AndroidTTSPlayer) ttsPlayer).toPlayPreambula(string);
    }

    @Override
    public void preambulaEnded() {
        onAudioTrackUrlAvailable(AfterPreambule);
    }

    @NonNull
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void onPlayOrPauseAction() {
        Logger.d(LogType.Player, "nPlayOrPauseAction -> " + getNotBlockPlayer().getClass().toString());
        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PLAYING) {
            onPauseAction();
        } else {
            onPlayAction();
        }
    }

    private void onPlayAction() {
        Logger.d(LogType.Player, "Play action received, player state (%s)", audioPlayer.getPlaybackState());
        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PREPARING) {
//            startPlayingAfterRetrieve = true;
            return;
        }
        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PAUSED) {
            tryToGetAudioFocus();
            setUpAsForeground(FileUtil.getNameFromPath(currentAudioTrackUrl), "playing...");
            configAndStartMediaPlayer();
        }
    }

    private void onPauseAction() {
        Logger.d(LogType.Player, "Pause action received, player state (%s)", getNotBlockPlayer().getPlaybackState());
        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PREPARING) {
            // If we are still retrieving media, clear the flag that indicates we should start
            // playing when we're ready
//            startPlayingAfterRetrieve = false;
            return;
        }
        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PLAYING) {
            getNotBlockPlayer().pause();
            sendCanPlayEvent();
            releaseResources(false);
        }
    }

    private void toPosition(int position) {
        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PLAYING || getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PAUSED) {
            getNotBlockPlayer().toPosition(position);

        }

    }

    private void onStopAction() {
        Logger.d(LogType.Player, "Stop action received...");
        sendPlaybackStopEvent();
        releaseResources(true);
        giveUpAudioFocus();
        stopSelf();
    }

    private void onNextAction(String nextTrack) {
        getNotBlockPlayer().flip(nextTrack);
//        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PLAYING
//                || getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PAUSED) {
//            // nothing to do right now
//
//        }
    }

    @SuppressLint("SwitchIntDef")
    private void onAudioTrackUrlAvailable(@NonNull String audioTrackUrl) {
        switch (getNotBlockPlayer().getPlaybackState()) {
            case AudioPlayer.State.IDLE:
            case AudioPlayer.State.ENDED:
            case AudioPlayer.State.PAUSED:
                playNextTrack(audioTrackUrl);
            case AudioPlayer.State.PLAYING:
                Logger.w("Already exists current audio track %s, player state is PLAYING",
                        currentAudioTrackUrl);
                break;
        }
    }

    private void playNextTrack(@NonNull String audioTrackUrl) {
        if (getNotBlockPlayer() == ttsPlayer) {
            currentAudioTrackUrl = audioTrackUrl;
            ttsPlayer.prepareAsync(currentAudioTrackUrl);
            configAndStartMediaPlayer();
            return;
        }
        tryToGetAudioFocus();
        if (audioTrackUrl.equals(currentAudioTrackUrl)) {
            setUpAsForeground(FileUtil.getNameFromPath(currentAudioTrackUrl), "playing...");
            configAndStartMediaPlayer();
        } else {
            if (currentAudioTrackUrl != null) {
                releaseResources(true);
            }
            currentAudioTrackUrl = audioTrackUrl;
            setUpAsForeground(FileUtil.getNameFromPath(currentAudioTrackUrl), "loading...");
            preparePlayerForTrack(currentAudioTrackUrl);
        }
    }

    private void preparePlayerForTrack(@NonNull String trackUrl) {
        File trackFile = new File(externalRootDir, "mobilehistory/" + trackUrl);
        if (trackFile.exists()) {
            audioPlayer.prepareAsync(trackFile.getAbsolutePath());
        } else {
            Log.w(getClass().getSimpleName(), "File not found for track " + trackUrl);
            currentAudioTrackUrl = null;
        }
    }

    private void releaseResources(boolean releaseAudioPlayer) {
        stopAudioTrackProgress();
        stopForeground(true);
        releaseAudioPlayerIfNeeded(releaseAudioPlayer);
    }

    private void startAudioTrackProgress() {
        trackProgressHandler.post(audioTrackProgressRunnable);
    }

    private void stopAudioTrackProgress() {
        trackProgressHandler.removeCallbacks(audioTrackProgressRunnable);
    }

    private void releaseAudioPlayerIfNeeded(boolean releaseAudioPlayer) {
        if (releaseAudioPlayer && getNotBlockPlayer() != null) {
            getNotBlockPlayer().release();
        }

        if (releaseAudioPlayer) {
            currentAudioTrackUrl = null;
        }
    }

    void tryToGetAudioFocus() {
        if (audioFocus != AudioFocus.FOCUSED && audioFocusHelper != null
                && audioFocusHelper.requestFocus()) {
            audioFocus = AudioFocus.FOCUSED;
        }
    }

    private void giveUpAudioFocus() {
        if (audioFocus == AudioFocus.FOCUSED && audioFocusHelper != null
                && audioFocusHelper.abandonFocus()) {
            audioFocus = AudioFocus.NO_FOCUS_NO_DUCK;
        }
    }

    @Override
    public void onGainedAudioFocus() {
        Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        audioFocus = AudioFocus.FOCUSED;
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PLAYING) {
            configAndStartMediaPlayer();
        }
    }

    private void configAndStartMediaPlayer() {
        if(getNotBlockPlayer()==ttsPlayer){
            if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PLAYING) {
                getNotBlockPlayer().pause();
                sendCanPlayEvent();
                return;
            }
            if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.READY
                    || getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PAUSED) {
                startAudioTrackProgress();
                getNotBlockPlayer().play();
                sendCanPauseEvent();
            }
            return;
        }
        if (audioFocus == AudioFocus.NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PLAYING) {
                getNotBlockPlayer().pause();
                sendCanPlayEvent();
            }
            return;
        } else if (audioFocus == AudioFocus.NO_FOCUS_CAN_DUCK) {
//            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
        } else {
//            mPlayer.setVolume(1.0f, 1.0f);
        }
        if (getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.READY
                || getNotBlockPlayer().getPlaybackState() == AudioPlayer.State.PAUSED) {
            startAudioTrackProgress();
            getNotBlockPlayer().play();
            sendCanPauseEvent();
        }
    }

    private void updateNotification(@NonNull String text) {
        //Todo notificationBuilder он вообще нужен?
        return;
//        notificationBuilder.setContentText(text);
//        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void setUpAsForeground(@NonNull String title, @NonNull String text) {
        notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_stat_playing)
                .setTicker(title)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true);
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        audioFocus = canDuck ? AudioFocus.NO_FOCUS_CAN_DUCK : AudioFocus.NO_FOCUS_NO_DUCK;
        // start/restart/pause media player with new focus settings
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PLAYING) {
            configAndStartMediaPlayer();
        }
    }

    @Override
    public void onReady() {

        Logger.d(LogType.Player, "Playback is ready for (%s)", currentAudioTrackUrl);
        updateNotification("playing...");
        startAudioTrackProgress();
        sendNextTrackInfoEvent();
        getNotBlockPlayer().play();
        sendCanPauseEvent();
    }

    @Override
    public void onEnded(boolean isFlip) {
        Logger.d(LogType.Player, "Playback is ended for " + currentAudioTrackUrl);
        if(!isFlip) releaseResources(true);
        sendTrackPlaybackEndedEvent(isFlip);


    }

    @Override
    public void onStopped() {
    }

    @Override
    public void onPaused() {
    }

    @Override
    public boolean onError() {
        Toast.makeText(getApplicationContext(), "Media player error!", Toast.LENGTH_SHORT).show();
        releaseResources(true);
        giveUpAudioFocus();
        return true;
    }

    private void sendNextTrackInfoEvent() {
        ThreadUtil.runOnUiThread(() -> {
            BusProvider.getInstance().post(new NextTrackInfoEvent(currentAutioTrackName,currentAudioTrackUrl,
                    getNotBlockPlayer().getDuration(), 1, 1));
        });

    }

    private void sendTrackDurationsUpdateEvent(long currentDuration, long totalDuration) {
        BusProvider.getInstance().post(new TrackProgressEvent(currentDuration, totalDuration));
    }

    private void sendTrackPlaybackEndedEvent(boolean isFlip) { //Конец трека
        ThreadUtil.runOnUiThread(() -> {
            BusProvider.getInstance().post(new TrackPlaybackEndedEvent(currentAudioTrackUrl,currentAudioContentId,!isFlip));
        });

    }

    private void sendPlaybackStopEvent() {
        BusProvider.getInstance().post(new PlaybackStopEvent());
    }

    private void sendCanPlayEvent() {
        BusProvider.getInstance().post(new CanPlayEvent());
    }

    private void sendCanPauseEvent() {
        BusProvider.getInstance().post(new CanPauseEvent());
    }
}