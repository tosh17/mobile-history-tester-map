package ru.mhistory.screen.main.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import ru.mhistory.R;

public class NumberPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private static final String NUMBER = "number";

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnNumberSetListener {
        /**
         * @param number The number that was set.
         */
        void onNumberSet(int number);
    }

    private final NumberPicker numberPicker;
    private final OnNumberSetListener mCallback;

    /**
     * @param context  Parent.
     * @param callBack How parent is notified.
     * @param number   The initial number.
     */
    public NumberPickerDialog(Context context,
                              OnNumberSetListener callBack,
                              int number,
                              int rangeMin,
                              int rangeMax,
                              int title) {
        this(context, 0, callBack, number, rangeMin, rangeMax, title);
    }

    /**
     * @param context  Parent.
     * @param theme    the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param number   The initial number.
     */
    public NumberPickerDialog(Context context,
                              int theme,
                              OnNumberSetListener callBack,
                              int number,
                              int rangeMin,
                              int rangeMax,
                              int title) {
        super(context, theme);
        mCallback = callBack;
        setTitle(title);
        setButton(DialogInterface.BUTTON_POSITIVE, context.getText(R.string.ok), this);
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(R.string.cancel),
                (OnClickListener) null);
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);
        setView(view);
        numberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        numberPicker.setMinValue(rangeMin);
        numberPicker.setMaxValue(rangeMax);
        numberPicker.setValue(number);
        numberPicker.setOnLongPressUpdateInterval(100); // make the repeat rate three times as fast
        // as normal since the range is so large.
        numberPicker.setWrapSelectorWheel(false);       // don't wrap from min->max
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            numberPicker.clearFocus();
            mCallback.onNumberSet(numberPicker.getValue());
            dialog.dismiss();
        }
    }

    public void setDisplayedValues(@NonNull String[] values) {
        numberPicker.setDisplayedValues(values);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(NUMBER, numberPicker.getValue());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int number = savedInstanceState.getInt(NUMBER);
        numberPicker.setValue(number);
    }
}
