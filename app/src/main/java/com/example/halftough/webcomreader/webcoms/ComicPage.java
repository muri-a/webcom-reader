package com.example.halftough.webcomreader.webcoms;

import com.google.gson.annotations.SerializedName;

public class ComicPage {
    @SerializedName("num")
    String num;
    @SerializedName("title")
    String title;

    public ComicPage(String num, String title){
        this.num = num;
        this.title = title;
    }

    public String getNum() {
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
}
