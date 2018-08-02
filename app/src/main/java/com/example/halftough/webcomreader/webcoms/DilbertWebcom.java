package com.example.halftough.webcomreader.webcoms;

import com.example.halftough.webcomreader.R;

public class DilbertWebcom extends Webcom {
    public DilbertWebcom(){
        id = "dilbert";
        title = "Dilbert";
    }

    @Override
    public int getIcon() {
        return R.mipmap.dilbert_ico;
    }

    @Override
    public format getFormat() {
        return format.PAGES;
    }

    @Override
    public int getPageNumber() {
        return 1;
    }
}
