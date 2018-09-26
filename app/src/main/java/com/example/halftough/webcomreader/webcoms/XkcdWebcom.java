package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.example.halftough.webcomreader.R;

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
    private XkcdService service;
    private static MutableLiveData<Integer> chapterCount;

    public XkcdWebcom(){
        if(chapterCount==null){
            chapterCount = new MutableLiveData<>();
            chapterCount.setValue(0);
        }
        initService();
    }

    @Override
    public String getId(){ return "xkcd"; }
    @Override
    public String getTitle(){ return "xkcd"; }
    @Override
    public String getDescription(){ return "Webcomic created by American author Randall Munroe. The comic's tagline describes it as \"A webcomic of romance, sarcasm, math, and language\". Munroe states on the comic's website that the name of the comic is not an initialism but \"just a word with no phonetic pronunciation\".\n" +
            "\n" +
            "\"The subject matter of the comic varies from statements on life and love to mathematical, programming, and scientific in-jokes. Some strips feature simple humor or pop-culture references. Although it has a cast of stick figures, the comic occasionally features landscapes, graphs and charts, and intricate mathematical patterns such as fractals. New cartoons are added three times a week, on Mondays, Wednesdays, and Fridays.";}
    @Override
    public int getIcon() {
        return R.mipmap.xkcd_ico;
    }
    @Override
    public format getFormat() {
        return format.PAGES;
    }
    @Override
    public String[] getTags() {
        return new String[]{"it", "funny", "science"};
    }
    @Override
    public String[] getLanguages() {
        return new String[]{"en"};
    }
    @Override
    public ReadingOrder getReadingOrder() {
        return ReadingOrder.NEWEST_FIRST;
    }

    @Override
    public MutableLiveData<Integer> getChapterCount() {
        return chapterCount;
    }

    public interface XkcdService{
        @GET("info.0.json")
        Call<ComicPage> getLast();

        @GET("{chapter}/info.0.json")
        Call<ComicPage> getChapter(@Path("chapter") String chapter);
    }

    private void initService(){
        if (service == null){
            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://xkcd.com").addConverterFactory(GsonConverterFactory.create()).build();
            service = retrofit.create(XkcdService.class);
        }
    }

    @Override
    public Call<ComicPage> getChapterMetaCall(String number) {
        initService();
        return service.getChapter(number);
    }

    @Override
    public List<String> getChapterList() {
        Integer chaptersNum = this.chapterCount.getValue();
        List<String> chapterList = new ArrayList<>();
        if(chaptersNum != null){
            //comic nr 404 doesn't exist
            for(int i = 1; i<404 && i<=chaptersNum; i++){
                chapterList.add(Integer.toString(i));
            }
            for(int i=405; i<=chaptersNum+1; i++){
                chapterList.add(Integer.toString(i));
            }
        }
        return chapterList;
    }

    @Override
    public void updateChapters(){
        Call<ComicPage> call = service.getLast();
        call.enqueue(new Callback<ComicPage>() {
            @Override
            public void onResponse(Call<ComicPage> call, Response<ComicPage> response) {
                chapterCount.postValue( Integer.parseInt(response.body().getChapterNumber()) -1); // We subtract 1, because comic nr 404 doesn't exist
            }

            @Override
            public void onFailure(Call<ComicPage> call, Throwable t) {
                chapterCount.postValue(chapterCount.getValue());
            }
        });
    }
}
