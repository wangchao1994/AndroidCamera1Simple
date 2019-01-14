package com.google.android.cameraview;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import com.example.cameraview.utils.CameraUtils;
import com.example.cameraview.video.VideoManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import static android.content.ContentValues.TAG;
public class Camera1 extends CameraViewImpl{

    private int mCameraId;
    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private final SizeMap mPreviewSizes = new SizeMap();
    private final SizeMap mPictureSizes = new SizeMap();
    private AspectRatio mAspectRatio;
    private boolean mShowingPreview;
    private boolean mAutoFocus;
    private int mFacing;
    private int mFlash;
    private int mDisplayOrientation;
    private Size size;
    private final SizeMap mVideoSizes = new SizeMap();
    private Camera.Size optimalVideoSize;
    private float mZoomValues = Constants.ZOOM_VALUE;
    private int maxZoom;
    private VideoManager mVideoManager;
    private byte[][] mPreviewCallbackBuffers = new byte[CACHE_BUFFER_NUM][];
    private final static int CACHE_BUFFER_NUM = 3;

    public Camera1(Callback callback, PreviewImpl preview) {
        super(callback, preview);
        preview.setCallback(new PreviewImpl.Callback() {
            @Override
            public void onSurfaceChanged() {
                if (mCamera != null) {
                    setUpPreview();
                    adjustCameraParameters();
                }
            }
        });
        if (mVideoManager == null){
            mVideoManager = new VideoManager(this);
        }
    }

    @Override
    public boolean start() {
        mCameraId = CameraUtils.chooseCamera(mCameraInfo,mFacing);
        openCamera();
        if (mPreview.isReady()) {
            setUpPreview();
        }
        mShowingPreview = true;
        mCamera.startPreview();
        return true;
    }

    // Suppresses Camera#setPreviewTexture
    private void setUpPreview() {
        try {
            if (mPreview.getOutputClass() == SurfaceHolder.class) {
                final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
                if (needsToStopPreview) {
                    mCamera.stopPreview();
                }
                mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                if (needsToStopPreview) {
                    mCamera.startPreview();
                }
            } else {
                mCamera.setPreviewTexture((SurfaceTexture) mPreview.getSurfaceTexture());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mShowingPreview = false;
        releaseCamera();
    }
    @Override
    public boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    public void setFacing(int facing) {
        if (mFacing == facing) {
            return;
        }
        mFacing = facing;
        if (isCameraOpened()) {
            stop();
            start();
        }
    }

    @Override
    public int getFacing() {
        return mFacing;
    }

    @Override
    public Set<AspectRatio> getSupportedAspectRatios() {
        SizeMap idealAspectRatios = mPreviewSizes;
        for (AspectRatio aspectRatio : idealAspectRatios.ratios()) {
            if (mPictureSizes.sizes(aspectRatio) == null) {
                idealAspectRatios.remove(aspectRatio);
            }
        }
        return idealAspectRatios.ratios();
    }

    @Override
    public boolean setAspectRatio(AspectRatio ratio) {
        if (mAspectRatio == null || !isCameraOpened()) {
            // Handle this later when camera is opened
            mAspectRatio = ratio;
            return true;
        } else if (!mAspectRatio.equals(ratio)) {
            final Set<Size> sizes = mPreviewSizes.sizes(ratio);
            if (sizes == null) {
                throw new UnsupportedOperationException(ratio + " is not supported");
            } else {
                mAspectRatio = ratio;
                adjustCameraParameters();
                return true;
            }
        }
        return false;
    }

    @Override
    public AspectRatio getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    public void setAutoFocus(boolean autoFocus) {
        if (mAutoFocus == autoFocus) {
            return;
        }
        if (setAutoFocusInternal(autoFocus)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    public boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return mAutoFocus;
        }
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    @Override
    public void setFlash(int flash) {
        if (flash == mFlash) {
            return;
        }
        if (setFlashInternal(flash)) {
            mCamera.setParameters(mCameraParameters);
        }
    }
    @Override
    public int getFlash() {
        return mFlash;
    }

    @Override
    public void takePicture() {
        if (!isCameraOpened()) {
            throw new IllegalStateException(
                    "Camera is not ready. Call start() before takePicture().");
        }
        if (getAutoFocus()) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    takePictureInternal();
                }
            });
        } else {
            takePictureInternal();
        }
    }

    private void takePictureInternal() {
        if (!isPictureCaptureInProgress.getAndSet(true)) {
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    isPictureCaptureInProgress.set(false);
                    mCallback.onPictureTaken(data);
                    camera.cancelAutoFocus();
                    camera.startPreview();
                }
            });
        }
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
        if (mDisplayOrientation == displayOrientation) {
            return;
        }
        mDisplayOrientation = displayOrientation;
        if (isCameraOpened()) {
            mCameraParameters.setRotation(CameraUtils.calcCameraRotation(mCameraInfo,displayOrientation));
            mCamera.setParameters(mCameraParameters);
            final boolean needsToStopPreview = mShowingPreview /*&& Build.VERSION.SDK_INT < 14*/;
            if (needsToStopPreview) {
                mCamera.stopPreview();
            }
            mCamera.setDisplayOrientation(CameraUtils.calcDisplayOrientation(mCameraInfo,displayOrientation));
            if (needsToStopPreview) {
                mCamera.startPreview();
            }
        }
    }

    @Override
    public PreviewImpl getPreview() {
        return mPreview;
    }

    /**
     * 打开Camera
     */
    private void openCamera() {
        if (mCamera != null) {
            releaseCamera();
        }
        mCamera = Camera.open(mCameraId);
        mCameraParameters = mCamera.getParameters();
        // Supported preview sizes
        mPreviewSizes.clear();
        for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
            mPreviewSizes.add(new Size(size.width, size.height));
        }
        // Supported picture sizes;
        mPictureSizes.clear();
        for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
            mPictureSizes.add(new Size(size.width, size.height));
        }
        //// Supported picture sizes;
        mVideoSizes.clear();
        for (Camera.Size size : mCameraParameters.getSupportedVideoSizes()) {
            mVideoSizes.add(new Size(size.width, size.height));
        }
        //FlashModes
        List<String> supportedFlashModes = mCameraParameters.getSupportedFlashModes();
        for (int i = 0; i < supportedFlashModes.size(); i++) {
            Log.d("prepareVideoRecorder","supportedFlashModes===="+supportedFlashModes.get(i));
        }
        // AspectRatio
        if (mAspectRatio == null) {
            mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
        }
        adjustCameraParameters();
        mCamera.setDisplayOrientation(CameraUtils.calcDisplayOrientation(mCameraInfo,mDisplayOrientation));
        mCallback.onCameraOpened();
    }

    /**
     * 参数设置
     */
    private void adjustCameraParameters() {
        SortedSet<Size> sizes = mPreviewSizes.sizes(mAspectRatio);
        if (sizes == null) { // Not supported
            mAspectRatio = CameraUtils.chooseAspectRatio(mPreviewSizes);
            sizes = mPreviewSizes.sizes(mAspectRatio);
        }
        size = chooseOptimalSize(sizes);
        //add adjustCameraParameters
        optimalVideoSize = CameraUtils.getOptimalVideoSize(mCameraParameters.getSupportedPictureSizes(), mCameraParameters.getSupportedPreviewSizes(), size.getWidth(), size.getHeight());
        // Always re-apply camera parameters
        // Largest picture size in this ratio
        final Size pictureSize = mPictureSizes.sizes(mAspectRatio).last();
        if (mShowingPreview) {
            mCamera.stopPreview();
        }
        mCameraParameters.setPreviewSize(size.getWidth(), size.getHeight());
        mCameraParameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        mCameraParameters.setRotation(CameraUtils.calcCameraRotation(mCameraInfo,mDisplayOrientation));
        maxZoom = mCameraParameters.getMaxZoom();
        setAutoFocusInternal(mAutoFocus);
        setFlashInternal(mFlash);
        setZoomInternal(mZoomValues);
        mCamera.setParameters(mCameraParameters);
        mCamera.setPreviewCallback(mPreviewCallBack);
        Log.d("onPreviewFrame","onPreviewFrame----mWidth="+mPreview.mWidth+"   mHeight=="+mPreview.mHeight);
        /*int bufferSize = mPreview.mHeight * mPreview.mWidth * 3 / 2;
        for (int i = 0; i < mPreviewCallbackBuffers.length; i++) {
            if (mPreviewCallbackBuffers[i] == null) {
                mPreviewCallbackBuffers[i] = new byte[bufferSize];
            }
            mCamera.addCallbackBuffer(mPreviewCallbackBuffers[i]);
        }
        mCamera.setPreviewCallbackWithBuffer(mPreviewCallbackWithBuffer);*/
        if (mShowingPreview) {
            mCamera.startPreview();
        }
    }

    /**
     * Camera preview
     */
    Camera.PreviewCallback mPreviewCallBack = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.d("[onPreviewFrame]","onPreviewFrame--------mPreviewCallBack--------------->"+data.length);
            if (mCallback != null){
                mCallback.onPreviewFrame(data);
            }
        }
    };

   /* Camera.PreviewCallback mPreviewCallbackWithBuffer = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            //mCallback.onPreviewFrame(data);
            if (data == null) {
                Log.e(TAG, "[onPreviewFrame], data is null");
                return;
            }
            Log.d("[onPreviewFrame]","onPreviewFrame-------mPreviewCallbackWithBuffer-------------->"+data);
            mCamera.addCallbackBuffer(data);
        }
    };*/

    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        if (!mPreview.isReady()) { // Not yet laid out
            return sizes.first(); // Return the smallest size
        }
        int desiredWidth;
        int desiredHeight;
        final int surfaceWidth = mPreview.getWidth();
        final int surfaceHeight = mPreview.getHeight();
        if (CameraUtils.isLandscape(mDisplayOrientation)) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = null;
        for (Size size : sizes) { // Iterate from small to large
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                return size;

            }
            result = size;
        }
        return result;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            Log.d("wang_log","releaseCamera------------------------------------->");
            mCamera.setPreviewCallback(null);
            //mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
            mCamera = null;
            mCallback.onCameraClosed();
        }
    }
    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
        if (isCameraOpened()) {
            final List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            } else {
                mCameraParameters.setFocusMode(modes.get(0));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setFlashInternal(int flash) {
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFlashModes();
            String mode = CameraUtils.FLASH_MODES.get(flash);
            if (modes != null && modes.contains(mode)) {
                mCameraParameters.setFlashMode(mode);
                mFlash = flash;
                return true;
            }
            String currentMode =CameraUtils.FLASH_MODES.get(mFlash);
            if (modes == null || !modes.contains(currentMode)) {
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlash = Constants.FLASH_OFF;
                return true;
            }
            return false;
        } else {
            mFlash = flash;
            return false;
        }
    }
    /**
     * 返回当前Camera
     * @return
     */
    @Override
    public Camera getCurrentCamera() {
        if (mCamera != null){
            return mCamera;
        }
        return null;
    }

    @Override
    public Camera.Size getVideoSize() {
        return optimalVideoSize;
    }

    /**
     * 获取支持最大的Zoom值
     * @return
     */
    @Override
    public int getMaxZoom(){
        return maxZoom;
    }
    /**
     * 设置缩放值
     * @param zoomValues
     */
    @Override
    public void setZoom(float zoomValues) {
        if (mZoomValues == zoomValues){
            return;
        }
        if (setZoomInternal(zoomValues)){
            mCamera.setParameters(mCameraParameters);
        }
    }
    private boolean setZoomInternal(float zoomValues) {
        mZoomValues = zoomValues;
        if (isCameraOpened()) {
            mCameraParameters.setZoom((int) zoomValues);
            return true;
        }
        return false;
    }
    @Override
    public float getZoom() {
        return mZoomValues;
    }
    //录像 start------------------------------------------------------
    @Override
    public void startRecording() {
        mVideoManager.startRecording();
    }

    @Override
    public void stopRecording() {
        mVideoManager.stopRecording();
    }

    @Override
    public boolean isRecording() {
        return mVideoManager.isRecording();
    }
    //录像 end------------------------------------------------------
    @Override
    public int getCameraId(){
        return mCameraId;
    }

    @Override
    public Camera.CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    @Override
    public String getNextVideoPath() {
        if (mVideoManager.getNextVideoAbsolutePath() != null){
            return mVideoManager.getNextVideoAbsolutePath();
        }
        return null;
    }

    @Override
    public boolean isZoomSupported() {
        return mCameraParameters.isZoomSupported();
    }

    @Override
    public void handleFocus(MotionEvent event) {
        int viewWidth = mPreview.mWidth;
        int viewHeight =  mPreview.mHeight;
        Rect focusRect = CameraUtils.calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
        Rect meteringRect = CameraUtils.calculateTapArea(event.getX(), event.getY(), 1.5f, viewWidth, viewHeight);
        mCamera.cancelAutoFocus();
        Log.d(TAG,"mCameraParameters.getMaxNumFocusAreas()==="+mCameraParameters.getMaxNumFocusAreas());
        Log.d(TAG,"mCameraParameters.getMaxNumMeteringAreas()==="+mCameraParameters.getMaxNumMeteringAreas());
        if (mCameraParameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            mCameraParameters.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }
        if (mCameraParameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            mCameraParameters.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
        final String currentFocusMode = mCameraParameters.getFocusMode();
        //mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        mCamera.setParameters(mCameraParameters);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }

}
