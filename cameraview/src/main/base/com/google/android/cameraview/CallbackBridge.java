package com.google.android.cameraview;

import com.example.cameraview.CameraView;

import java.util.ArrayList;

public class CallbackBridge implements CameraViewImpl.Callback {

    private final ArrayList<Callback> mCallbacks = new ArrayList<>();
    private boolean mRequestLayoutOnOpen;
    private CameraView mCameraView;
    public CallbackBridge(CameraView cameraView) {
        mCameraView = cameraView;
    }
    public void add(Callback callback) {
        mCallbacks.add(callback);
    }
    public void remove(Callback callback) {
        mCallbacks.remove(callback);
    }
    @Override
    public void onCameraOpened() {
        if (mRequestLayoutOnOpen) {
            mRequestLayoutOnOpen = false;
            mCameraView.requestLayout();
        }
        for (Callback callback : mCallbacks) {
            callback.onCameraOpened(mCameraView);
        }
    }
    @Override
    public void onCameraClosed() {
        for (Callback callback : mCallbacks) {
            callback.onCameraClosed(mCameraView);
        }
    }
    @Override
    public void onPictureTaken(byte[] data) {
        for (Callback callback : mCallbacks) {
            callback.onPictureTaken(mCameraView, data);
        }
    }
    public void reserveRequestLayoutOnOpen() {
        mRequestLayoutOnOpen = true;
    }
    /**
     * Callback for monitoring events about {@link CameraView}.
     */
    @SuppressWarnings("UnusedParameters")
    public abstract static class Callback {

        /**
         * Called when camera is opened.
         *
         * @param cameraView The associated {@link CameraView}.
         */
        public void onCameraOpened(CameraView cameraView) {
        }

        /**
         * Called when camera is closed.
         *
         * @param cameraView The associated {@link CameraView}.
         */
        public void onCameraClosed(CameraView cameraView) {
        }
        /**
         * Called when a picture is taken.
         *
         * @param cameraView The associated {@link CameraView}.
         * @param data       JPEG data.
         */
        public void onPictureTaken(CameraView cameraView, byte[] data) {
        }
    }

}