package ru.mhistory.bus.event;

import ru.mhistory.R;
import ru.mhistory.bus.BusProvider;
import ru.mhistory.common.util.ThreadUtil;

/**
 * Created by shcherbakov on 27.01.2018.
 */

public class InitStatus {
    public static int InitState = 1;
    public static int FileLoadStart = 2;
    public static int FileLoadStop = 3;
    public static int ZipStart = 4;
    public static int ZipStop = 5;
    public static int JsonStart = 6;
    public static int JsonStop = 7;
    public static int BdStart = 8;
    public static int BdStop = 9;
    public static int BdLoadStart = 10;
    public static int BdLoadStop = 11;
    public static int Finish = 50;

    public int resId;
    public boolean isInit = false;
    public int progress = 0;

    public InitStatus(int res) {
        resId = getRes(res);
    }

    public InitStatus(int res, int p) {
        resId = getRes(res);
        progress += p * 55 / 100;
    }

    private int getRes(int res) {
        switch (res) {
            case 1:
                progress = 0;
                return R.string.map_init_state_init;
            case 2:
                progress = 0;
                return R.string.map_init_state_ftp_start;
            case 3:
                progress = 20;
                return R.string.map_init_state_ftp_stop;
            case 4:
                progress = 20;
                return R.string.map_init_state_zip_start;
            case 5:
                progress = 24;
                return R.string.map_init_state_zip_stop;
            case 6:
                progress = 25;
                return R.string.map_init_state_json_start;
            case 7:
                progress = 79;
                return R.string.map_init_state_json_stop;
            case 8:
                progress = 80;
                return R.string.map_init_state_bd_start;
            case 9:
                progress = 89;
                return R.string.map_init_state_bd_stop;
            case 10:
                progress = 94;
                return R.string.map_init_state_bd_load_start;
            case 11:
                progress = 99;
                return R.string.map_init_state_bd_load_stop;
            case 50:
                progress = 100;
                isInit = true;
        }
        return R.string.map_init_state_init;
    }

    public static void send(int res) {
        ThreadUtil.runOnUiThread(() -> BusProvider.getInstance().post(new InitStatus(res)));
    }

    public static void send(int res, int progress) {
        ThreadUtil.runOnUiThread(() -> BusProvider.getInstance().post(new InitStatus(res, progress)));
    }
}
