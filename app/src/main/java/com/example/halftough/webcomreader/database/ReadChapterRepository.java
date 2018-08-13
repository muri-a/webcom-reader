package com.example.halftough.webcomreader.database;

import android.content.Context;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.example.halftough.webcomreader.webcoms.ComicPage;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadChapterRepository {
    Webcom webcom;
    Context context;
    public ReadChapterRepository(Context context, Webcom webcom) {
        this.context = context;
        this.webcom = webcom;
    }

    public void getImageFor(String number, final ImageView readChapterImage) {
        Call<ComicPage> call = webcom.getPageCall(number);
        call.enqueue(new Callback<ComicPage>() {
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response) {

                Picasso.get().load(response.body().getImg()).fit().centerInside().into(readChapterImage);
            }

            @Override
            public void onFailure(Call<ComicPage> call, Throwable t) {

            }
        });

    }
}
