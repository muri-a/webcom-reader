package com.example.halftough.webcomreader.webcoms;

import android.graphics.drawable.Drawable;

import com.example.halftough.webcomreader.R;

public class XkcdWebcom extends Webcom {
    public XkcdWebcom(){
        id = "xkcd";
        title = "xkcd";
        description = "Webcomic created by American author Randall Munroe. The comic's tagline describes it as \"A webcomic of romance, sarcasm, math, and language\". Munroe states on the comic's website that the name of the comic is not an initialism but \"just a word with no phonetic pronunciation\".\n" +
                "\n" +
                "The subject matter of the comic varies from statements on life and love to mathematical, programming, and scientific in-jokes. Some strips feature simple humor or pop-culture references. Although it has a cast of stick figures, the comic occasionally features landscapes, graphs and charts, and intricate mathematical patterns such as fractals. New cartoons are added three times a week, on Mondays, Wednesdays, and Fridays.";
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
    public int getPageNumber() {
        return 0;
    }
}
