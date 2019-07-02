package com.example.wangchao.androidcamera1view;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cameraview.CameraView;
import com.example.cameraview.ui.ICameraUI;
import com.example.cameraview.utils.CameraUtils;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseApplication;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.controller.CameraContract;
import com.example.wangchao.androidcamera1view.camera.event.GlobalAction;
import com.example.wangchao.androidcamera1view.presenter.CameraPresenter;
import com.example.wangchao.androidcamera1view.utils.glide.GlideLoader;
import com.google.android.cameraview.AspectRatio;

/**
 * Main CameraView
 */
public class CameraFragment extends Fragment implements CameraContract.CameraViewCall ,View.OnClickListener,ICameraUI.OnGestureListener{

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
    private ImageView mCameraViewZoom;
    private ImageView mCameraViewFoucs;

    private final int[] FLASH_OPTIONS = {
            CameraUtils.FLASH_AUTO,
            CameraUtils.FLASH_OFF,
            CameraUtils.FLASH_ON,
    };

    private final int[] FLASH_ICONS = {
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_on,
    };
    private AlertDialog aAspectRatioAlertDialog;
    private int mAspectRatioItem;
    private float mSupportMaxZoom;

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance(ICameraImpl iCamera) {
        mICameraImpl = iCamera;
        return new CameraFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraPresenter = mICameraImpl.getCameraPresenter();
        mCameraManager = mICameraImpl.getCameraManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootCameraView = inflater.inflate(R.layout.fragment_camera, container, false);
        initView(mRootCameraView);
        return mRootCameraView;
    }

    private void initView(View mRootCameraView) {
        mCameraView = mRootCameraView.findViewById(R.id.camera_view);
        mCameraView.getUIEventGlobal().setOnGestureListener(this);
        mBtnTakePicture = mRootCameraView.findViewById(R.id.fb_take_picture);
        mBtnVideoRecord = mRootCameraView.findViewById(R.id.fb_video_recording);
        mCameraFlashAuto = mRootCameraView.findViewById(R.id.iv_flash_switch);
        mCameraIdSwitch = mRootCameraView.findViewById(R.id.iv_camera_switch);
        mCameraAspectRadio = mRootCameraView.findViewById(R.id.iv_aspect_switch);
        mCameraViewThumb = mRootCameraView.findViewById(R.id.iv_last_thumb);
        mCamerViewTvShowTime = mRootCameraView.findViewById(R.id.tv_show_time_view);
        mCameraViewZoom = mRootCameraView.findViewById(R.id.iv_add_zoom);
        mCameraViewFoucs = mRootCameraView.findViewById(R.id.iv_change_focus);
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
        mCameraViewZoom.setOnClickListener(this);
        mCameraViewFoucs.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //获取上一次文件路径
        mFilePath = mCameraManager.getRecentlyPhotoPath(BaseApplication.getInstance());
        if (mCameraPresenter != null){
            mCameraPresenter.onResume();
            mCameraPresenter.setPictureCallBack();
            mCameraPresenter.setRecentlyPhotoPath(mCameraManager.getRecentlyPhotoPath(BaseApplication.getInstance()));
            mSupportMaxZoom = mCameraPresenter.getMaxZoom();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraPresenter != null){
            //释MediaRecorder
            mCameraPresenter.onReleaseRecord();
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
    public ImageView getCameraThumbView() {
        if (mCameraViewThumb != null){
            return mCameraViewThumb;
        }
        return null;
    }

    @Override
    public void loadPictureResult(String filePath) {
        mFilePath = filePath;
        Log.d("loadPictureResult","filePath-------------"+filePath);
        GlideLoader.loadNetWorkResource(BaseApplication.getInstance(),filePath,mCameraViewThumb);
        //delayed
        if (mICameraImpl.getGlobalHandler() != null) {
            mICameraImpl.getGlobalHandler().sendEmptyMessageDelayed(GlobalAction.SAVE_VIDEO_DIALOG_DISMISS,1000);
        }
    }
    /**
     * 时间显示
     * @param timing
     */
    @Override
    public void setTimeShow(String timing) {
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
                if (mCameraPresenter != null){
                    mCameraPresenter.setViewShowOrHide(mCamerViewTvShowTime,true);
                    mCameraPresenter.setViewShowOrHide(mViewRecordController,true);
                    mCameraPresenter.setViewShowOrHide(mCameraAspectRadio,false);
                    mCameraPresenter.setViewShowOrHide(mCameraFlashAuto,false);
                    mCameraPresenter.setViewShowOrHide(mCameraIdSwitch,false);
                    mCameraPresenter.setViewShowOrHide(mCameraViewZoom,false);
                    mCameraPresenter.setViewShowOrHide(mCameraViewFoucs,false);
                }
                break;
            //录制暂停
            case CameraContract.CameraViewCall.MODE_RECORD_STOP:
                Log.d("camera_log","录制暂停------------>");
                break;
            //录制完成
            case CameraContract.CameraViewCall.MODE_RECORD_FINISH:
                Log.d("camera_log","录制完成------------>");
                mCamerViewTvShowTime.setText("");
                if (mCameraPresenter != null){
                    mCameraPresenter.setViewShowOrHide(mCamerViewTvShowTime,false);
                    mCameraPresenter.setViewShowOrHide(mViewRecordController,false);
                    mCameraPresenter.setViewShowOrHide(mCameraAspectRadio,true);
                    mCameraPresenter.setViewShowOrHide(mCameraFlashAuto,true);
                    mCameraPresenter.setViewShowOrHide(mCameraIdSwitch,true);
                    mCameraPresenter.setViewShowOrHide(mCameraViewZoom,true);
                    mCameraPresenter.setViewShowOrHide(mCameraViewFoucs,true);
                }
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
                    Log.d("wangchao_camera","isCameraBack------------------------ 1 "+cameraFacingId);
                    mCameraPresenter.switchCameraId(cameraFacingId == CameraUtils.FACING_FRONT ?CameraUtils.FACING_BACK : CameraUtils.FACING_FRONT);
                }
                break;
            case R.id.iv_aspect_switch:
                String[] aspectRatios = {"4:3","16:9"};
                AspectRatio currentSupportAspectRatio = mCameraManager.getCurrentSupportAspectRatio();
                String mAspectRatio = currentSupportAspectRatio.toString();
                if (mAspectRatio.equals(aspectRatios[0])){
                    mAspectRatioItem = 0;
                }else if (mAspectRatio.equals(aspectRatios[1])){
                    mAspectRatioItem = 1;
                }
                AlertDialog.Builder builder =new AlertDialog.Builder(getActivity())
                        .setTitle("")
                        .setSingleChoiceItems(aspectRatios, mAspectRatioItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        if (mCameraPresenter != null){
                                            mCameraPresenter.setCurrentAspectRatio(AspectRatio.parse("4:3"));
                                        }
                                        break;
                                    case 1:
                                        if (mCameraPresenter != null){
                                            mCameraPresenter.setCurrentAspectRatio(AspectRatio.parse("16:9"));
                                        }
                                        break;
                                }
                                aAspectRatioAlertDialog.dismiss();
                            }
                        });
                aAspectRatioAlertDialog = builder.create();
                aAspectRatioAlertDialog.show();

                break;
            case R.id.iv_last_thumb:
                Log.d("camera_log","last FilePath--------------->mFilePath="+mFilePath);
                if (mFilePath != null && !"".equals(mFilePath)){
                    CameraUtils.OnIntentGallery(BaseApplication.getInstance(),mFilePath);
                }
                break;
            case R.id.iv_add_zoom:
                float zoomValues = mCameraPresenter.getZoom();
                if (zoomValues == mSupportMaxZoom){
                    zoomValues = 0.0f;
                }
                zoomValues += 1.0f;
                Log.d("onResume","zoomValues-------------->"+zoomValues);
                mCameraPresenter.setZoom(zoomValues);
                break;
            case R.id.iv_change_focus:
                Log.d("camera_log","--------------->mCameraPresenter.getFocusMode()="+mCameraPresenter.getFocusMode());
                boolean focusMode = mCameraPresenter.getFocusMode();
                mCameraPresenter.setFocusMode(!focusMode);
                break;
            case R.id.iv_video_controller:
                int mode = (int) mViewRecordController.getTag();
                if (mode == CameraContract.CameraViewCall.MODE_RECORD_START) { //录制状态中，可以暂停
                    Log.d("camera_log","录制状态中--------------->暂停");
                    mCameraPresenter.stopRecord();
                    mViewRecordController.setImageResource(R.drawable.recording_play);
                }else if (mode == CameraContract.CameraViewCall.MODE_RECORD_STOP) {//暂停状态，可以继续开始录制
                    mCameraPresenter.restartRecord();
                    mViewRecordController.setImageResource(R.drawable.recording_pause);
                    Log.d("camera_log","暂停状态---------------->继续开始录制");
                }
                break;
        }
    }

    @Override
    public boolean onSingleTap(MotionEvent event) {
        if (null == mCameraView) {
            return false;
        }
        mCameraPresenter.focusOnTouch(event);
        Log.d("onSingleTap--->","---------------mCameraView.getWidth()="+mCameraView.getWidth()+"   mCameraView.getHeight()="+mCameraView.getHeight());
        return false;
    }

    @Override
    public void onScale(float factor) {
        Log.d("onScale--->","----------------------mSupportMaxZoom----"+mSupportMaxZoom);
        if (mCameraPresenter != null){
            float zoomValues = mCameraPresenter.getZoom();
            if (factor >= 1.0f){
                if (zoomValues >=1.0f){
                    zoomValues += 0.1f;
                    if (zoomValues >mSupportMaxZoom){
                        zoomValues = mSupportMaxZoom;
                    }
                }
            }else{
                if (zoomValues >1.0f){
                    zoomValues -= 0.1f;
                    if (zoomValues < 1.0f){
                        zoomValues = 1.0f;
                    }
                }
            }
            mCameraPresenter.setZoom(zoomValues);
        }
    }

    @Override
    public void showPress() {
        Log.d("onScale--->","---------showPress-----------------");
    }

    @Override
    public void onLongPress() {
        Log.d("onScale--->","--------------onLongPress------------");
    }

    @Override
    public void onActionUp() {
        Log.d("onScale--->","--------------onActionUp------------");
    }
}
