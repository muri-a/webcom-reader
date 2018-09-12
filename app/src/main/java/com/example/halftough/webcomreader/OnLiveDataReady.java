package com.example.halftough.webcomreader;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public abstract class OnLiveDataReady {
    public enum WaitUntil { NOT_NULL, CHANGED }
    public abstract void onReady();
    private boolean over = false;

    public void observe(LiveData liveData){
        observe(liveData, WaitUntil.NOT_NULL);
    }

    public void observe(LiveData liveData, WaitUntil waitUntil){
        List<LiveData> list = new ArrayList<>();
        list.add(liveData);
        observe(list, waitUntil);
    }

    public void observe(LiveData data1, LiveData data2){ observe(data1, data2, WaitUntil.NOT_NULL); }

    public void observe(LiveData data1, LiveData data2, final WaitUntil waitUntil){
        ArrayList<LiveData> list = new ArrayList<>();
        list.add(data1);
        list.add(data2);
        observe(list, waitUntil);
    }

    public void observe(Collection<LiveData> collection){
        observe(collection, WaitUntil.NOT_NULL);
    }

    //WaitUntil NOT_NULL, run method as soon as can, CHANGED wait until every value was updated. Updating to same value still counts
    public void observe(final Collection<LiveData> collection, final WaitUntil waitUntil){
        final Map<LiveData,Boolean> changed = new Hashtable<>();
        final Map<LiveData, Boolean> skipFirst = new Hashtable<>();
        if(waitUntil == WaitUntil.CHANGED){
            for(LiveData i : collection) {
                changed.put(i, false);
                skipFirst.put(i, i.getValue()!=null);
            }
        }
        for(final LiveData liveData : collection){
            liveData.observeForever(new Observer() {
                LiveData data = liveData;
                @Override
                public void onChanged(@Nullable Object o) {
                    if(waitUntil == WaitUntil.CHANGED && skipFirst.get(data)) {
                        skipFirst.put(data, false);
                        return;
                    }
                    data.removeObserver(this);
                    if(waitUntil == WaitUntil.CHANGED){
                        changed.put(data, true);
                    }
                    for(LiveData i : collection){
                        if(i.getValue() == null)
                            return;
                        if(waitUntil == WaitUntil.CHANGED && !changed.get(i))
                            return;
                    }
                    if(!over) {
                        over = true;
                        onReady();
                    }
                }
            });
        }
    }
}
