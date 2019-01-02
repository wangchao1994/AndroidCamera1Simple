package com.example.wangchao.androidcamera1view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cameraview.CameraView;
import com.example.cameraview.utils.CameraUtils;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseApplication;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.controller.CameraContract;
import com.example.wangchao.androidcamera1view.presenter.CameraPresenter;
import com.example.wangchao.androidcamera1view.utils.glide.GlideLoader;

/**
 * Main CameraView
 */
public class CameraFragment extends Fragment implements CameraContract.CameraViewCall ,View.OnClickListener{

    public static final String TAG = CameraFragment.class.getSimpleName();

    private static ICameraImpl mICameraImpl;
    private FloatingActionButton mBtnTakePicture;
    private FloatingActionButton mBtnVideoRecord;
    private CameraView mCameraView;
    private View mRootCameraView;
    private CameraPresenter mCameraPresenter;
    private CameraManager mCameraManager;
    private ImageView mCameraFlashAuto;
    private ImageView mCameraIdSwitch;
    private ImageView mCameraAspectRadio;
    private int mCurrentFlash;
    private ImageView mCameraViewThumb;
    private String mFilePath;
    private TextView mCamerViewTvShowTime;
    private ImageView mViewRecordController;

    private final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private final int[] FLASH_ICONS = {
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_on,
    };

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance(ICameraImpl iCamera) {
        mICameraImpl = iCamera;
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraPresenter = mICameraImpl.getCameraPresenter();
        mCameraManager = mICameraImpl.getCameraManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootCameraView = inflater.inflate(R.layout.fragment_camera, container, false);
        initView(mRootCameraView);
        return mRootCameraView;
    }

    private void initView(View mRootCameraView) {
        mCameraView = mRootCameraView.findViewById(R.id.camera_view);
        mBtnTakePicture = mRootCameraView.findViewById(R.id.fb_take_picture);
        mBtnVideoRecord = mRootCameraView.findViewById(R.id.fb_video_recording);
        mCameraFlashAuto = mRootCameraView.findViewById(R.id.iv_flash_switch);
        mCameraIdSwitch = mRootCameraView.findViewById(R.id.iv_camera_switch);
        mCameraAspectRadio = mRootCameraView.findViewById(R.id.iv_aspect_switch);
        mCameraViewThumb = mRootCameraView.findViewById(R.id.iv_last_thumb);
        mCamerViewTvShowTime = mRootCameraView.findViewById(R.id.tv_show_time_view);
        mViewRecordController = mRootCameraView.findViewById(R.id.iv_video_controller);
        //录制状态标志
        mViewRecordController.setTag(CameraContract.CameraViewCall.MODE_RECORD_FINISH);
        mViewRecordController.setOnClickListener(this);
        mBtnTakePicture.setOnClickListener(this);
        mBtnVideoRecord.setOnClickListener(this);
        mCameraFlashAuto.setOnClickListener(this);
        mCameraIdSwitch.setOnClickListener(this);
        mCameraAspectRadio.setOnClickListener(this);
        mCameraViewThumb.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCameraPresenter != null){
            mCameraPresenter.onResume();
            mCameraPresenter.setPictureCallBack();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraPresenter != null){
            mCameraPresenter.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public CameraView getCameraView() {
        return mCameraView;
    }

    @Override
    public void loadPictureResult(String filePath) {
        mFilePath = filePath;
        Log.d("loadPictureResult","filePath-------------"+filePath);
        GlideLoader.loadNetWorkResource(BaseApplication.getInstance(),filePath,mCameraViewThumb);
    }

    /**
     * 时间显示
     * @param timing
     */
    @Override
    public void setTimeShow(String timing) {
        Log.d("camera_log","current time is------> "+timing);
        mCamerViewTvShowTime.setText(timing);
    }
    /**
     * 录制状态
     * @param  mode
     */
    @Override
    public void switchRecordMode(int mode) {
        switch (mode) {
            //录制开始
            case CameraContract.CameraViewCall.MODE_RECORD_START:
                Log.d("camera_log","录制开始------------>");
                break;
            //录制暂停
            case CameraContract.CameraViewCall.MODE_RECORD_STOP:
                Log.d("camera_log","录制暂停------------>");
                break;
            //录制完成
            case CameraContract.CameraViewCall.MODE_RECORD_FINISH:
                Log.d("camera_log","录制完成------------>");
                break;
            default:
                break;
        }
        mViewRecordController.setTag(mode);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fb_take_picture:
                if (mCameraPresenter!=null){
                    if (mCameraPresenter.getCameraMode() == CameraManager.MODE_CAMERA){
                        mCameraPresenter.takePictureOrVideo();
                    }else{
                        mCameraPresenter.switchCameraMode(CameraManager.MODE_CAMERA);
                    }
                }
                break;
            case R.id.fb_video_recording:
                if (mCameraPresenter!=null){
                    if (mCameraPresenter.getCameraMode() == CameraManager.MODE_VIDEO_RECORD){
                        mCameraPresenter.takePictureOrVideo();
                    }else{
                        mCameraPresenter.switchCameraMode(CameraManager.MODE_VIDEO_RECORD);
                    }
                }
                break;
            case R.id.iv_flash_switch:
                if (mCameraPresenter!=null){
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    mCameraFlashAuto.setImageResource(FLASH_ICONS[mCurrentFlash]);
                    mCameraPresenter.setFlashAuto(mCurrentFlash);
                }
                break;
            case R.id.iv_camera_switch:
                if (mCameraPresenter!=null){
                    //获取当前Id
                    int cameraFacingId = mCameraManager.getCameraFacingId();
                    mCameraPresenter.switchCameraId(cameraFacingId == CameraView.FACING_FRONT ?CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                break;
            case R.id.iv_aspect_switch:
                if (mCameraPresenter!=null){

                }
                break;
            case R.id.iv_last_thumb:
                if (mFilePath != null){
                    CameraUtils.OnIntentGallery(BaseApplication.getInstance(),mFilePath);
                }
                break;
            case R.id.iv_video_controller:
                int mode = (int) mBtnVideoRecord.getTag();
                if (mode == CameraContract.CameraViewCall.MODE_RECORD_START) { //录制状态中，可以暂停
                    Log.d("camera_log","录制状态中--------------->暂停");
                    mCameraPresenter.stopRecord();
                }else if (mode == CameraContract.CameraViewCall.MODE_RECORD_STOP) {//暂停状态，可以继续开始录制
                    mCameraPresenter.restartRecord();
                    Log.d("camera_log","暂停状态---------------->继续开始录制");
                }
                break;
        }
    }
}
