package com.example.halftough.webcomreader;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

public abstract class OnLiveDataReady<T> {
    public abstract void onReady(T value);

    public void run(final LiveData<T> data) {
        if(data.getValue() != null){
            onReady(data.getValue());
        }
        else{
            data.observeForever(new Observer<T>() {
                @Override
                public void onChanged(@Nullable T t) {
                    data.removeObserver(this);
                    onReady(t);
                }
            });
        }
    }
}
