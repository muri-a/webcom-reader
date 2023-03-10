package com.example.halftough.webcomreader;

import java.util.Queue;

public abstract class OneByOneDownloader<ElementType, Extra> {
    protected int capacity;
    protected int free;
    protected boolean downloading = false;
    protected Queue<ElementType> queue;
    protected Queue<Extra> extras;

    public void add(ElementType element, Extra extra){
        queue.add(element);
        if(extras != null){
            extras.add(extra);
        }
    }
    // Add to the list and start downloading, if it wasn't before
    public void enqueue(ElementType element, Extra extra){
        add(element, extra);
        download();
    }


    public void download(){
        if (!downloading) {
            if(queue.isEmpty()){
                onFinished();
            }
            while (free > 0 && !queue.isEmpty()) {
                downloading = true;
                free--;
                ElementType element = queue.remove();
                Extra extra = extras!=null?extras.remove():null;

                downloadElement(element, extra);
            }
        }
    }

    protected abstract void downloadElement(ElementType element, final Extra extra);

    protected void onFinished(){ }

    // Method that should be called on success of downloadElement.
    // Because ways of doing that may vary, extension of this class should remember to call it.
    // finishedExtra isn't used in this method, but it's for overriding
    protected void elementDownloaded(Extra finishedExtra){
        if(!queue.isEmpty()) {
            ElementType element = queue.remove();
            Extra extra = extras!=null?extras.remove():null;

            downloadElement(element, extra);
        }
        else{
            free++;
            if(free==capacity) {
                downloading = false;
                onFinished();
            }
        }
    }

}
