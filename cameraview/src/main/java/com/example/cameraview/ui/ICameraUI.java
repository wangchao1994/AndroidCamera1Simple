package com.example.cameraview.ui;

import android.view.MotionEvent;

public interface ICameraUI {
    interface OnGestureListener {
        boolean onSingleTap(MotionEvent e);
        void onScale(float factor);
        void showPress();
        void onLongPress();
        void onActionUp();
    }
}
