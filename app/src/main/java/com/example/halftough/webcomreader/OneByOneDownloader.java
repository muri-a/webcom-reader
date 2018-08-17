package com.example.halftough.webcomreader;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Class preventing app from asking server for huge number of calls at once
public abstract class OneByOneDownloader<T, K> {
    int free;
    List<Call<T>> calls;
    List<K> extra;

    public OneByOneDownloader(List<Call<T>> calls){
        this(calls,1);
    }

    public OneByOneDownloader(List<Call<T>> calls, int slots){
        this(calls, null, slots);
    }

    public OneByOneDownloader(List<Call<T>> calls, List<K> extra){
        this(calls, extra, 1);
    }

    public OneByOneDownloader(List<Call<T>> calls, List<K> extra, int slots){
        free = slots;
        this.calls = calls;
        this.extra = extra;
    }

    private void markDone(){
        if(calls.size()>0) {
            calls.get(0).enqueue(new Callback<T>() {
                K ex = extra!=null?extra.get(0):null;
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    OneByOneDownloader.this.onResponse(call, response, ex);
                    markDone();
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    markDone();
                }
            });
            calls.remove(0);
            if(extra!=null)
                extra.remove(0);
        }
    }

    public abstract void onResponse(Call<T> call, Response<T> response, K extra);

    public void download() {
        for (int i = 0; i < free && i < calls.size(); i++) {
            Call call = calls.get(0);
            final K ex = extra!=null?extra.get(0):null;
            calls.remove(0);
            if(extra!=null)
                extra.remove(0);

            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    OneByOneDownloader.this.onResponse(call, response, ex);
                    markDone();
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    markDone();
                }
            });
        }
    }

}
