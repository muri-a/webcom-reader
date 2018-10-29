package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.ChapterUpdateBroadcaster;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.TaskDelegate;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ChaptersRepository;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class LunarbaboonWebcom extends Webcom {
    private LunarbaboonService service;

    public LunarbaboonWebcom(){
    }

    @Override
    public String getId(){ return "lunarbaboon"; }
    @Override
    public String getTitle(){ return "Lunarbaboon"; }
    @Override
    public String getWebpage() {
        return "http://www.lunarbaboon.com/";
    }

    @Override
    public String getDescription(){ return "Some time in the 80's a human woman made love to a space monkey. Eight months later a lunarbaboon was born.\n" +
            "\n" +
            "Lunarbaboon is married and has one child. He works as a school teacher and lives a life similar to most North American humans.\n" +
            "\n" +
            "He is different from you though in a few distinct ways. Lunarbaboon has too many pubes. His body hair count is outrageous. When he eats he never really feels full. He poops 4 to 5 times a day and rarely smells his fingers after. Lunarbaboon is very fast, enjoys foods wrapped inside a taco shell, and never drinks with a straw(even when a straw is required). He is hardly ever satisfied with anything. He pretends to be nice and like human people, but generally he does not like most people. This makes lunarbaboon feel bad about himself.\n" +
            "\n" +
            "Lunarbaboon currently lives in Toronto, a city full of humans.";}
    @Override
    public int getIcon() {
        return R.drawable.wicon_lunarbaboon;
    }
    @Override
    public format getFormat() {
        return format.PAGES;
    }
    @Override
    public String[] getTags() {
        return new String[]{"funny"};
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

    public interface LunarbaboonService{
        @GET("")
        Call<LunarbaboonListPage> getNewest();

        @GET("comics/?currentPage={page}")
        Call<LunarbaboonListPage> getList(int page);

        @GET("{chapter}")
        Call<LunarbaboonComicPage> getChapter(@Path("chapter") String chapter);
    }

    //Contains a page of few consecutive comics
    public class LunarbaboonListPage {
        private int page;

        public List<Chapter> getChapters(){
            return null;
        }

        public void insertUntil(Chapter until, TaskDelegate delegate){
            insertUntil(until, null, delegate);
        }

        public void insertUntil(Chapter until, List<Chapter> leadingToAdd, TaskDelegate delegate){
            //We wait with adding chapters, when there is something in the database and we find new ones to aviod rare error which results chapter not being listed evev
            if(leadingToAdd == null) {
                leadingToAdd = new ArrayList<>();
            }
            for(Chapter chapter : getChapters()){
                if( chapter.compareTo(until) <= 0){
                    addLeadingChapters(leadingToAdd);
                    delegate.onFinish();
                    return;
                }
                else {
                    leadingToAdd.add(chapter);
                }
            }
            //when all've been added and none was in db
            Call<LunarbaboonListPage> nextPageCall = service.getList(page+1);
            Response<LunarbaboonListPage> nextPage;
            try {
                nextPage = nextPageCall.execute();
            } catch (IOException e) {
                //We discard what we do have, because saving it would mean errors later
                delegate.onFinish();
                return;
            }
            nextPage.body().insertUntil(until, leadingToAdd, delegate);
        }

        private void addLeadingChapters(List<Chapter> leadingToAdd){
            for(Chapter chapter : leadingToAdd){
                ChaptersRepository.insertChapter(chapter, chaptersDAO, readWebcomsDAO, chapterUpdateBroadcaster);
            }
        }

    }

    //Contains single comic page
    public class LunarbaboonComicPage extends ComicPage{
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
            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://www.lunarbaboon.com/").addConverterFactory(GsonConverterFactory.create()).build();
            service = retrofit.create(LunarbaboonService.class);
        }
    }

    //TODO this might be impossible with this comic. Think how to change it
    @Override
    public LiveData<ComicPage> getChapterMeta(String number) {
        initService();
        final MutableLiveData<ComicPage> page = new MutableLiveData<>();
        Call<LunarbaboonComicPage> call = service.getChapter(number);
        call.enqueue(new Callback<LunarbaboonComicPage>() {
            @Override
            public void onResponse(Call<LunarbaboonComicPage> call, Response<LunarbaboonComicPage> response) {
                page.postValue(response.body());
            }
            @Override
            public void onFailure(Call<LunarbaboonComicPage> call, Throwable t) {
                page.postValue(new LunarbaboonComicPage());
            }
        });
        return page;
    }

    @Override
    public LiveData<Uri> getChapterSource(String chapterNumber) {
        return null;
    }

    @Override
    public void updateChapterList(final TaskDelegate delegate) {
        initService();

        Call<LunarbaboonListPage> newestCall = service.getNewest();
        final Response<LunarbaboonListPage> newest;
        try {
            newest = newestCall.execute();
        } catch (IOException e) {
            delegate.onFinish();
            return;
        }
        final LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(getId());
        dbChapters.observeForever(new Observer<List<Chapter>>() {
            @Override
            public void onChanged(@Nullable List<Chapter> chapters) {
                dbChapters.removeObserver(this);
                if(!chapters.isEmpty()) {
                    Chapter dbLast = chapters.get(chapters.size() - 1);
                    newest.body().insertUntil(dbLast, new TaskDelegate() {
                        @Override
                        public void onFinish() {

                        }
                    });
                }
            }
        });
    }

}
