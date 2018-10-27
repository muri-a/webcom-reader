package com.example.halftough.webcomreader;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {
    private int value;
    private NumberPicker minutesPicker, hoursPicker, daysPicker;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout dialogView = new LinearLayout(getContext());
        LinearLayout layout = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.time_interval_layout, dialogView);
        minutesPicker = layout.findViewById(R.id.pickerMinutes);
        hoursPicker = layout.findViewById(R.id.pickerHours);
        daysPicker = layout.findViewById(R.id.pickerDays);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        int minutes = value;
        int days = minutes/(24*60);
        minutes -= days*24*60;
        int hours = minutes/60;
        minutes -= hours*60;

        minutesPicker.setValue(minutes);
        hoursPicker.setValue(hours);
        daysPicker.setValue(days);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            minutesPicker.clearFocus();
            int minutesValue = minutesPicker.getValue();
            int hoursValue = hoursPicker.getValue();
            int daysValue = daysPicker.getValue();
            int newValue = minutesValue+60*hoursValue+60*24*daysValue;
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 120);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(120) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(value);
    }

    public int getValue() {
        return this.value;
    }

}
