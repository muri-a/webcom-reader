package com.example.halftough.webcomreader;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public abstract class OneByOneUrlDownloader<Extra> extends OneByOneDownloader<String, Extra> {

    public OneByOneUrlDownloader(){
        this(new LinkedList<String>(), new LinkedList<Extra>());
    }

    public OneByOneUrlDownloader(Queue<String> urls){
        this(urls, null);
    }

    public OneByOneUrlDownloader(Queue<String> urls, Queue<Extra>extras){
        this(urls, extras, 1);
    }

    public OneByOneUrlDownloader(Queue<String> urls, Queue<Extra>extras, int capacity){
        free = this.capacity = capacity;
        queue = urls;
        this.extras = extras;
    }

    abstract void onResponse(BufferedInputStream bufferinstream, Extra extra, String extentsion);

    protected void downloadElement(String element, final Extra extra){
        new asyncDownload(element).execute(extra);
    }

    private class asyncDownload extends AsyncTask<Extra, Void, Void>{
        String url;
        public asyncDownload(String url){
            this.url = url;
        }
        @Override
        protected Void doInBackground(Extra... extras) {
            int dot = url.lastIndexOf(".");
            String ext = (dot>=0)?url.substring(dot):"";
            try {
                URL url = new URL(this.url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream is = connection.getInputStream();
                BufferedInputStream bufferInStream = new BufferedInputStream(is);
                onResponse(bufferInStream, extras[0], ext);
                elementDownloaded();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
