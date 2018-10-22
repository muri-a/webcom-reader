package com.example.halftough.webcomreader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Class preventing app from asking server for huge number of calls at once
public abstract class OneByOneCallDownloader<CallClass, Extra> extends OneByOneDownloader<Call<CallClass>, Extra> {

    public OneByOneCallDownloader(){ this(new LinkedList<Call<CallClass>>(), new LinkedList<Extra>()); }

    public OneByOneCallDownloader(Queue<Call<CallClass>> calls){
        this(calls,1);
    }

    public OneByOneCallDownloader(Queue<Call<CallClass>> calls, int slots){
        this(calls, null, slots);
    }

    public OneByOneCallDownloader(Queue<Call<CallClass>> calls, Queue<Extra> extras){
        this(calls, extras, 1);
    }

    public OneByOneCallDownloader(Queue<Call<CallClass>> calls, Queue<Extra> extras, int slots){
        free = capacity = slots;
        queue = calls;
        this.extras = extras;
    }

    public abstract void onResponse(Call<CallClass> call, Response<CallClass> response, Extra extra);

    protected void downloadElement(Call<CallClass> element, final Extra extra){
        element.enqueue(new Callback<CallClass>() {
            @Override
            public void onResponse(Call<CallClass> call, Response<CallClass> response) {
                OneByOneCallDownloader.this.onResponse(call, response, extra);
                elementDownloaded(extra);
            }

            @Override
            public void onFailure(Call<CallClass> call, Throwable t) {

            }
        });
    }
}
