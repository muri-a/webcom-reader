package com.example.halftough.webcomreader.activities.ReadChapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

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
    private ValueAnimator slideAnimator, xAnimator, yAnimator, wAnimator, hAnimator;

    private TouchState touchState = TouchState.EMPTY;
    private boolean zoomed = false;
    private float startX1, startY1, startX2, startY2;
    private float x1f, y1f, x2f, y2f;
    private int imgWidth, imgHeight, padX, padY, sPadX, sPadY;
    private int slide;
    private int slideOffset;
    private float currentZoom;

    private static final long SLIDE_BACK_SPEED = 300;
    private static final long SLIDE_NEXT_SPEED = 400;
    private static final long FIX_ZOOM_SPEED = 80;
    private static final float MAX_ZOOM_MOD = 2.5f;
    private static final float MIN_ZOOM_MOD = 0.8f;
    private float maxZoom, minZoom, noZoom;


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
                saveStartPoint1(event.getX(), event.getY());
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
                    saveStartPoint2(event.getX(1), event.getY(1));
                    touchState = TouchState.ZOOM;
                }
                break;
            case ACTION_POINTER_UP:
                endZoom();
                break;
            case ACTION_MOVE:
                switch(touchState){
                    case MOVE:
                        // TODO
                        break;
                    case DOWN:
                        if( Math.abs(event.getX()- startX1) > slideOffset ){
                            touchState = TouchState.SWIPE;
                        }
                        break;
                    case SWIPE:
                        slide = Math.round(event.getX()- startX1);
                        break;
                    case ZOOM:
                        changeZoom(event.getX(), event.getY(), event.getX(1), event.getY(1));
                        break;
                }
                break;
        }
        return true;
    }

    private void endZoom() {
        if(currentZoom < noZoom){
            int newW = Math.round(pageImg.getWidth()*noZoom);
            int newH = Math.round(pageImg.getHeight()*noZoom);
            int newX = sPadX;
            int newY = sPadY;

            animateZoom(newX, newY, newW, newH, (long)((noZoom-currentZoom)/(noZoom-minZoom)*FIX_ZOOM_SPEED));
            currentZoom = noZoom;
        }
        touchState = TouchState.MOVE;
    }

    private void saveStartPoint1(float x, float y) {
        startX1 = x;
        startY1 = y;
    }

    private void saveStartPoint2(float x, float y) {
        startX2 = x;
        startY2 = y;

        // see where we touched on the image, not view (0-begin, 1-end)
        x1f = (startX1-padX)/imgWidth;
        y1f = (startY1-padY)/imgHeight;
        x2f = (x-padX)/imgWidth;
        y2f = (y-padY)/imgHeight;
    }

    private void changeZoom(float x1, float y1, float x2, float y2) {
        // We find position/size such that fxs,fys move to new position
        float w = (x1-x2)/(x1f-x2f);
        float h = (y1-y2)/(y1f-y2f);
        float x = -(x1f*w-x1);
        float y = -(y1f*h-y1);

        // Fixing aspect ratio
        // Calculate how much both are being zoomed and take average
        float zx = w/pageImg.getWidth();
        float zy = h/pageImg.getHeight();
        currentZoom = (zx+zy)/2;
        currentZoom = Math.min(currentZoom,maxZoom); // don't let zoom be bigger than maxZoom
        currentZoom = Math.max(currentZoom, minZoom); // or smaller than minEditZoom

        imgWidth = Math.round(pageImg.getWidth()*currentZoom);
        imgHeight = Math.round(pageImg.getHeight()*currentZoom);

        //x,y are calculated for initial transformation, so we need to fix them as well
        padX = Math.round(x+(w-imgWidth)/2);
        padY = Math.round(y+(h-imgHeight)/2);
    }

    private void swipeEnd(float x) {
        slide = Math.round(x- startX1);
        int a = Math.abs((getWidth()-Math.abs(slide)));
        final int sign = (int) Math.signum(x - startX1);
        if(Math.abs(slide) > getWidth()/2){
            animateSlide(sign * getWidth(), (a* SLIDE_NEXT_SPEED)/getWidth(), new AnimationObserver() {
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
            animateSlide(0, Math.abs(slide* SLIDE_BACK_SPEED)/getWidth());
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

    private void animateZoom(int x, int y, int w, int h, long duration){
        xAnimator = ValueAnimator.ofInt(padX, x);
        yAnimator = ValueAnimator.ofInt(padY, y);
        wAnimator = ValueAnimator.ofInt(imgWidth, w);
        hAnimator = ValueAnimator.ofInt(imgHeight, h);

        xAnimator.setDuration(duration);
        yAnimator.setDuration(duration);
        wAnimator.setDuration(duration);
        hAnimator.setDuration(duration);

        xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                padX = (int)animation.getAnimatedValue();
            }
        });
        yAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                padY = (int)animation.getAnimatedValue();
            }
        });
        wAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                imgWidth = (int)animation.getAnimatedValue();
            }
        });
        hAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                imgHeight = (int)animation.getAnimatedValue();
            }
        });

        xAnimator.start();
        yAnimator.start();
        wAnimator.start();
        hAnimator.start();
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
                        //Rect src = new Rect(0,0, pageImg.getWidth(),pageImg.getHeight());
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
            float imgRatio = (float)(pageImg.getWidth()/pageImg.getHeight());
            float screenRatio = (float)(getWidth()/getHeight());
            if( imgRatio > screenRatio ){
                imgWidth = getWidth();
                noZoom = (float)imgWidth/pageImg.getWidth();
                imgHeight = (int)(pageImg.getHeight()*noZoom);
                padX = 0;
                padY = (getHeight()-imgHeight)/2;
            }
            else{
                imgHeight = getHeight();
                noZoom = (float)imgHeight/pageImg.getHeight();
                imgWidth = (int)(pageImg.getWidth()*noZoom);
                padX = (getWidth()-imgWidth)/2;
                padY = 0;
            }
            sPadX = padX;
            sPadY = padY;
            maxZoom = MAX_ZOOM_MOD*noZoom;
            minZoom = MIN_ZOOM_MOD*noZoom;
        }
    }

    interface AnimationObserver{
        void onEnd();
    }

}
