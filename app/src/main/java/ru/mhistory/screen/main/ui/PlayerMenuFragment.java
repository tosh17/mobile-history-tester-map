package ru.mhistory.screen.main.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.yandex.metrica.YandexMetrica;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.mhistory.R;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.bus.event.AppPrepareBDCompleteEvent;
import ru.mhistory.common.util.PermissionUtils;
import ru.mhistory.fileservice.FileService;
import ru.mhistory.playback.AudioService;
import ru.mhistory.realm.RealmFactory;
import ru.mhistory.screen.map.MapPresenter;

public class PlayerMenuFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {


    private static final int REQUEST_STORY_PICKER = 0;
    private static final int REQUEST_LOCATION_PERMISSIONS = 100;
    int wDev, hDev;
    float friendPersent = 0.10f;


    private MapPresenter presenter;
    private Unbinder unbinder;

    @BindView(R.id.menuLinerFriend)
    LinearLayout layoutFriend;
    @BindView(R.id.menuLinerADW)
    LinearLayout layoutAWD;
    @BindView(R.id.menuLinerPlayerAdv)
    LinearLayout layoutPlayerAdv;
    @BindView(R.id.lPlayer)
    LinearLayout layoutPlayer;

    //Refresh button
    @BindView(R.id.RefreshButton)
    LinearLayout buttonRefresh;
    @BindView(R.id.RefreshOn)
    ImageView buttonRefreshOn;
    boolean isOnButtonRefresh = true;

    @BindView(R.id.listenPoiButton)
    LinearLayout buttonListenPoi;
    @BindView(R.id.listenPoiOn)
    ImageView buttonListenPoiOn;
    boolean isOnButtonListenPoi = false;


    //CATEGORY
    @BindView(R.id.bgCategoryNature)
    LinearLayout bgCategoryNature;
    @BindView(R.id.icCategoryNature)
    ImageView icCategoryNature;
    @BindView(R.id.txtCategoryNature)
    TextView txtCategoryNature;
    private boolean isCategoryNaturePress = true;

    @BindView(R.id.bgCategoryHistory)
    LinearLayout bgCategoryHistory;
    @BindView(R.id.icCategoryHistory)
    ImageView icCategoryHistory;
    @BindView(R.id.txtCategoryHistory)
    TextView txtCategoryHistory;
    private boolean isCategoryHistoryPress = true;

    @BindView(R.id.bgCategoryPeople)
    LinearLayout bgCategoryPeople;
    @BindView(R.id.icCategoryPeople)
    ImageView icCategoryPeople;
    @BindView(R.id.txtCategoryPeople)
    TextView txtCategoryPeople;
    private boolean isCategoryPeoplePress = true;

    @BindView(R.id.bgCategoryCulture)
    LinearLayout bgCategoryCulture;
    @BindView(R.id.icCategoryCulture)
    ImageView icCategoryCulture;
    @BindView(R.id.txtCategoryCulture)
    TextView txtCategoryCulture;
    private boolean isCategoryCulturePress = true;

    //PlayerControl
    @BindView(R.id.imageViewPausePlay)
    ImageView imageViewPausePlay;
    boolean isPlay = false;
    @BindDrawable(R.drawable.ic_player_controll_pause_button)
    Drawable pauseDrawable;
    @BindDrawable(R.drawable.ic_player_controll_play_button)
    Drawable playDrawable;
    @BindDrawable(R.drawable.ic_player_poi_info_shape)
    Drawable poiInfoShape;
    @BindView(R.id.imageViewRew)
    ImageView icButtonNextTrack;
    @BindView(R.id.buttonNextTrack)
    View buttonNextTrack;
    @BindView(R.id.imageViewFF)
    ImageView icButtonPrevTrack;
    @BindView(R.id.buttonPrevTrack)
    View buttonPrevTrack;

    @BindView(R.id.textViewPlayerTrackInfo)
    TextView textViewPlayerTrackInfo;
    @BindView(R.id.seekBarPlayer)
    SeekBar seekBarPlayer;
    private boolean isSeekBarTouch = false;
    @BindView(R.id.textViewPlayerCurrentTime)
    TextView textViewPlayerCurrentTime;
    @BindView(R.id.textViewPlayerTotalTime)
    TextView textViewPlayerTotalTime;

    //info panel
    @BindView(R.id.layoutPoiInfo)
    LinearLayout layoutPoiInfo;
    @BindView(R.id.layoutCategory)
    LinearLayout layoutCategory;

    @BindView(R.id.lTrackInfo)
    LinearLayout layoutTrackFullInfo;
    @BindView(R.id.textViewTrackInfo)
    TextView textViewTrackInfo;
    @BindView(R.id.textViewTrackInfoCount)
    TextView textViewTrackInfoCount;
    @BindView(R.id.textViewTrackInfoDistance)
    TextView textViewTrackInfoDistance;

    //test panel
    @BindView(R.id.textTest)
    TextView textTest;
    @BindView(R.id.scrollTest)
    ScrollView scrollTest;
    public boolean isDebug = false;

    public void setTest(String s) {
        textTest.setText("Max distance to stay" + (long) presenter.maxDistance + "\n");
        textTest.append(Html.fromHtml(s));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        presenter = new MapPresenter(getActivity(), PermissionUtils.createRequester(this,
                REQUEST_LOCATION_PERMISSIONS));
        presenter.attach(this);
        getActivity().startService(new Intent(getActivity(), AudioService.class)
                .setAction(AudioService.Action.INIT));
        if (!RealmFactory.getInstance(getContext()).isCreate()) {
            Intent intent = new Intent(getContext(), FileService.class).setAction(FileService.Action.LOAD_ALL_BD);
            getActivity().startService(intent);
        } else
            BusProvider.getInstance().post(new AppPrepareBDCompleteEvent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu_player, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initViews();
        if (isDebug) debugShow(isDebug);
        return rootView;
    }

    private void initViews() {
        //Получаем размеры устройства
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);
        wDev = metricsB.widthPixels;
        hDev = metricsB.heightPixels;
        //Размер Добавить друзей 10%
        ConstraintLayout.LayoutParams lpFriend = (ConstraintLayout.LayoutParams) layoutFriend.getLayoutParams();
        lpFriend.height = (int) (hDev * friendPersent); //10%

        ConstraintLayout.LayoutParams lpCategory = (ConstraintLayout.LayoutParams) layoutCategory.getLayoutParams();
        lpCategory.width = (int) (wDev);
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

        //Размер TrackInfo
        LinearLayout.LayoutParams lpTrackFullInfo = (LinearLayout.LayoutParams) layoutTrackFullInfo.getLayoutParams();
        int sizeTrackInfo = Math.min(wDev, hDev) / 2;
        lpTrackFullInfo.height = sizeTrackInfo;
        lpTrackFullInfo.width = sizeTrackInfo;
        poiInfoShape.setColorFilter(
                getResources().
                        getColor(R.color.colorBgTrackInfo3), PorterDuff.Mode.OVERLAY);
        layoutTrackFullInfo.setBackground(poiInfoShape);
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

    public void updateUiOnResetTracking() {
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

    public void setPoi(@NonNull String poiDesc, int size, float distanceTo) {
        textViewTrackInfo.setText(poiDesc);
        //todo hardcode
        textViewTrackInfoCount.setText(size + " треков");
        textViewTrackInfoDistance.setText((int) distanceTo + "м до объекта");

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

    public void updateAudioTrackDurationsSeekBar(int position) {
        seekBarPlayer.setProgress(position + 1);
    }


    @OnClick(R.id.RefreshButton)
    public void refreshButtonClicked() {
        isOnButtonRefresh = !isOnButtonRefresh;
        buttonRefreshOn.setVisibility(booleanToVisible(isOnButtonRefresh));

        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
        ad.setMessage(getString(R.string.mp_warning_history_reset)); // сообщение
        ad.setPositiveButton(getString(R.string.mp_dialog_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presenter.clearHistory();
            }
        }).setNegativeButton(getString(R.string.mp_dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        ad.show();


    }

    @OnClick(R.id.listenPoiButton)
    public void listenPoiButtonClicked() {
        isOnButtonListenPoi = !isOnButtonListenPoi;
        buttonListenPoiOn.setVisibility(booleanToVisible(isOnButtonListenPoi));
        layoutPoiInfo.setVisibility(booleanToVisible(!isOnButtonListenPoi));
        layoutCategory.setVisibility(booleanToVisible(isOnButtonListenPoi));

    }

    private int booleanToVisible(boolean isVisible) {
        return isVisible ? View.VISIBLE : View.INVISIBLE;
    }


    // Category
    public void categoryClick(boolean isCategoryPress, LinearLayout bgCategory, ImageView icCategory, TextView txtCategory, int color) {
        if (isCategoryPress) {
            bgCategory.setBackgroundColor(getResources().getColor(R.color.colorPlayerCategoryUnCheck));
            icCategory.setColorFilter(
                    Color.BLACK, PorterDuff.Mode.MULTIPLY);
            txtCategory.setTextColor(Color.BLACK);
        } else {
            bgCategory.setBackgroundColor(color);
            icCategory.setColorFilter(
                    Color.WHITE, PorterDuff.Mode.MULTIPLY);
            txtCategory.setTextColor(Color.WHITE);
        }


    }

    @OnClick(R.id.btnCategoryNature)
    public void categoryNatureClick() {
        categoryClick(isCategoryNaturePress, bgCategoryNature, icCategoryNature, txtCategoryNature, getResources().getColor(R.color.colorPlayerCategoryNature));
        isCategoryNaturePress = !isCategoryNaturePress;

    }

    @OnClick(R.id.btnCategoryHistory)
    public void categoryHistoryClick() {
        categoryClick(isCategoryHistoryPress, bgCategoryHistory, icCategoryHistory, txtCategoryHistory, getResources().getColor(R.color.colorPlayerCategoryHistory));
        isCategoryHistoryPress = !isCategoryHistoryPress;
    }

    @OnClick(R.id.btnCategoryPeople)
    public void categoryPeopleClick() {
        categoryClick(isCategoryPeoplePress, bgCategoryPeople, icCategoryPeople, txtCategoryPeople, getResources().getColor(R.color.colorPlayerCategoryPeople));
        isCategoryPeoplePress = !isCategoryPeoplePress;
    }

    @OnClick(R.id.btnCategoryCulture)
    public void categoryCultureClick() {
        categoryClick(isCategoryCulturePress, bgCategoryCulture, icCategoryCulture, txtCategoryCulture, getResources().getColor(R.color.colorPlayerCategoryCulture));
        isCategoryCulturePress = !isCategoryCulturePress;
    }

    @OnClick(R.id.btnCategoryALL)
    public void categoryALLClick() {
        if (!isCategoryNaturePress) categoryNatureClick();
        if (!isCategoryHistoryPress) categoryHistoryClick();
        if (!isCategoryPeoplePress) categoryPeopleClick();
        if (!isCategoryCulturePress) categoryCultureClick();
        isCategoryNaturePress = true;
        isCategoryHistoryPress = true;
        isCategoryPeoplePress = true;
        isCategoryCulturePress = true;
    }


    //Player Control
    @OnClick(R.id.buttonPausePlay)
    public void playPauseClicked() {
        presenter.playOrPauseTrack();
        String eventParameters = "{\"name\":\"PlayPause\", \"location\":\"player\"}";
        YandexMetrica.reportEvent("Button press", eventParameters);
    }

    @OnClick(R.id.buttonPrevTrack)
    public void prevTrackClicked() {
        presenter.playPrevTrack();
        String eventParameters = "{\"name\":\"PrevTrack\", \"location\":\"player\"}";
        YandexMetrica.reportEvent("Button press", eventParameters);
    }

    @OnClick(R.id.buttonNextTrack)
    public void nextTrackClicked() {
        presenter.playNextTrack();
        String eventParameters = "{\"name\":\"NextTrack\", \"location\":\"player\"}";
        YandexMetrica.reportEvent("Button press", eventParameters);
    }

    @OnClick(R.id.mpButtonPrevTrack)
    public void prevPoiClicked() {
        presenter.playPrevPoi();
    }

    @OnClick(R.id.mpButtonNextTrack)
    public void nextPoiClicked() {
        presenter.playNextPoi();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (isSeekBarTouch) presenter.playTrackToPosition(i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekBarTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekBarTouch = false;
    }


    public void debugShow(boolean isDebag) {
        int isVisible = booleanToVisible(!isDebag);
        layoutFriend.setVisibility(isVisible);
        layoutCategory.setVisibility(isVisible);
        layoutPoiInfo.setVisibility(isVisible);
        layoutAWD.setVisibility(isVisible);
        layoutPlayer.setVisibility(isVisible);
        layoutPlayerAdv.setVisibility(isVisible);
        scrollTest.setVisibility(isDebag ? View.VISIBLE : View.GONE);
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
        //tintMenuItem(menu.findItem(R.id.choose_story_file));
        //tintMenuItem(menu.findItem(R.id.clear_story_file));
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
//            case R.id.choose_story_file:
//                onChooseStoryFile();
//                return true;
//            case R.id.clear_story_file:
//                onClearStoryFile();
//                return true;

        }
        SettingFragment d = new SettingFragment();
        // d.setTargetFragment(this,5);
        d.show(getFragmentManager(), "");
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
            // presenter.setStoryFileUri(storyFileUri);
            // печать названия файла
            //       storyFile.setText(storyFileUri.toString());
            //Todo выбор файла json
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

    public void nextTrackClickEnabled(boolean enable) {
        buttonNextTrack.setEnabled(enable);
        if (!enable)
            icButtonNextTrack.setColorFilter(getResources().getColor(R.color.mp_player_disable_btn), PorterDuff.Mode.SRC_ATOP);
        else
            icButtonNextTrack.setColorFilter(getResources().getColor(R.color.mp_player_enable_btn), PorterDuff.Mode.SRC_ATOP);

    }

    public void prevTrackClickEnabled(boolean enable) {
        buttonPrevTrack.setEnabled(enable);
        if (!enable)
            icButtonPrevTrack.setColorFilter(getResources().getColor(R.color.mp_player_disable_btn), PorterDuff.Mode.SRC_ATOP);
        else
            icButtonPrevTrack.setColorFilter(getResources().getColor(R.color.mp_player_enable_btn), PorterDuff.Mode.SRC_ATOP);
    }
}