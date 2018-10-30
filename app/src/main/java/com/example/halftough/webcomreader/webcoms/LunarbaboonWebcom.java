package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.halftough.webcomreader.ChapterUpdateBroadcaster;
import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.TaskDelegate;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersDAO;
import com.example.halftough.webcomreader.database.ChaptersRepository;
import com.example.halftough.webcomreader.database.ReadWebcomsDAO;
import com.google.gson.annotations.SerializedName;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
        @GET("/")
        Call<LunarbaboonListPage> getNewest();

        @GET("comics")
        Call<LunarbaboonListPage> getList(@Query("currentPage") int page);

        @GET("comics/{chapter}.html")
        Call<LunarbaboonComicPage> getChapter(@Path("chapter") String chapter);
    }

    class LunarbaboonConverterFactory extends Converter.Factory{
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            if (type == LunarbaboonListPage.class)
                return new LunarbaboonListConverter();
            if (type == LunarbaboonComicPage.class)
                return new LunarbaboonPageConverter();
            return null;
        }
    }

    class LunarbaboonListConverter implements Converter<ResponseBody, LunarbaboonListPage>{
        @Override
        public LunarbaboonListPage convert(ResponseBody value) throws IOException {
            LunarbaboonListPage page = new LunarbaboonListPage();
            Document doc = Jsoup.parse(value.string());
            Elements entries = doc.select(".journal-entry");
            for(Element entry : entries){
                String id = entry.id();
                Chapter chapter = new Chapter(getId(), id.substring(4));
                Element titleA = entry.selectFirst(".title a");
                String title = titleA.html();
                String extra = titleA.attr("href");
                int index = extra.indexOf("comics");
                extra = extra.substring(index+7, extra.length()-5);
                chapter.setTitle(title);
                chapter.setExtra(extra);
                page.addChapter(chapter);
            }
            int pagenumber = Integer.parseInt( doc.selectFirst(".activePage").html() );
            page.setPageNumber(pagenumber);
            Elements pagination = doc.getElementsByClass("paginationPageNumber");
            int lastPage = Integer.parseInt( pagination.last().html() );
            page.setLast( pagenumber == lastPage );
            return page;
        }
    }

    class LunarbaboonPageConverter implements Converter<ResponseBody, LunarbaboonComicPage>{
        @Override
        public LunarbaboonComicPage convert(ResponseBody value) throws IOException {
            LunarbaboonComicPage page = new LunarbaboonComicPage();
            Document doc = Jsoup.parse(value.string());
            Element img = doc.selectFirst(".body img");
            String src = img.attr("src");
            src = src.substring(0, src.indexOf("?__"));
            page.setImage( "http://www.lunarbaboon.com" + src );
            return page;
        }
    }

    //Contains a page of few consecutive comics
    public class LunarbaboonListPage {
        private int page;
        private boolean isLast;
        List<Chapter> chapters;

        public LunarbaboonListPage(){
            chapters = new ArrayList<>();
        }

        public void addChapter(Chapter chapter){
            chapters.add(chapter);
        }

        public void setPageNumber(int pageNumber){
            page = pageNumber;
        }
        public void setLast(boolean last){
            isLast = last;
        }

        public List<Chapter> getChapters(){
            return chapters;
        }

        public void insertUntil(Chapter until, TaskDelegate delegate){
            insertUntil(until, null, delegate);
        }

        public void insertUntilEnd(TaskDelegate delegate){
            readWebcomsDAO.setExtra(getId(), Integer.toString(page));
            for(Chapter chapter : getChapters()){
                ChaptersRepository.insertChapter(chapter, chaptersDAO, readWebcomsDAO, chapterUpdateBroadcaster);
            }
            if(!isLast){
                Call<LunarbaboonListPage> nextPageCall = service.getList(page+1);
                Response<LunarbaboonListPage> nextPage;
                try {
                    nextPage = nextPageCall.execute();
                } catch (IOException e) {
                    delegate.onFinish();
                    return;
                }
                nextPage.body().insertUntilEnd(delegate);
            }
            else{
                delegate.finish();
            }
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
    public class LunarbaboonComicPage{
        String image;

        public void setImage(String img){ image = img; }
        public String getImage() {
            return image;
        }
    }

    private void initService(){
        if (service == null){
            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://www.lunarbaboon.com/")
                    .addConverterFactory(new LunarbaboonConverterFactory()).build();
            service = retrofit.create(LunarbaboonService.class);
        }
    }

    @Override
    public LiveData<String> getChapterUrl(String chapter) {
        initService();

        final MutableLiveData<String> chapterUrl = new MutableLiveData<>();

        final LiveData<String> extraLive = chaptersDAO.getExtra( getId(), chapter);
        extraLive.observeForever(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String extra) {
                extraLive.removeObserver(this);
                Call<LunarbaboonComicPage> call = service.getChapter(extra);
                call.enqueue(new Callback<LunarbaboonComicPage>() {
                    @Override
                    public void onResponse(Call<LunarbaboonComicPage> call, Response<LunarbaboonComicPage> response) {
                        if(response.isSuccessful()){
                            chapterUrl.setValue( response.body().getImage() );
                        }
                        else{
                            onFailure(call, new Throwable());
                        }
                    }

                    @Override
                    public void onFailure(Call<LunarbaboonComicPage> call, Throwable t) {
                        chapterUrl.setValue("");
                    }
                });
            }
        });

        return chapterUrl;
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

        Chapter dbLast = chaptersDAO.getLastChapterAsync(getId());
        if (dbLast != null) {
            newest.body().insertUntil(dbLast, new TaskDelegate() {
                @Override
                public void onFinish() {
                    String extra = readWebcomsDAO.getExtraAsync( getId() );
                    int lastPage;
                    if(extra == null){
                        lastPage = 1;
                    }
                    else{
                        lastPage = Integer.parseInt(extra);
                    }
                    Call<LunarbaboonListPage> listCall = service.getList(lastPage);
                    Response<LunarbaboonListPage> listPage;
                    try {
                        listPage = listCall.execute();
                    } catch (IOException e) {
                        delegate.onFinish();
                        return;
                    }
                    listPage.body().insertUntilEnd( delegate );
                }
            });
        }
        else{
            newest.body().insertUntilEnd( delegate );
        }
    }

}
