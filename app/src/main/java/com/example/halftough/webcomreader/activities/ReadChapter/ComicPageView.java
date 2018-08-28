package com.example.halftough.webcomreader.activities.ReadChapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

public class ComicPageView extends SurfaceView implements Runnable, Target {
    private enum TouchState { EMPTY, DOWN, ZOOM, SWIPE, MOVE }

    private ReadChapterActivity readChapterActivity;
    private Paint paint;
    private Bitmap pageImg;

    private Thread thread;
    private boolean running;
    private ValueAnimator slideAnimator;

    private TouchState touchState = TouchState.EMPTY;
    private boolean zoomed = false;
    private float startX, startY, startX2, startY2, moveX, moveY;
    private int imgWidth, imgHeight, padX, padY;
    private int slide;
    private int slideOffset;
    private static final long backSpeed = 250;



    public ComicPageView(Context context) {
        super(context);
        init(context);
    }
    public ComicPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public ComicPageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        readChapterActivity = (ReadChapterActivity)context;
        paint = new Paint();
        slideOffset = (int) (getResources().getDisplayMetrics().density*10);
        slideAnimator = ValueAnimator.ofInt(0, 0);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        pageImg = bitmap;
        calcImgSize();
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch(event.getAction()& MotionEvent.ACTION_MASK){
            case ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                if(zoomed)
                    touchState = TouchState.MOVE;
                else
                    touchState = TouchState.DOWN;
                break;
            case ACTION_UP:
                if(touchState == TouchState.SWIPE){
                    swipeEnd(event.getX());
                }
                if(zoomed){
//                  // TODO
                    zoomed = false;
                }
                touchState = TouchState.EMPTY;
                return false;
            case ACTION_POINTER_DOWN:
                if(touchState==TouchState.DOWN || touchState==TouchState.MOVE){
                    startX2 = event.getX(1);
                    startY2 = event.getY(1);
                    touchState = TouchState.ZOOM;
                }
                break;
            case ACTION_POINTER_UP:
                touchState = TouchState.MOVE;
                break;
            case ACTION_MOVE:
                switch(touchState){
                    case MOVE:
                        // TODO
                        break;
                    case DOWN:
                        if( Math.abs(event.getX()-startX) > slideOffset ){
                            touchState = TouchState.SWIPE;
                        }
                        break;
                    case SWIPE:
                        slide = Math.round(event.getX()-startX);
                        break;
                    case ZOOM:
                        // TODO
                        break;
                }
                break;
        }
        return true;
    }

    private void swipeEnd(float x) {
        slide = Math.round(x-startX);
        int a = Math.abs((getWidth()-Math.abs(slide)));
        final int sign = (int) Math.signum(x - startX);
        if(Math.abs(slide) > getWidth()/2){
            animateSlide(sign * getWidth(), (a*400)/getWidth(), new AnimationObserver() {
                @Override
                public void onEnd() {
                    pageImg = null;
                    slide = 0;
                    if(sign<0){
                        readChapterActivity.nextPage();
                    }
                    else{
                        readChapterActivity.previousPage();
                    }
                }
            });
        }
        else {
            animateSlide(0, Math.abs(slide*300)/getWidth());
        }
    }

    private void animateSlide(final int end, long duration){ animateSlide(end, duration, null); }

    private void animateSlide(final int end, long duration, final AnimationObserver observer) {
        slideAnimator = ValueAnimator.ofInt(slide, end);
        slideAnimator.setDuration(duration);
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                slide = (int)animation.getAnimatedValue();
                if(slide == end && observer!=null){
                    observer.onEnd();
                }
            }
        });
        slideAnimator.start();
    }

    @Override
    public void run() {
        Canvas canvas;
        running = true;
        while(running){
            if(getHolder().getSurface().isValid()){
                try {
                    canvas = getHolder().lockCanvas();
                    canvas.save();
                    canvas.drawColor(Color.WHITE);
                    if (pageImg != null) {
                        Rect src = new Rect(0,0, pageImg.getWidth(),pageImg.getHeight());
                        RectF des = new RectF(padX+slide, padY, imgWidth+padX+slide, imgHeight+padY);
                        canvas.drawBitmap(pageImg, null, des, paint);
                    }
                    getHolder().unlockCanvasAndPost(canvas);
                }
                catch(IllegalArgumentException e){
                }
            }
        }
    }

    public void start(){
        calcImgSize();
        thread = new Thread(this);
        thread.start();
    }

    public void stop(){
        running = false;
        try{
            thread.join();
        }
        catch (InterruptedException e){}
    }

    private void calcImgSize(){
        if(pageImg != null){
            float a = (float)(pageImg.getWidth()/pageImg.getHeight());
            float b = (float)(getWidth()/getHeight());
            if( a > b ){
                imgWidth = getWidth();
                double v = (double)imgWidth/pageImg.getWidth();
                imgHeight = (int)(pageImg.getHeight()*v);
                padX = 0;
                padY = (getHeight()-imgHeight)/2;
            }
            else{
                imgHeight = getHeight();
                double v = (double)imgHeight/pageImg.getHeight();
                imgWidth = (int)(pageImg.getWidth()*v);
                padX = (getWidth()-imgWidth)/2;
                padY = 0;
            }
        }
    }

    interface AnimationObserver{
        void onEnd();
    }

}
