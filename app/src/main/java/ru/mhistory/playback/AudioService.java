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
import ru.mhistory.bus.event.TrackPlaybackEndedEvent;
import ru.mhistory.bus.event.PlaybackStopEvent;
import ru.mhistory.bus.event.TrackProgressEvent;
import ru.mhistory.common.util.FileUtil;
import ru.mhistory.log.Logger;

public class AudioService extends Service implements AudioPlayer.Callbacks, AudioFocusable {
    public static final String KEY_AUDIO_URL = "AUDIO_URL";
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
            Action.PLAY_OR_PAUSE,
            Action.PLAY_TO_POSITION,
            Action.STOP,
            Action.NEXT,
            Action.URL,
    })
    public @interface Action {
        String PLAY_OR_PAUSE = "ru.mhistory.audioservice.action.PLAY_OR_PAUSE";
        String PLAY_TO_POSITION = "ru.mhistory.audioservice.action.PLAY_TO_POSITION";
        String STOP = "ru.mhistory.audioservice.action.STOP";
        String NEXT = "ru.mhistory.audioservice.action.NEXT";
        String URL = "ru.mhistory.audioservice.action.URL";

    }

    private Runnable audioTrackProgressRunnable = new Runnable() {
        public void run() {
            sendTrackDurationsUpdateEvent(audioPlayer.getCurrentPosition(),
                    audioPlayer.getDuration());
            trackProgressHandler.postDelayed(this, 100);
        }
    };

    private PowerManager.WakeLock powerLock;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private AudioPlayer audioPlayer;
    private AudioFocusHelper audioFocusHelper;
    private Handler trackProgressHandler;
    @AudioFocus private int audioFocus;
    private File externalRootDir;
    private String currentAudioTrackUrl;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioPlayer = new AndroidAudioPlayer(this);
        notificationManager = getNotificationManager();
        audioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        trackProgressHandler = new Handler();
        powerLock = getPowerLock();
        externalRootDir = getRootDir();
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
            case Action.NEXT:
                onNextAction();
                break;
            case Action.URL:
                String url = intent.getExtras().getString(KEY_AUDIO_URL);
                if (!TextUtils.isEmpty(url)) {
                    onAudioTrackUrlAvailable(url);
                }
                break;
        }
        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }

    @NonNull
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void onPlayOrPauseAction() {
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PLAYING) {
            onPauseAction();
        } else {
            onPlayAction();
        }
    }

    private void onPlayAction() {
        Logger.d("Play action received, player state (%s)", audioPlayer.getPlaybackState());
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PREPARING) {
//            startPlayingAfterRetrieve = true;
            return;
        }
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PAUSED) {
            tryToGetAudioFocus();
            setUpAsForeground(FileUtil.getNameFromPath(currentAudioTrackUrl), "playing...");
            configAndStartMediaPlayer();
        }
    }

    private void onPauseAction() {
        Logger.d("Pause action received, player state (%s)", audioPlayer.getPlaybackState());
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PREPARING) {
            // If we are still retrieving media, clear the flag that indicates we should start
            // playing when we're ready
//            startPlayingAfterRetrieve = false;
            return;
        }
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PLAYING) {
            audioPlayer.pause();
            sendCanPlayEvent();
            releaseResources(false);
        }
    }
    private void toPosition(int position){
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PLAYING || audioPlayer.getPlaybackState() == AudioPlayer.State.PAUSED) {
            audioPlayer.toPosition(position);

        }

    }
    private void onStopAction() {
        Logger.d("Stop action received...");
        sendPlaybackStopEvent();
        releaseResources(true);
        giveUpAudioFocus();
        stopSelf();
    }

    private void onNextAction() {
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.PLAYING
                || audioPlayer.getPlaybackState() == AudioPlayer.State.PAUSED) {
            // nothing to do right now
        }
    }

    @SuppressLint("SwitchIntDef")
    private void onAudioTrackUrlAvailable(@NonNull String audioTrackUrl) {
        switch (audioPlayer.getPlaybackState()) {
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
        if (releaseAudioPlayer && audioPlayer != null) {
            audioPlayer.release();
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
        if (audioFocus == AudioFocus.NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (audioPlayer.getPlaybackState() == AudioPlayer.State.PLAYING) {
                audioPlayer.pause();
                sendCanPlayEvent();
            }
            return;
        } else if (audioFocus == AudioFocus.NO_FOCUS_CAN_DUCK) {
//            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
        } else {
//            mPlayer.setVolume(1.0f, 1.0f);
        }
        if (audioPlayer.getPlaybackState() == AudioPlayer.State.READY
                || audioPlayer.getPlaybackState() == AudioPlayer.State.PAUSED) {
            startAudioTrackProgress();
            audioPlayer.play();
            sendCanPauseEvent();
        }
    }

    private void updateNotification(@NonNull String text) {
        notificationBuilder.setContentText(text);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
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
        Logger.d("Playback is ready for (%s)", currentAudioTrackUrl);
        updateNotification("playing...");
        startAudioTrackProgress();
        sendNextTrackInfoEvent();
        audioPlayer.play();
        sendCanPauseEvent();
    }

    @Override
    public void onEnded() {
        Logger.d("Playback is ended for (%s)", currentAudioTrackUrl);
        sendTrackPlaybackEndedEvent();
        releaseResources(true);
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
        BusProvider.getInstance().post(new NextTrackInfoEvent(currentAudioTrackUrl,
                audioPlayer.getDuration(), 1, 1));
    }

    private void sendTrackDurationsUpdateEvent(long currentDuration, long totalDuration) {
        BusProvider.getInstance().post(new TrackProgressEvent(currentDuration, totalDuration));
    }

    private void sendTrackPlaybackEndedEvent() {
        BusProvider.getInstance().post(new TrackPlaybackEndedEvent(currentAudioTrackUrl));
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