package com.example.halftough.webcomreader;

public class NoWebcomClassException extends Exception {
    public String id;

    public NoWebcomClassException(String id){
        this.id = id;
    }

}
