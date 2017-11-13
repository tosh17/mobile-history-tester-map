package ru.mhistory.screen.main.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.Space;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nononsenseapps.filepicker.FilePickerActivity;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.mhistory.R;
import ru.mhistory.common.util.PermissionUtils;

import ru.mhistory.screen.map.MapPresenter;

public class DebugInfoFragment extends Fragment implements SeekBar.OnSeekBarChangeListener{
    private static final int REQUEST_STORY_PICKER = 0;
    private static final int REQUEST_LOCATION_PERMISSIONS = 100;
    int wDev, hDev;
    float friendPersent = 0.10f;
    float buttonPlayerAdv = 0.11f;

    private MapPresenter presenter;
    private Unbinder unbinder;

    //    @BindView(R.id.nav_view)
//    NavigationView nv;
    @BindView(R.id.menuLinerFriend)
    LinearLayout layoutFriend;
    @BindView(R.id.textViewPlayAgain)
    TextView textViewPlayAgain;
    @BindView(R.id.textViewPlayAbout)
    TextView textViewPlayAbout;
    @BindView(R.id.textViewNotPause)
    TextView textViewNotPause;
    @BindView(R.id.lTrackInfo)
    LinearLayout layoutTrackFullInfo;
    @BindView(R.id.spaceTrackInfo)
    Space spaceMidle;

    @BindView(R.id.textViewTrackInfo)
    TextView textViewTrackInfo;
    @BindView(R.id.fabPrev)
    FloatingActionButton fabPrev;
    @BindView(R.id.fabNext)
    FloatingActionButton fabNext;

    @BindView(R.id.textViewPlayerTrackInfo)
    TextView textViewPlayerTrackInfo;
    @BindView(R.id.seekBarPlayer)
    SeekBar seekBarPlayer;
    private boolean isSeekBarTouch=false;
    @BindView(R.id.textViewPlayerCurrentTime)
    TextView textViewPlayerCurrentTime;
    @BindView(R.id.textViewPlayerTotalTime)
    TextView textViewPlayerTotalTime;
    @BindView(R.id.imageViewPausePlay)
    ImageView imageViewPausePlay;
    boolean isPlay=false;
    @BindDrawable(R.drawable.ic_player_pause_button)
     Drawable pauseDrawable;
    @BindDrawable(R.drawable.ic_player_play_button)
     Drawable playDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        presenter = new MapPresenter(getActivity(), PermissionUtils.createRequester(this,
                REQUEST_LOCATION_PERMISSIONS));
        presenter.attach(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_new, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initViews();
        return rootView;
    }

    private void initViews() {
        //Получаем размеры устройства
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);
        wDev = metricsB.widthPixels;
        hDev = metricsB.heightPixels;
        //шторка 100%
//     DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) this.getView().getLayoutParams();
//        lp.width =(int)(wDev *1.0);
//       lp.height =hDev;

        //Размер Добавить друзей 10%
        LinearLayout.LayoutParams lpFriend = (LinearLayout.LayoutParams) layoutFriend.getLayoutParams();
        lpFriend.height = (int) (hDev * friendPersent); //10%
        //Добавить отступ для статусбар
//        if(android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.LOLLIPOP)
//
//        {
//            int hStatusBar = 0;
//            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//            if (resourceId > 0) {
//                hStatusBar = getResources().getDimensionPixelSize(resourceId);
//                lpFriend.setMargins(0, hStatusBar, 0, 0);
//            }
//        }

        //Размер кнопок для вариантов прослушивани
        llsetH(textViewPlayAgain, (int) (hDev * buttonPlayerAdv));

        llsetH(textViewPlayAbout, (int) (hDev * buttonPlayerAdv));

        llsetH(textViewNotPause, (int) (hDev * buttonPlayerAdv));

        //Размер TrackInfo
        LinearLayout.LayoutParams lpTrackFullInfo = (LinearLayout.LayoutParams) layoutTrackFullInfo.getLayoutParams();
        int sizeTrackInfo = Math.min(wDev, hDev) / 2;
        lpTrackFullInfo.height = sizeTrackInfo;
        lpTrackFullInfo.width = sizeTrackInfo;
        Drawable bgTracInfo = getResources().getDrawable(R.drawable.shape);
        bgTracInfo.setColorFilter(

                getResources().

                        getColor(R.color.colorBgTrackInfo1), PorterDuff.Mode.OVERLAY);
        layoutTrackFullInfo.setBackground(bgTracInfo);

        //Поднимаем текст TrackInfo  на уровень стрелок
        llsetH(spaceMidle, (int) (wDev / 4 -

                getResources().

                        getDimensionPixelSize(R.dimen.nv_track_info_text_size) * 2 / 3));
        //Бегущая строка
        textViewPlayerTrackInfo.setSelected(true);
        seekBarPlayer.setOnSeekBarChangeListener(this);
    }

    private void llsetH(View v, int h) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.height = h;
    }

    private void llsetW(View v, int w) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.width = w;
    }

    private void llsetH(View v, int h, int w) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.height = h;
        lp.width = w;
    }

    public void setLocationUpdateIntervalSec(int number) {
    }

    public void setPoiMaxRadius(int poiMaxRadius) {

    }

    public void updateUiOnResetTracking() {
    }

    public void setPoiMinRadius(int radius) {
    }

    public void setNextAudioTrackInfo(String trackName, int trackSequence, int totalTrackCount) {
        textViewPlayerTrackInfo.setText(trackName);

    }

    //    public void setNextAudioTrackInfo(String trackName, int trackIndex, int totalTrackCount) {
//        audioTrackName.setText(trackName);
//        audioTrackCount.setText(String.format(Locale.getDefault(), "%d/%d", trackIndex,
//                totalTrackCount));
//        playerControls.setVisibility(View.VISIBLE);
//    }
    public void hidePlayerControls() {
    }

    public void setPoi(@NonNull String poiDesc) {
        textViewTrackInfo.setText(poiDesc);

    }

    //    public void setPoi(@NonNull String poiDesc) {
//        poiName.setText(poiDesc);
//    }
    public void setCoordinates(double longitude, double latitude) {
    }

    public void clearAudioTrackInfo() {
    }

    public void showPlayControl() {
        imageViewPausePlay.setImageDrawable(playDrawable);
    }

    public void showPauseControl() {
        imageViewPausePlay.setImageDrawable(pauseDrawable);
    }

    public void updateUiOnStartTracking() {       //найден Poi начали воспроизводить
        textViewTrackInfo.setText(R.string.label_location_searching);
        textViewPlayerTrackInfo.setText(R.string.label_location_searching);

    }
    public void updateUiOnStopTracking() {
    }

    public void updateAudioTrackDurations(@NonNull String totalDurationAsStr,
                                          @NonNull String currentDurationAsStr) {
        textViewPlayerCurrentTime.setText(currentDurationAsStr);
        textViewPlayerTotalTime.setText(totalDurationAsStr);
     }

    public void updateAudioTrackDurationsSeekBar(int position){
        seekBarPlayer.setProgress(position+1);
    }

    @OnClick(R.id.fabNext)
    public void poiNextClicked() {
        presenter.playOrPauseTrack();
    }

    @OnClick(R.id.imageViewPausePlay)
    public void playPauseClicked() {
    presenter.playOrPauseTrack();
     }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(isSeekBarTouch) presenter.playTrackToPosition(i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouch=true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekBarTouch=false;

    }
//    public void onPoiMinRadiusClicked() {
//        presenter.onPoiMinRadiusClicked();
//    }

//    public void updateAudioTrackDurations(@NonNull String totalDurationAsStr,
//                                          @NonNull String currentDurationAsStr) {
//        audioTotalDuration.setText(totalDurationAsStr);
//        audioCurrentDuration.setText(currentDurationAsStr);
//    }
//

    //    private static final int REQUEST_STORY_PICKER = 0;
//    private static final int REQUEST_LOCATION_PERMISSIONS = 100;
//
//    @IntDef({
//            LocationNameState.NOT_AVAILABLE,
//            LocationNameState.SEARCHING,
//            LocationNameState.STOPPED_SEARCHING})
//    private @interface LocationNameState {
//        int NOT_AVAILABLE = 0;
//        int SEARCHING = 1;
//        int STOPPED_SEARCHING = 2;
//    }
//
//    private MapPresenter presenter;
//    private Unbinder unbinder;
//
//    @BindView(R.id.longitude) TextView longitude;
//    @BindView(R.id.latitude) TextView latitude;
//    @BindView(R.id.poi_name) TextView poiName;
//    @BindView(R.id.audio_track_name) TextView audioTrackName;
//    @BindView(R.id.audio_tracks_count) TextView audioTrackCount;
//    @BindView(R.id.audio_current_duration) TextView audioCurrentDuration;
//    @BindView(R.id.audio_total_duration) TextView audioTotalDuration;
//    @BindView(R.id.btn_start) Button startButton;
//    @BindView(R.id.btn_stop) Button stopButton;
//    @BindView(R.id.btn_reset) Button resetButton;
//    @BindView(R.id.location_update_interval) TextView locationUpdateInterval;
//    @BindView(R.id.player_controls) ViewGroup playerControls;
//    @BindView(R.id.btn_play_pause_track) ImageButton playPauseAudioTrackButton;
//    @BindView(R.id.btn_next_track) ImageButton nextAudioTrackButton;
//    @BindView(R.id.btn_stop_track) ImageButton stopAudioTrackButton;
//    @BindView(R.id.story_file) TextView storyFile;
//    @BindView(R.id.poi_min_radius) TextView poiMinRadius;
//    @BindView(R.id.poi_max_radius) TextView poiMaxRadius;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//        presenter = new MapPresenter(getActivity(), PermissionUtils.createRequester(this,
//                REQUEST_LOCATION_PERMISSIONS));
//        presenter.attach(this);
//    }
//
//    @Override
//    public void onDetach() {
//        presenter.detach();
//        super.onDetach();
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater,
//                             ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        unbinder = ButterKnife.bind(this, rootView);
//        initViews();
//        return rootView;
//    }
//
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
        tintMenuItem(menu.findItem(R.id.choose_story_file));
        tintMenuItem(menu.findItem(R.id.clear_story_file));
    }

    private void tintMenuItem(MenuItem menuItem) {
        Drawable icon = menuItem.getIcon();
        icon = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(icon, ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        menuItem.setIcon(icon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_story_file:
                onChooseStoryFile();
                return true;
            case R.id.clear_story_file:
                onClearStoryFile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_LOCATION_PERMISSIONS:
//                if (PermissionUtils.getGrantResult(grantResults) == PERMISSION_GRANTED) {
//                    presenter.startTracking();
//                } else {
//                    getActivity().finish();
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//                break;
//        }
//    }
//
//    private void initViews() {
//        locationUpdateInterval.setText(String.valueOf(presenter.getLocationUpdateIntervalSec()));
//        poiMinRadius.setText(String.valueOf(PoiProviderConfig.DEFAULT_MIN_RADIUS_METERS));
//        poiMaxRadius.setText(String.valueOf(PoiProviderConfig.DEFAULT_MAX_RADIUS_METERS));
//        nextAudioTrackButton.setClickable(false);
//        nextAudioTrackButton.setEnabled(false);
//        stopAudioTrackButton.setClickable(false);
//        stopAudioTrackButton.setEnabled(false);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        unbinder.unbind();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        presenter.onDestroy();
//    }
//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w(getClass().getSimpleName(), "onActivityResult");
        if (requestCode == REQUEST_STORY_PICKER && resultCode == Activity.RESULT_OK) {
            Uri storyFileUri = data.getData();
            presenter.setStoryFileUri(storyFileUri);
            // печать названия файла
            //       storyFile.setText(storyFileUri.toString());

            presenter.startTracking();
        }
    }

    //
//    @OnClick(R.id.poi_min_radius)
//    public void onPoiMinRadiusClicked() {
//        presenter.onPoiMinRadiusClicked();
//    }
//
//    @OnClick(R.id.poi_max_radius)
//    public void onPoiMaxRadiusClicked() {
//        presenter.onPoiMaxRadiusClicked();
//    }
//
//    @OnClick(R.id.location_update_interval)
//    public void onLocationUpdateIntervalClicked() {
//        presenter.onLocationUpdateIntervalClicked();
//    }
//
//    @OnClick(R.id.btn_start)
//    public void onStartClicked() {
//        presenter.startTracking();
//    }
//
//    @OnClick(R.id.btn_stop)
//    public void onStopClicked() {
//        presenter.stopTracking();
//    }
//
//    @OnClick(R.id.btn_reset)
//    public void onResetClicked() {
//        presenter.resetTracking();
//    }
//
//    @OnClick(R.id.btn_play_pause_track)
//    public void onPlayPauseTrackClicked() {
//        playPauseAudioTrackButton.setClickable(false);
//        presenter.playOrPauseTrack();
//    }
//
//    @OnClick(R.id.btn_next_track)
//    public void onNextTrackClicked() {
//        presenter.playNextTrack();
//    }
//
//    @OnClick(R.id.btn_stop_track)
//    public void onStopTrackClicked() {
//        presenter.stopPlay();
//    }
//
    private void onChooseStoryFile() {
        Intent fpIntent = new Intent(getActivity(), StoryFilePickerActivity.class)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        startActivityForResult(fpIntent, REQUEST_STORY_PICKER);
    }

    private void onClearStoryFile() {
        presenter.setStoryFileUri(null);
        //storyFile.setText(R.string.empty_story_file);
    }



//

//
//    public void updateUiOnStopTracking() {
//        poiName.setText(R.string.label_location_stopped_searching);
//        updateButtonBar(LocationNameState.STOPPED_SEARCHING);
//    }
//
//    public void updateUiOnResetTracking() {
//        poiName.setText(R.string.label_not_available);
//        longitude.setText(R.string.label_not_available);
//        latitude.setText(R.string.label_not_available);
//        updateButtonBar(LocationNameState.NOT_AVAILABLE);
//    }
//

//
//    public void setCoordinates(double longitude, double latitude) {
//        this.longitude.setText(String.valueOf(longitude));
//        this.latitude.setText(String.valueOf(latitude));
//    }
//

//
//    public void clearAudioTrackInfo() {
//        audioTrackName.setText("");
//        audioTrackCount.setText("");
//        audioTotalDuration.setText("");
//        audioCurrentDuration.setText("");
//    }
//

//    public void setLocationUpdateIntervalSec(int intervalSec) {
//        locationUpdateInterval.setText(String.valueOf(intervalSec));
//    }
//
//    public void setPoiMinRadius(int radiusInMeters) {
//        poiMinRadius.setText(String.valueOf(radiusInMeters));
//    }
//
//    public void setPoiMaxRadius(int radiusInMeters) {
//        poiMaxRadius.setText(String.valueOf(radiusInMeters));
//    }
//
//    private void updateButtonBar(@LocationNameState int state) {
//        startButton.setEnabled(state != LocationNameState.SEARCHING);
//        stopButton.setEnabled(state == LocationNameState.SEARCHING);
//        resetButton.setEnabled(state == LocationNameState.STOPPED_SEARCHING);
//    }
//
//    public void hidePlayerControls() {
//        playerControls.setVisibility(View.GONE);
//    }
//
//    public void showPlayControl() {
//        playPauseAudioTrackButton.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_play));
//        playPauseAudioTrackButton.setClickable(true);
//    }
//
//    public void showPauseControl() {
//        playPauseAudioTrackButton.setImageDrawable(getResources().getDrawable(R.drawable.player_btn_pause));
//        playPauseAudioTrackButton.setClickable(true);
//    }
}