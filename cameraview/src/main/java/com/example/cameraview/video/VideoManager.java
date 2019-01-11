package com.example.cameraview.video;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.example.cameraview.utils.CameraUtils;
import com.example.cameraview.utils.file.FileUtils;
import com.google.android.cameraview.CameraViewImpl;
import java.io.IOException;
/**
 * 视频录制 Camera1
 */
public class VideoManager {
    public static final String TAG = VideoManager.class.getSimpleName();
    private Camera.Size optimalVideoSize;
    private CameraViewImpl mCameraViewImpl;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private String mNextVideoAbsolutePath;

    public VideoManager(CameraViewImpl cameraView){
        mCameraViewImpl = cameraView;
    }
    public void startRecording(){
        if (mCamera == null){
            mCamera = mCameraViewImpl.getCurrentCamera();
        }
        if (optimalVideoSize == null){
            optimalVideoSize = mCameraViewImpl.getVideoSize();
        }
        new MediaPrepareTask().execute(null, null, null);
    }

    public void stopRecording(){
        if (mMediaRecorder != null) {
            Log.d(TAG,"stopRecording--------mMediaRecorder.stop();-------");
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
        releaseMediaRecorder();
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
        Log.d(TAG,"mOptVideoWidth="+optimalVideoSize.width+"   mOptVideoHeight="+optimalVideoSize.height);
        mMediaRecorder.setVideoSize(optimalVideoSize.width, optimalVideoSize.height);
        mNextVideoAbsolutePath = FileUtils.createVideoDiskFile(mCameraViewImpl.getView().getContext(), FileUtils.createVideoFileName()).getAbsolutePath();
        Log.d(TAG,"mNextVideoAbsolutePath="+mNextVideoAbsolutePath);
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setPreviewDisplay(mCameraViewImpl.getPreview().getSurface());
        int rotateDegree = CameraUtils.getRotateDegree(mCameraViewImpl.getView().getContext(),mCameraViewImpl.getCameraId(),mCameraViewImpl.getCameraInfo());
        Log.d(TAG,"rotateDegree=============="+rotateDegree);
        mMediaRecorder.setOrientationHint(rotateDegree);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
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
            Log.d(TAG,"result---------------->"+result);
        }

        @Override
        protected void onPreExecute() {
            //surfaceView TextureView
            Log.d(TAG,"onPreExecute-------------------------------->");
            View currentView = mCameraViewImpl.getPreview().getView();
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
    /**
     * 是否正在录像
     * @return
     */
    public boolean isRecording(){
        return mMediaRecorder != null;
    }
    /**
     * 获取视频输出路径
     * @return
     */
    public String getNextVideoAbsolutePath(){
        return mNextVideoAbsolutePath;
    }
}
