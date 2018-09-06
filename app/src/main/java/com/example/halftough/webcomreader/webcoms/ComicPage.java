package com.example.halftough.webcomreader.webcoms;

import com.google.gson.annotations.SerializedName;

public class ComicPage {
    @SerializedName("num")
    String num;
    @SerializedName("title")
    String title;
    @SerializedName("img")
    String img;

    public ComicPage(String num, String title){
        this.num = num;
        this.title = title;
    }

    public String getChapterNumber() {
        return num;
    }
    public void setNum(String num) {
        this.num = num;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() { return img; }
    public void setImg(String img) { this.img = img; }
}
