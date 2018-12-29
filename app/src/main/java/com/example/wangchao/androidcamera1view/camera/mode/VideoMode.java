package com.example.wangchao.androidcamera1view.camera.mode;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.example.cameraview.CameraView;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseApplication;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.example.wangchao.androidcamera1view.utils.CameraUtils;
import com.example.wangchao.androidcamera1view.utils.rxjava.ObservableBuilder;
import com.google.android.cameraview.AspectRatio;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class VideoMode extends CameraModeBase {
    public static final String TAG = VideoMode.class.getSimpleName();
    private ICameraImpl mICameraImpl;
    private CameraManager mCameraManager;
    /**
     * 当前是否是在录制视频
     */
    private boolean mIsRecordingVideo;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private CompositeSubscription compositeSubscription;
    private List<String> oldVideoPath;
    /**
     * 点击开启录制时候创建的新视频文件路径
     */
    private String mNextVideoAbsolutePath;

    public VideoMode(ICameraImpl iCameraIml) {
        mICameraImpl = iCameraIml;
        mCameraManager = mICameraImpl.getCameraManager();
        oldVideoPath = new CopyOnWriteArrayList<>();
        compositeSubscription = new CompositeSubscription();

    }

    @Override
    public void switchCameraId(int cameraId) {
    }

    @Override
    public int getCameraFacingId() {
        return 0;
    }

    @Override
    public void openCamera() {
        CameraView cameraView = getCameraView();
        if (cameraView != null) {
            cameraView.start();
        }
    }

    @Override
    public void stopOperate() {
        CameraView cameraView = getCameraView();
        releaseMediaRecorder();//释放MediaRecorder
        if (cameraView != null) {
            cameraView.stop();
        }
    }

    @Override
    public void startOperate() {
        CameraView cameraView = getCameraView();
        if (cameraView != null) {
            cameraView.start();
        }
    }

    @Override
    public void setAutoFlash(int autoFlash) {
    }

    @Override
    public Set<AspectRatio> getSupportedAspectRatios() {
        return null;
    }

    @Override
    public AspectRatio getCurrentAspectRatio() {
        return null;
    }

    @Override
    public void cameraPhotoClick() {
    }

    @Override
    public void addCameraPictureCallBack() {
        CameraView cameraView = getCameraView();
        if (cameraView != null) {
            cameraView.addCallback(mPictureCallback);
        }
    }

    @Override
    protected void writePictureData(byte[] data) {
    }

    @Override
    public void cameraVideoClick() {
        if (mIsRecordingVideo) {
            stopRecordingVideo(true);
        } else {
            startRecordingVideo();
        }
    }


    /**
     * 结束录像
     *
     * @param isFinishing
     */
    private void stopRecordingVideo(final boolean isFinishing) {
        mIsRecordingVideo = false;
        Subscription subscription = Observable
                //延迟三十毫秒
                .timer(30, TimeUnit.MICROSECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        // 停止录制
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        if (isFinishing) {
                            mIsRecordingVideo = false;
                            Log.i(TAG, "stopRecordingVideo recording complete--------");
                            if (mCameraVideoResultCallBack != null) {
                                mCameraVideoResultCallBack.finishRecord();
                            }
                            mergeMultipleFileCallBack();
                            mNextVideoAbsolutePath = null;
                            oldVideoPath.clear();
                        } else {//暂停的操作
                            Log.i(TAG, "pauseRecordingVideo recording stop--------");
                            //若是开始新的录制，原本暂停产生的多个文件合并成一个文件。
                            oldVideoPath.add(mNextVideoAbsolutePath);
                            if (oldVideoPath.size() > 1) {
                                mergeMultipleFile();
                            }
                            mNextVideoAbsolutePath = null;
                        }
                       // startPreview();
                    }
                });
        compositeSubscription.add(subscription);
    }
    /**
     * 完成录制，输出最终的视频录制文件
     */
    private void mergeMultipleFileCallBack() {
        if (oldVideoPath.size() > 0) {
            Log.i(TAG, " mergeMultipleFileCallBack file.size()===" + oldVideoPath.size());
            Subscription subscription = ObservableBuilder.createMergeMuiltFile(BaseApplication.getInstance(), oldVideoPath.get(0), mNextVideoAbsolutePath)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            if (mCameraPitureResultCallBack != null) {
                                mCameraPitureResultCallBack.callPictureBack(ObservableBuilder.createVideo(s));
                            }
                            Log.i(TAG, " mergeMultipleFileCallBack--------success-------------");
                            //ToastUtils.showToast(BaseApplication.getInstance(), "视频文件保存路径:" + s);
                            Log.d(TAG,"视频文件保存路径======"+s);
                        }
                    });
            compositeSubscription.add(subscription);
        } else {
            if (mCameraPitureResultCallBack != null) {
                mCameraPitureResultCallBack.callPictureBack(ObservableBuilder.createVideo(mNextVideoAbsolutePath));
            }
            Log.d(TAG,"视频文件保存在======"+mNextVideoAbsolutePath);
            // ToastUtils.showToast(BaseApplication.getInstance(), "视频文件保存在" + mNextVideoAbsolutePath);
        }
    }
    /**
     * 暂停后又从新恢复录制，合并多个视频文件
     */
    private void mergeMultipleFile() {
        Log.i(TAG, " mergeMultipleFile  开始操作：文件个数 " + oldVideoPath.size());
        Subscription subscription = ObservableBuilder.createMergeMuiltFile(BaseApplication.getInstance(), oldVideoPath.get(0), oldVideoPath.get(1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String filePath) {
                        oldVideoPath.clear();
                        oldVideoPath.add(filePath);
                        Log.i(TAG, " mergeMultipleFile  完成： 文件个数" + oldVideoPath.size());
                    }
                });

        compositeSubscription.add(subscription);
    }
    /**
     * 暂停录制
     */
    public void pauseRecordingVideo() {
        stopRecordingVideo(false);
    }
    /**
     * 开始视频录制
     */
    private void startRecordingVideo() {
        CameraView cameraView = getCameraView();
        mCamera = cameraView.getCurrentCamera();
        if (null == mCamera) {
            return;
        }
        new MediaPrepareTask().execute(null, null, null);
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();
                mIsRecordingVideo = true;
                if (mCameraVideoResultCallBack != null){
                   mCameraVideoResultCallBack.startRecord();
                }
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("camera_log","MediaPrepareTask onPostExecute result===="+result);
        }
    }
    /**
     * 录像参数准备
     */
    public boolean prepareVideoRecorder() {
        // BEGIN_INCLUDE (configure_preview)
        CameraView cameraView = getCameraView();
        //mCamera = CameraHelper.getDefaultCameraInstance();
        mCamera = cameraView.getCurrentCamera();
        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraUtils.getOptimalVideoSize(mSupportedVideoSizes, mSupportedPreviewSizes, cameraView.getWidth(), cameraView.getHeight());
        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            //mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
            mCamera.setPreviewDisplay(cameraView.getSurfaceHolder());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)

        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);
        // Step 4: Set output file
        File mOutputFile = CameraUtils.getOutputMediaFile(CameraUtils.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        mNextVideoAbsolutePath= mOutputFile.getPath();
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
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
    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }
}
