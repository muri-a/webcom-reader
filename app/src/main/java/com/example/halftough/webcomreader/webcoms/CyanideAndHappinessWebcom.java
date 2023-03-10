package com.example.halftough.webcomreader.webcoms;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.halftough.webcomreader.R;
import com.example.halftough.webcomreader.TaskDelegate;
import com.example.halftough.webcomreader.database.Chapter;
import com.example.halftough.webcomreader.database.ChaptersRepository;
import com.example.halftough.webcomreader.database.ReadWebcomRepository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class CyanideAndHappinessWebcom extends Webcom {
    private CyanideService service;

    public CyanideAndHappinessWebcom(){
    }

    @Override
    public String getId(){ return "cyanideandhappiness"; }
    @Override
    public String getTitle(){ return "Cyanide and Happiness"; }
    @Override
    public String getWebpage() {
        return "http://explosm.net/";
    }
    @Override
    public String getDescription(){ return "Webcomic written and illustrated by Rob DenBleyker, Kris Wilson, Dave McElfatrick and formerly Matt Melvin, published on their website explosm.net. It was created on December 9, 2004, and started running daily on January 26, 2005. The comic's authors attribute its success to its often controversial nature, and the series is noted for its dark humor and sometimes surrealistic approach."; }
    @Override
    public int getIcon() {
        return R.drawable.wicon_cyanideandhappiness;
    }
    @Override
    public format getFormat() {
        return format.PAGES;
    }
    @Override
    public boolean canOpenSource() {
        return true;
    }

    @Override
    public String[] getTags() {
        String[] tags =  {"funny", "dark"};
        return tags;
    }
    @Override
    public String[] getLanguages() {
        return new String[]{"en"};
    }
    @Override
    public ReadingOrder getReadingOrder() {
        return ReadingOrder.NEWEST_FIRST;
    }

    public class CyanideComicPage extends ComicPage{
        private String chapter, title, url, previous = "";
        @Override
        public String getChapterNumber() {
            return chapter;
        }
        @Override
        public String getTitle() {
            return title;
        }
        @Override
        public String getImage() {
            return url;
        }
        public String getPrevious(){ return previous; }

        public void setChapter(String chapter){
            this.chapter = chapter;
        }
        public void setImage(String image) { this.url = image; }
        public void setTitle(String title){
            this.title = title;
        }
        public void setPrevious(String previous) { this.previous = previous; }

        public void insertUntilEnd(TaskDelegate delegate){
            insertUntil(null, delegate);
        }

        public void insertUntil(final String until, final TaskDelegate delegate){
            Chapter insert = new Chapter(getId(), chapter);
            insert.setTitle(title);
            ChaptersRepository.insertChapter(insert, chaptersDAO, readWebcomsDAO, chapterUpdateBroadcaster);
            if(!previous.isEmpty() && !previous.equals(until)){
                Call<CyanideComicPage> call = service.getChapter(previous);
                call.enqueue(new Callback<CyanideComicPage>() {
                    @Override
                    public void onResponse(Call<CyanideComicPage> call, Response<CyanideComicPage> response) {
                        if(response.isSuccessful()) {
                            response.body().insertUntil(until, delegate);
                        }
                        else {
                            onFailure(call, new Throwable());
                        }
                    }

                    @Override
                    public void onFailure(Call<CyanideComicPage> call, Throwable t) {
                        delegate.onFinish();
                    }
                });
            }
            else{
                delegate.onFinish();
            }
        }
    }

    public class CyanideComicPageConverterFactory extends Converter.Factory{
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            if (type == CyanideComicPage.class)
                return new CyanideComicPageConverter();
            return null;
        }
    }

    public class CyanideComicPageConverter implements Converter<ResponseBody, CyanideComicPage>{
        @Override
        public CyanideComicPage convert(ResponseBody value) throws IOException {
            CyanideComicPage page = new CyanideComicPage();
            Document doc = Jsoup.parse(value.string());
            Elements meta = doc.select("meta");
            for(Element element : meta){
                String prop = element.attr("property");
                if(prop.equals("og:url")){
                    String chapter = element.attr("content");
                    if(!chapter.isEmpty()){
                        int index = chapter.lastIndexOf('/');
                        if(index == chapter.length()-1){
                            chapter = chapter.substring(0, chapter.length()-1);
                            index = chapter.lastIndexOf('/');
                        }
                        chapter = chapter.substring(index+1);
                        page.setChapter(chapter);
                    }
                }
                else if(prop.equals("og:image")){
                    String image = element.attr("content");
                    page.setImage(image);
                }
            }
            Element author = doc.getElementById("comic-author");
            String text = author.html();
            text = text.substring(0, text.indexOf("\n"));
            page.setTitle(text);
            Element previous = doc.selectFirst(".nav-previous");
            if(previous != null){
                String href = previous.attr("href");
                if(!href.equals("")) {
                    int index = href.lastIndexOf("/");
                    if (index == href.length() - 1) {
                        href = href.substring(0, href.length() - 1);
                        index = href.lastIndexOf("/");
                    }
                    href = href.substring(index + 1);
                    page.setPrevious(href);
                }
            }
            return page;
        }
    }

    public interface CyanideService{
        @GET("latest")
        Call<CyanideComicPage> getLast();

        @GET("{chapter}")
        Call<CyanideComicPage> getChapter(@Path("chapter") String chapter);
    }

    private void initService(){
        if (service == null){
            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://explosm.net/comics/")
                    .addConverterFactory(new CyanideComicPageConverterFactory()).build();
            service = retrofit.create(CyanideService.class);
        }
    }

    public LiveData<ComicPage> getChapterPage(String number) {
        initService();
        final MutableLiveData<ComicPage> page = new MutableLiveData<>();
        Call<CyanideComicPage> call = service.getChapter(number);
        call.enqueue(new Callback<CyanideComicPage>() {
            @Override
            public void onResponse(Call<CyanideComicPage> call, Response<CyanideComicPage> response) {
                page.postValue(response.body());
            }
            @Override
            public void onFailure(Call<CyanideComicPage> call, Throwable t) {
                page.postValue(new CyanideComicPage());
            }
        });
        return page;
    }

    @Override
    public LiveData<Uri> getChapterSource(String chapterNumber) {
        MutableLiveData<Uri> source = new MutableLiveData<>();
        source.setValue(Uri.parse("http://explosm.net/comics/"+chapterNumber));
        return source;
    }

    @Override
    public LiveData<String> getChapterUrl(String chapter) {
        final MutableLiveData<String> chapterUrl = new MutableLiveData<>();
        final LiveData<ComicPage> call = getChapterPage(chapter);
        call.observeForever(new Observer<ComicPage>() {
            @Override
            public void onChanged(@Nullable ComicPage comicPage) {
                call.removeObserver(this);
                if(comicPage != null)
                    chapterUrl.postValue(comicPage.getImage());
                else
                    chapterUrl.postValue("");
            }
        });
        return chapterUrl;
    }

    private boolean a = false, b = false;
    //Downloader Service is here for broadcasting. Might move it somewhere
    //TODO when there are few new chapters to download it starts downloading them, gets at least one, but not all and something goes wrong, those are lost forever
    //TODO changing "downloading direction" should fix it
    @Override
    public void updateChapterList(final TaskDelegate delegate) {
        initService();
        Call<CyanideComicPage> call = service.getLast();

        call.enqueue(new Callback<CyanideComicPage>() {
            @Override
            public void onResponse(Call<CyanideComicPage> call, final Response<CyanideComicPage> response) {
                if(!response.isSuccessful()){
                    onFailure(call, new Throwable());
                    return;
                }
                final int lastChapter = Integer.parseInt(response.body().getChapterNumber());
                final LiveData<List<Chapter>> dbChapters = chaptersDAO.getChapters(getId());
                dbChapters.observeForever(new Observer<List<Chapter>>() {
                    @Override
                    public void onChanged(@Nullable List<Chapter> chapters) {
                        dbChapters.removeObserver(this);
                        if(!chapters.isEmpty()){
                            Chapter lastDb = chapters.get(chapters.size()-1);
                            if(Integer.parseInt(lastDb.getChapter()) < lastChapter){
                                //// pobieraj wszystkie nowe
                                ReadWebcomRepository.setLastUpdateDate(getId(), response.body().title.replaceAll("\\.", "-"), readWebcomsDAO);
                                response.body().insertUntil(lastDb.getChapter(), new TaskDelegate(){
                                    @Override
                                    public void onFinish() {
                                        a = true;
                                        if(b){
                                            delegate.onFinish();
                                        }
                                    }
                                });
                            }
                            else{
                                a = true;
                            }
                            Chapter firstDb = chapters.get(0);
                            final LiveData<ComicPage> oldest = getChapterPage(firstDb.getChapter());
                            oldest.observeForever(new Observer<ComicPage>() {
                                @Override
                                public void onChanged(@Nullable ComicPage comicPage) {
                                    oldest.removeObserver(this);
                                    CyanideComicPage page = (CyanideComicPage)comicPage;
                                    if(!page.getPrevious().isEmpty()){
                                        final LiveData<ComicPage> older = getChapterPage(page.getPrevious());
                                        older.observeForever(new Observer<ComicPage>() {
                                            @Override
                                            public void onChanged(@Nullable ComicPage comicPage) {
                                                older.removeObserver(this);
                                                ((CyanideComicPage)comicPage).insertUntilEnd(new TaskDelegate() {
                                                    @Override
                                                    public void onFinish() {
                                                        b = true;
                                                        if(a){
                                                            delegate.onFinish();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    else{
                                        b = true;
                                        if(a)
                                            delegate.onFinish();
                                    }
                                }
                            });
                        }
                        else{
                            ReadWebcomRepository.setLastUpdateDate(getId(), response.body().title.replaceAll("\\.", "-"), readWebcomsDAO);
                            response.body().insertUntilEnd(new TaskDelegate() {
                                @Override
                                public void onFinish() {
                                    delegate.onFinish();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<CyanideComicPage> call, Throwable t) {
                delegate.onFinish();
            }
        });
    }

}
