package com.example.halftough.webcomreader;

public abstract class TaskDelegate {
    //TODO public for now. Change it later
    abstract public void onFinish();

    public void finish(){
        onFinish();
    }

    public void error(String message){

    }
}
