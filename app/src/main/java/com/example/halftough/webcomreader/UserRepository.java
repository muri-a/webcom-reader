package com.example.halftough.webcomreader;

import com.example.halftough.webcomreader.webcoms.DilbertWebcom;
import com.example.halftough.webcomreader.webcoms.Webcom;
import com.example.halftough.webcomreader.webcoms.XkcdWebcom;


public class UserRepository {

    static public Webcom getWebcomInstance(String id) throws NoWebcomClassException {
        switch(id){
            case "dilbert":
                return new DilbertWebcom();
            case "xkcd":
                return new XkcdWebcom();
            default:
                throw new NoWebcomClassException(id);
        }
    }

}
