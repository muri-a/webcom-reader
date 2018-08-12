package com.example.halftough.webcomreader;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Class preventing app from asking server for huge number of calls at once
public abstract class OneByOneDownloader<T> {
    int free;
    List<Call<T>> calls;

    public OneByOneDownloader(List<Call<T>> calls){
        this(calls,1);
    }

    public OneByOneDownloader(List<Call<T>> calls, int slots){
        free = slots;
        this.calls = calls;
    }

    private void markDone(){
        if(calls.size()>0) {
            calls.get(0).enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    onMyResponse(call, response);
                    markDone();
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    markDone();
                }
            });
            calls.remove(0);
        }
    }

    public abstract void onMyResponse(Call<T> call, Response<T> response);

    public void download() {
        for (int i = 0; i < free && i < calls.size(); i++) {
            Call call = calls.get(0);
            calls.remove(0);
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    onMyResponse(call, response);
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
