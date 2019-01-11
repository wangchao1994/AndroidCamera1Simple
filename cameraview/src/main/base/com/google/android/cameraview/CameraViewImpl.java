package com.google.android.cameraview;

import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.View;
import java.util.Set;

public abstract class CameraViewImpl {

    protected final Callback mCallback;

    protected final PreviewImpl mPreview;

    CameraViewImpl(Callback callback, PreviewImpl preview) {
        mCallback = callback;
        mPreview = preview;
    }

    public View getView() {
        return mPreview.getView();
    }

    /**
     * @return {@code true} if the implementation was able to start the camera session.
     */
    public abstract boolean start();

    public abstract void stop();

    public abstract boolean isCameraOpened();

    public abstract void setFacing(int facing);

    public abstract int getFacing();

    public abstract Set<AspectRatio> getSupportedAspectRatios();

    /**
     * @return {@code true} if the aspect ratio was changed.
     */
    public abstract boolean setAspectRatio(AspectRatio ratio);

    public abstract AspectRatio getAspectRatio();

    public abstract void setAutoFocus(boolean autoFocus);

    public abstract boolean getAutoFocus();

    public  abstract void setFlash(int flash);

    public abstract int getFlash();

    public  abstract void takePicture();

    public abstract void setDisplayOrientation(int displayOrientation);

    public interface Callback {

        void onCameraOpened();

        void onCameraClosed();

        void onPictureTaken(byte[] data);

        void onPreviewFrame(byte[] data);
    }

    public abstract PreviewImpl getPreview();
    public abstract int getCameraId();
    public abstract Camera.CameraInfo getCameraInfo();
    public abstract int getMaxZoom();
    public abstract void setZoom(float zoomValues);
    public abstract float getZoom();
    public abstract Camera getCurrentCamera();
    public abstract Camera.Size getVideoSize();
    public abstract void startRecording();
    public abstract void stopRecording();
    public abstract boolean isRecording();
    public abstract String getNextVideoPath();
    public abstract boolean isZoomSupported();
    public abstract void handleFocus(MotionEvent event);

}
