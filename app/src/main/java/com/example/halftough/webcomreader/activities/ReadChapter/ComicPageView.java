package com.example.halftough.webcomreader.activities.ReadChapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

public class ComicPageView extends android.support.v7.widget.AppCompatImageView {
    private enum TouchState { EMPTY, DOWN, ZOOM, SWIPE, MOVE }
    private TouchState touchState = TouchState.EMPTY;
    private boolean zoomed = false;
    private float startX, startY;
    private static final long backSpeed = 250;
    private ReadChapterActivity readChapterActivity;

    public ComicPageView(Context context) {
        super(context);
        readChapterActivity = (ReadChapterActivity)getContext();
    }

    public ComicPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        readChapterActivity = (ReadChapterActivity)getContext();
    }

    public ComicPageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readChapterActivity = (ReadChapterActivity)getContext();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch(event.getAction()){
            case ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                touchState = TouchState.DOWN;
                break;
            case ACTION_UP:
                if(touchState == TouchState.SWIPE){
                    int diff = (int)Math.abs(event.getX() - startX);
                    float sign = Math.signum(event.getX() - startX);
                    if(diff > getWidth()/3){
                        animate().setDuration((long) (backSpeed * diff / (getWidth() / 2.7))).x(sign*getWidth());
                        if(sign<0){
                            readChapterActivity.nextPage();
                        }
                        else{
                            readChapterActivity.previousPage();
                        }
                    }
                    else {
                        animate().setDuration((long) (backSpeed * diff / (getWidth() / 2.7))).x(0f);
                    }
                }
                touchState = TouchState.EMPTY;
                return false;
            case ACTION_POINTER_DOWN:
                if(touchState==TouchState.DOWN){
                    touchState = TouchState.ZOOM;
                }
                break;
            case ACTION_POINTER_UP:
                break;
            case ACTION_MOVE:
                switch(touchState){
                    case DOWN:
                        if( Math.abs(event.getX()-startX) > 5 ){
                            touchState = TouchState.SWIPE;
                        }
                        break;
                    case SWIPE:
                        // TODO fix shredding
                        setX(event.getX()-startX);
                        break;
                    case ZOOM:
                        break;
                }
                break;
        }
        return true;
    }

}
