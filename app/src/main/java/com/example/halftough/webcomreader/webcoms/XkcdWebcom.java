package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.database.Chapter;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class XkcdWebcom extends Webcom {
    private static int pageCount = -1;

    public XkcdWebcom(){
        id = "xkcd";
        title = "xkcd";
        description = "Webcomic created by American author Randall Munroe. The comic's tagline describes it as \"A webcomic of romance, sarcasm, math, and language\". Munroe states on the comic's website that the name of the comic is not an initialism but \"just a word with no phonetic pronunciation\".\n" +
                "\n" +
                "The subject matter of the comic varies from statements on life and love to mathematical, programming, and scientific in-jokes. Some strips feature simple humor or pop-culture references. Although it has a cast of stick figures, the comic occasionally features landscapes, graphs and charts, and intricate mathematical patterns such as fractals. New cartoons are added three times a week, on Mondays, Wednesdays, and Fridays.";
        if(pageCount == -1){
            updatePageCount();
        }
    }

    @Override
    public int getIcon() {
        return R.mipmap.xkcd_ico;
    }

    @Override
    public format getFormat() {
        return format.PAGES;
    }

    @Override
    public int getPageCount() {
        return pageCount;
    }

    @Override
    public String[] getTags() {
        return new String[]{"it", "funny"};
    }

    public class XkcdPage{
        @SerializedName("num")
        int num;
        @SerializedName("title")
        String title;

        public XkcdPage(int num, String title){
            this.num = num;
            this.title = title;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public interface XkcdService{
        @GET("info.0.json")
        Call<XkcdPage> getLast();

        @GET("{chapter}/info.0.json")
        Call<XkcdPage> getChapter(@Path("chapter") int chapter);
    }

    //TODO Informing when data is updated
    public void updatePageCount(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://xkcd.com").addConverterFactory(GsonConverterFactory.create()).build();
        XkcdService service = retrofit.create(XkcdService.class);

        Call<XkcdPage> call = service.getLast();
        call.enqueue(new Callback<XkcdPage>() {
            @Override
            public void onResponse(Call<XkcdPage> call, Response<XkcdPage> response) {
                pageCount = response.body().getNum();
            }

            @Override
            public void onFailure(Call<XkcdPage> call, Throwable t) {
                //TODO onFailure
            }
        });
    }

    @Override
    public LiveData<List<Chapter>> getChapters() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://xkcd.com").addConverterFactory(GsonConverterFactory.create()).build();
        XkcdService service = retrofit.create(XkcdService.class);
        final MutableLiveData<List<Chapter>> chapters = new MutableLiveData<>();
        final List<Chapter> tmpList = new ArrayList<>();
        //chapters.setValue(tmpList);
        //tmpList.add(new Chapter("xkcd", 5));

        for( int i=1; i<6; i++){
            Call<XkcdPage> call = service.getChapter(i);
            call.enqueue(new Callback<XkcdPage>() {
                @Override
                public void onResponse(Call<XkcdPage> call, Response<XkcdPage> response) {
                    Chapter chapter = new Chapter(id, response.body().num);
                    chapter.setTitle(response.body().title);
                    tmpList.add(chapter);
                    chapters.postValue(tmpList);
                }

                @Override
                public void onFailure(Call<XkcdPage> call, Throwable t) {

                }
            });
        }
        return chapters;
    }

    @Override
    public String[] getLanguages() {
        return new String[]{"en"};
    }
}
