package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.ChapterUpdateBroadcaster;
import com.example.halftough.webcomreader.OneByOneChapterDownloader;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.TaskDelegate;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ChaptersRepository;
import com.example.halftough.webcomreader.database.ReadWebcomRepository;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class XkcdWebcom extends Webcom {
    private XkcdService service;

    @Override
    public String getId(){ return "xkcd"; }
    @Override
    public String getTitle(){ return "xkcd"; }
    @Override
    public String getWebpage() {
        return "https://xkcd.com/";
    }

    @Override
    public String getDescription(){ return "Webcomic created by American author Randall Munroe. The comic's tagline describes it as \"A webcomic of romance, sarcasm, math, and language\". Munroe states on the comic's website that the name of the comic is not an initialism but \"just a word with no phonetic pronunciation\".\n" +
            "\n" +
            "\"The subject matter of the comic varies from statements on life and love to mathematical, programming, and scientific in-jokes. Some strips feature simple humor or pop-culture references. Although it has a cast of stick figures, the comic occasionally features landscapes, graphs and charts, and intricate mathematical patterns such as fractals. New cartoons are added three times a week, on Mondays, Wednesdays, and Fridays.";}
    @Override
    public int getIcon() {
        return R.drawable.wicon_xkcd;
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
    public boolean canOpenSource() {
        return true;
    }

    public interface XkcdService{
        @GET("info.0.json")
        Call<XkcdComicPage> getLast();

        @GET("{chapter}/info.0.json")
        Call<XkcdComicPage> getChapter(@Path("chapter") String chapter);
    }

    public class XkcdComicPage extends ComicPage{
        @SerializedName("num")
        String num;
        @SerializedName("title")
        String title;
        @SerializedName("img")
        String img;
        @SerializedName("year")
        int year;
        @SerializedName("month")
        int month;
        @SerializedName("day")
        int day;

        public void setNum(String num){ this.num = num; }
        public void setTitle(String title){ this.title = title; }
        public void setImg(String img){ this.img = img; }

        @Override
        public String getChapterNumber() {
            return num;
        }
        @Override
        public String getTitle() {
            return title;
        }
        @Override
        public String getImage() {
            return img;
        }

        public String getDate() { return String.format("%d-%02d-%02d 00:00:00", year, month, day); }
    }

    private void initService(){
        if (service == null){
            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://xkcd.com").addConverterFactory(GsonConverterFactory.create()).build();
            service = retrofit.create(XkcdService.class);
        }
    }

    @Override
    public LiveData<ComicPage> getChapterMeta(String number) {
        initService();
        final MutableLiveData<ComicPage> page = new MutableLiveData<>();
        Call<XkcdComicPage> call = service.getChapter(number);
        call.enqueue(new Callback<XkcdComicPage>() {
            @Override
            public void onResponse(Call<XkcdComicPage> call, Response<XkcdComicPage> response) {
                page.postValue(response.body());
            }
            @Override
            public void onFailure(Call<XkcdComicPage> call, Throwable t) {
                page.postValue(new XkcdComicPage());
            }
        });
        return page;
    }

    @Override
    public LiveData<Uri> getChapterSource(String chapterNumber) {
        MutableLiveData<Uri> source = new MutableLiveData<>();
        source.setValue(Uri.parse("https://xkcd.com/"+chapterNumber));
        return source;
    }

    @Override
    public void updateChapterList(final TaskDelegate delegate) {
        initService();
        Call<XkcdComicPage> call = service.getLast();
        final Response<XkcdComicPage> response;
        try {
            //We should be in a thread, so doing it synchronously is OK
            response = call.execute();
        } catch (IOException e) {
            delegate.onFinish();
            return;
        }
        if(response.isSuccessful()){
            final int lastChapter = Integer.parseInt(response.body().getChapterNumber());
            final LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(getId());
            dbChapters.observeForever(new Observer<List<Chapter>>() {
                @Override
                public void onChanged(@Nullable List<Chapter> chapters) {
                    Queue<String> chaptersToGet = new LinkedList<>();
                    dbChapters.removeObserver(this);
                    int i = lastChapter;
                    ListIterator<Chapter> it = chapters.listIterator(chapters.size());
                    while (it.hasPrevious()){
                        Chapter ch = it.previous();
                        while(i > Integer.parseInt(ch.getChapter())){
                            //This will only happend for latest chapter, if it haven't been in database yet
                            //We update information about when was webcom last updated
                            if(i == lastChapter){
                                String date = response.body().getDate();
                                ReadWebcomRepository.setLastUpdateDate(getId(), date, readWebcomsDAO);
                            }
                            if(i != 404) //comic number 404 doesn't exist
                                chaptersToGet.add(Integer.toString(i));
                            i--;
                        }
                        i--;
                    }
                    while(i > 0){
                        if(i != 404)
                            chaptersToGet.add(Integer.toString(i));
                        i--;
                    }

                    if(!chaptersToGet.isEmpty()){
                        int count = chapters.size() + chaptersToGet.size();
                        ReadWebcomRepository.updateChapterCount(getId(), count, readWebcomsDAO);
                    }

                    //TODO editable number of slots
                    new OneByOneChapterDownloader(chaptersToGet, XkcdWebcom.this, chapterUpdateBroadcaster, 2){
                        @Override
                        public void onResponse(ComicPage page) {
                            if(page != null) {
                                Chapter chapter = new Chapter(getId(), page.getChapterNumber());
                                chapter.setTitle(page.getTitle());
                                ChaptersRepository.insertChapter(chapter, chaptersDAO);
                            }
                        }

                        @Override
                        protected void onFinished() {
                            delegate.onFinish();
                        }
                    }.download();
                }
            });
        }
        else{
            delegate.onFinish();
        }
    }

}
