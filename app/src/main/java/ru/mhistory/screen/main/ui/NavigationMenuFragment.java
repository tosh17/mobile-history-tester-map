package ru.mhistory.screen.main.ui;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.Space;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.mhistory.R;
import ru.mhistory.common.util.PermissionUtils;
import ru.mhistory.screen.map.MapPresenter;

/**
 * Created by shcherbakov on 29.10.2017.
 */

public class NavigationMenuFragment extends DebugInfoFragment {

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

    @BindView(R.id.textViewPlayerTrackInfo)
    TextView textViewPlayerTrackInfo;

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
    wDev =metricsB.widthPixels;
    hDev =metricsB.heightPixels;
    //шторка 100%
//    DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) nv.getLayoutParams();
//    lp.width =(int)(wDev *1.0);
//    lp.height =hDev;

    //Размер Добавить друзей 10%
    LinearLayout.LayoutParams lpFriend = (LinearLayout.LayoutParams) layoutFriend.getLayoutParams();
    lpFriend.height =(int)(hDev *friendPersent); //10%
    //Добавить отступ для статусбар
        if(android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.LOLLIPOP)

    {
        int hStatusBar = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            hStatusBar = getResources().getDimensionPixelSize(resourceId);
            lpFriend.setMargins(0, hStatusBar, 0, 0);
        }
    }

    //Размер кнопок для вариантов прослушивани
    llsetH(textViewPlayAgain, (int) (hDev *buttonPlayerAdv));

    llsetH(textViewPlayAbout, (int) (hDev *buttonPlayerAdv));

    llsetH(textViewNotPause, (int) (hDev *buttonPlayerAdv));

    //Размер TrackInfo
    LinearLayout.LayoutParams lpTrackFullInfo = (LinearLayout.LayoutParams) layoutTrackFullInfo.getLayoutParams();
    int sizeTrackInfo = Math.min(wDev, hDev) / 2;
    lpTrackFullInfo.height =sizeTrackInfo;
    lpTrackFullInfo.width =sizeTrackInfo;
    Drawable bgTracInfo = getResources().getDrawable(R.drawable.shape);
        bgTracInfo.setColorFilter(

    getResources().

    getColor(R.color.colorBgTrackInfo1), PorterDuff.Mode.OVERLAY);
        layoutTrackFullInfo.setBackground(bgTracInfo);

    //Поднимаем текст TrackInfo  на уровень стрелок
    llsetH(spaceMidle, (int) (wDev /4-

    getResources().

    getDimensionPixelSize(R.dimen.nv_track_info_text_size) *2/3));
    //Бегущая строка
        textViewPlayerTrackInfo.setSelected(true);
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

}
