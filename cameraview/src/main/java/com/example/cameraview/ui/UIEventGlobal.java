package com.example.cameraview.ui;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.example.cameraview.ui.ICameraUI.OnGestureListener;

/**
 * 包括对焦和缩放
 */
public class UIEventGlobal {

    private OnGestureListener mOuterGestureLsn;
    private ScaleGestureDetector mScaleGestureDector;
    private GestureDetector mGestureDector;

    public UIEventGlobal(Context context){
        mGestureDector = new GestureDetector(context, mGestureLsn);
        mScaleGestureDector = new ScaleGestureDetector(context, mScaleGestureLsn);
    }
    public void setOnGestureListener(OnGestureListener listener) {
        mOuterGestureLsn = listener;
    }
    //focus
    GestureDetector.OnGestureListener mGestureLsn = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.showPress();
            }
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.onSingleTap(e);
            }
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.onLongPress();
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };
    //zoom Scale
    ScaleGestureDetector.OnScaleGestureListener mScaleGestureLsn = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (null != mOuterGestureLsn) {
                mOuterGestureLsn.onScale(detector.getScaleFactor());
            }
            return true;
        }
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };

    public ScaleGestureDetector getScaleGestureDetector(){
        return mScaleGestureDector;
    }
    public GestureDetector getGestureDetector(){
        return mGestureDector;
    }
    public OnGestureListener getOnGestureListener() {
        return mOuterGestureLsn;
    }
}
