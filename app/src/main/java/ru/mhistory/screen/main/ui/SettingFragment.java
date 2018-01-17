package ru.mhistory.screen.main.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import ru.mhistory.R;
import ru.mhistory.providers.SearchConf;

/**
 * Created by shcherbakov on 14.12.2017.
 */

public class SettingFragment extends DialogFragment {
    EditText timeUpdate;
    EditText stayRadius, zone1Radius, zone2Radius, zone3Radius;
    EditText angleZone2, angleZone3;
    EditText deltaToTracking;
    EditText angleAvg,angleAvgSpeed;
    CheckBox isStayPlay;
    SearchConf conf;
    Switch isDebag;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_settings, container, false);
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.activity_settings, null);

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.setContentView(R.layout.activity_settings);
            //      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog_theme);
        isDebag = dialog.findViewById(R.id.switchDebug);
        timeUpdate = dialog.findViewById(R.id.editTextSearchTimeUpdate);
        isStayPlay = dialog.findViewById(R.id.checkBoxIsStayPlay);
        stayRadius = dialog.findViewById(R.id.editTextStayRadius);
        zone1Radius = dialog.findViewById(R.id.editTextZona1Radius);
        zone2Radius = dialog.findViewById(R.id.editTextZona2Radius);
        zone3Radius = dialog.findViewById(R.id.editTextZona3Radius);
        angleZone2 = dialog.findViewById(R.id.editTextAngle2);
        angleZone3 = dialog.findViewById(R.id.editTextAngle3);
        angleAvg = dialog.findViewById(R.id.editTextAngleAVG);
        angleAvgSpeed = dialog.findViewById(R.id.editTextAngleAVGSpeed);
        deltaToTracking = dialog.findViewById(R.id.editTextDeltaToTraking);

        conf = SearchConf.getSearchPoiConf(getContext());

        isDebag.setChecked(conf.debug);
        isStayPlay.setChecked(conf.isStayPlay);
        timeUpdate.setText(String.valueOf(conf.searchTimeUpdate));
        deltaToTracking.setText(String.valueOf(conf.deltaDistanceToTracking));
        stayRadius.setText(String.valueOf(conf.radiusStay));
        zone1Radius.setText(String.valueOf(conf.radiusZone1));
        zone2Radius.setText(String.valueOf(conf.radiusZone2));
        zone3Radius.setText(String.valueOf(conf.radiusZone3));
        angleZone2.setText(String.valueOf(conf.deltaAngleZona2));
        angleZone3.setText(String.valueOf(conf.deltaAngleZona3));
        angleAvg.setText(String.valueOf(conf.angleAvgCount));
        angleAvgSpeed.setText(String.valueOf(conf.angleAvgSpeed));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        saveConfig();
    }

    public void saveConfig() {
        conf.debug = isDebag.isChecked();
        conf.searchTimeUpdate = editToInt(timeUpdate);
        conf.isStayPlay = isStayPlay.isChecked();
        conf.deltaDistanceToTracking = editToInt(deltaToTracking);
        conf.radiusStay = editToInt(stayRadius);
        conf.radiusZone3 = editToInt(zone3Radius);
        conf.radiusZone1 = editToInt(zone1Radius);
        conf.radiusZone2 = editToInt(zone2Radius);
        conf.deltaAngleZona2 = editToFloat(angleZone2);
        conf.deltaAngleZona3 = editToFloat(angleZone3);
        conf.angleAvgCount = editToInt(angleAvg);
        conf.angleAvgSpeed = editToInt(angleAvgSpeed);
        conf.save(getContext());
    }

    private int editToInt(EditText edit) {
        String str = edit.getText().toString();
        if (str.equals("")) return 0;
        return Integer.parseInt(str);
    }

    private float editToFloat(EditText edit) {
        String str = edit.getText().toString();
        if (str.equals("")) return 0;
        return Float.parseFloat(str);
    }
}