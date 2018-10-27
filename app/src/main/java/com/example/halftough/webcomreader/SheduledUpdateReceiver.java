package com.example.halftough.webcomreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SheduledUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateWebcomsService.updateNewChapters(context);
    }
}
