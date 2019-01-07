/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.example.cameraview.utils.CameraUtils;
import com.example.cameraview.utils.file.FileUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import static android.content.ContentValues.TAG;
@SuppressWarnings("deprecation")
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
    private MediaRecorder mMediaRecorder;
    private Size size;
    private String mNextVideoAbsolutePath;
    private final SizeMap mVideoSizes = new SizeMap();
    private Camera.Size optimalVideoSize;
    private float mZoomValues = Constants.ZOOM_VALUE;
    private boolean isAELock;
    private int maxZoom;
    private Point mPointFocusArea;

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
    @Override
    public void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mShowingPreview = false;
        releaseCamera();
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
            final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
            if (needsToStopPreview) {
                mCamera.stopPreview();
            }
            mCamera.setDisplayOrientation(CameraUtils.calcDisplayOrientation(mCameraInfo,displayOrientation));
            if (needsToStopPreview) {
                mCamera.startPreview();
            }
        }
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
        if (mShowingPreview) {
            mCamera.startPreview();
        }
    }


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

    /**
     * 设置AE_LOCK
     * @param isLock
     */
    @Override
    public void setAELock(boolean isLock) {
        if (isAELock == isLock){
            return;
        }
        if (setAEInternal(isAELock)){
            mCamera.setParameters(mCameraParameters);
        }
    }

    private boolean setAEInternal(boolean aeLock) {
        isAELock = aeLock;
        if (isCameraOpened()) {
            mCameraParameters.setAutoExposureLock(aeLock);
            return true;
        }
        return false;
    }
    @Override
    public boolean getAELock() {
        return isAELock;
    }

    //录像 start------------------------------------------------------

    @Override
    public void startRecording() {
        new MediaPrepareTask().execute(null, null, null);
    }

    @Override
    public void stopRecording() {
        if (mMediaRecorder != null) {
            Log.d("prepareVideoRecorder","stopRecording--------mMediaRecorder.stop();-------");
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
        releaseMediaRecorder();
    }

    @Override
    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    @Override
    public String getNextVideoPath() {
        if (mNextVideoAbsolutePath != null){
            return mNextVideoAbsolutePath;
        }
        return null;
    }

    @Override
    public void setFocusArea(Point point) {
        if (mPointFocusArea == point){
            return;
        }
        if (setFocusAreaInternal(point)){
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    public boolean isZoomSupported() {
        if (mCameraParameters.isZoomSupported()){
            return true;
        }
        return false;
    }

    private boolean setFocusAreaInternal(Point point) {
        mPointFocusArea = point;
        if (isCameraOpened()) {
            List<Camera.Area> focusAera = getFocusAera(point);
            mCameraParameters.setFocusAreas(focusAera);
            return true;
        }
        return false;
    }
    @Override
    public void handleFocus(MotionEvent event) {
        int viewWidth = mPreview.mWidth;
        int viewHeight =  mPreview.mHeight;
        Rect focusRect = CameraUtils.calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
        Rect meteringRect = CameraUtils.calculateTapArea(event.getX(), event.getY(), 1.5f, viewWidth, viewHeight);
        mCamera.cancelAutoFocus();
        Log.d("singleTap","mCameraParameters.getMaxNumFocusAreas()==="+mCameraParameters.getMaxNumFocusAreas());
        Log.d("singleTap","mCameraParameters.getMaxNumMeteringAreas()==="+mCameraParameters.getMaxNumMeteringAreas());
        if (mCameraParameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            mCameraParameters.setFocusAreas(focusAreas);
        } else {
            Log.i("singleTap", "focus areas not supported");
        }
        if (mCameraParameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            mCameraParameters.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
        final String currentFocusMode = mCameraParameters.getFocusMode();
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
    /**
     * 获取当前聚焦区域
     * @param point
     * @return
     */
    private List<Camera.Area> getFocusAera(Point point) {
        List<Camera.Area> areas = new ArrayList<Camera.Area>();
        int left = point.x - 300;
        int top = point.y - 300;
        int right = point.x + 300;
        int bottom = point.y + 300;
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
        return areas;
    }

    /**
     * 参数设置
     * @return
     */
    private boolean prepareVideoRecorder() {
        if (mCamera == null) return false;
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        Log.d("prepareVideoRecorder","mOptVideoWidth="+optimalVideoSize.width+"   mOptVideoHeight="+optimalVideoSize.height);
        mMediaRecorder.setVideoSize(optimalVideoSize.width, optimalVideoSize.height);
        mNextVideoAbsolutePath = FileUtils.createVideoDiskFile(getView().getContext(), FileUtils.createVideoFileName()).getAbsolutePath();
        Log.d("prepareVideoRecorder","mNextVideoAbsolutePath="+mNextVideoAbsolutePath);
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setPreviewDisplay(mPreview.getSurface());
        int rotateDegree = CameraUtils.getRotateDegree(getView().getContext(),mCameraId,mCameraInfo);
        Log.d("prepareVideoRecorder","rotateDegree=============="+rotateDegree);
        mMediaRecorder.setOrientationHint(rotateDegree);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("prepareVideoRecorder", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("prepareVideoRecorder", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            if (prepareVideoRecorder()) {
                mMediaRecorder.start();
            } else {
                releaseMediaRecorder();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("onPostExecute","result---------------->"+result);
        }

        @Override
        protected void onPreExecute() {
            //surfaceView TextureView
            View currentView = mPreview.getCurrentView();
            if (currentView != null){
                currentView.setKeepScreenOn(true);
            }
        }
    }

    /**
     * 释放MediaRecorder
     */
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    //录像 end------------------------------------------------------
}
