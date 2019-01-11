package com.example.cameraview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.FrameLayout;

import com.example.cameraview.ui.UIEventGlobal;
import com.example.cameraview.utils.CameraUtils;
import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CallbackBridge;
import com.google.android.cameraview.Camera1;
import com.google.android.cameraview.CameraViewImpl;
import com.google.android.cameraview.Constants;
import com.google.android.cameraview.PreviewImpl;
import com.google.android.cameraview.SurfaceViewPreview;
import com.google.android.cameraview.TextureViewPreview;
import java.util.Set;

public class CameraView extends FrameLayout {
    private static final String TAG = CameraView.class.getSimpleName();
    private PreviewImpl preview;
    private CameraViewImpl mImpl;
    private final CallbackBridge mCallbacks;
    private boolean mAdjustViewBounds;
    private final DisplayOrientationDetector mDisplayOrientationDetector;
    private UIEventGlobal mUIEventGlobal;

    public CameraView(Context context) {
        this(context, null);
    }
    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("WrongConstant")
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()){
            mCallbacks = null;
            mDisplayOrientationDetector = null;
            return;
        }
        // Internal setup
        preview = createPreviewImpl(context);
        mCallbacks = new CallbackBridge(this);
        //camera1/camera2自适应 change
        /*if (Build.VERSION.SDK_INT < 21) {
            mImpl = new Camera1(mCallbacks, preview);
        } else if (Build.VERSION.SDK_INT < 23) {
            mImpl = new Camera2(mCallbacks, preview, context);
        } else {
            Log.d("sdk_int","other------------------------------------");
            mImpl = new Camera2Api23(mCallbacks, preview, context);
        }*/
        mImpl = new Camera1(mCallbacks, preview);
        initUIEventLsn(context);
        // Attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraView, defStyleAttr,R.style.Widget_CameraView);
        mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
        setFacing(a.getInt(R.styleable.CameraView_facing, CameraUtils.FACING_BACK));
        String aspectRatio = a.getString(R.styleable.CameraView_aspectRatio);
        if (aspectRatio != null) {
            setAspectRatio(AspectRatio.parse(aspectRatio));
        } else {
            setAspectRatio(Constants.DEFAULT_ASPECT_RATIO);
        }
        setAutoFocus(a.getBoolean(R.styleable.CameraView_autoFocus, true));
        setFlash(a.getInt(R.styleable.CameraView_flash, Constants.FLASH_AUTO));
        //设置缩放
        setZoom(a.getFloat(R.styleable.CameraView_zoom, Constants.ZOOM_VALUE));
        a.recycle();
        // Display orientation detector
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                mImpl.setDisplayOrientation(displayOrientation);
            }
        };
    }

    @NonNull
    private PreviewImpl createPreviewImpl(Context context) {
        PreviewImpl preview;
        //camera1/camera2自适应 change
        if (Build.VERSION.SDK_INT < 14) {
            preview = new SurfaceViewPreview(context, this);
        } else {
            Log.d(TAG,"createPreviewImpl---------------------->");
            preview = new TextureViewPreview(context, this);
        }
        return preview;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mDisplayOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInEditMode()){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Handle android:adjustViewBounds
        if (mAdjustViewBounds) {
            if (!isCameraOpened()) {
                mCallbacks.reserveRequestLayoutOnOpen();
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                assert ratio != null;
                int height = (int) (MeasureSpec.getSize(widthMeasureSpec) * ratio.toFloat());
                if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
                }
                super.onMeasure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                final AspectRatio ratio = getAspectRatio();
                assert ratio != null;
                int width = (int) (MeasureSpec.getSize(heightMeasureSpec) * ratio.toFloat());
                if (widthMode == MeasureSpec.AT_MOST) {
                    width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        heightMeasureSpec);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        // Measure the TextureView
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        AspectRatio ratio = getAspectRatio();
        if (mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
            ratio = ratio.inverse();
        }
        assert ratio != null;
        if (height < width * ratio.getY() / ratio.getX()) {
            mImpl.getView().measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width * ratio.getY() / ratio.getX(),
                            MeasureSpec.EXACTLY));
        } else {
            mImpl.getView().measure(
                    MeasureSpec.makeMeasureSpec(height * ratio.getX() / ratio.getY(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.facing = getFacing();
        state.ratio = getAspectRatio();
        state.autoFocus = getAutoFocus();
        state.flash = getFlash();
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setFacing(ss.facing);
        setAspectRatio(ss.ratio);
        setAutoFocus(ss.autoFocus);
        setFlash(ss.flash);
    }

    /**
     * Open a camera device and start showing camera preview. This is typically called from
     * {@link Activity#onResume()}.
     */
    public void start() {
        if (!mImpl.start()) {
            //store the state ,and restore this state after fall back o Camera1
            Parcelable state=onSaveInstanceState();
            // Camera2 uses legacy hardware layer; fall back to Camera1
            mImpl = new Camera1(mCallbacks, createPreviewImpl(getContext()));
            onRestoreInstanceState(state);
            mImpl.start();
        }
    }

    /**
     * Stop camera preview and close the device. This is typically called from
     * {@link Activity#onPause()}.
     */
    public void stop() {
        mImpl.stop();
    }

    /**
     * @return {@code true} if the camera is opened.
     */
    public boolean isCameraOpened() {
        return mImpl.isCameraOpened();
    }

    /**
     * Add a new callback.
     *
     * @param callback The {@link CallbackBridge.Callback} to add.
     */
    public void addCallback(@NonNull CallbackBridge.Callback callback) {
        mCallbacks.add(callback);
    }

    /**
     * Remove a callback.
     *
     * @param callback The {@link CallbackBridge.Callback} to remove.
     * @see #addCallback(CallbackBridge.Callback)
     */
    public void removeCallback(@NonNull CallbackBridge.Callback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * @param adjustViewBounds {@code true} if you want the CameraView to adjust its bounds to
     *                         preserve the aspect ratio of camera.
     * @see #getAdjustViewBounds()
     */
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (mAdjustViewBounds != adjustViewBounds) {
            mAdjustViewBounds = adjustViewBounds;
            requestLayout();
        }
    }

    /**
     * @return True when this CameraView is adjusting its bounds to preserve the aspect ratio of
     * camera.
     * @see #setAdjustViewBounds(boolean)
     */
    public boolean getAdjustViewBounds() {
        return mAdjustViewBounds;
    }

    /**
     * Chooses camera by the direction it faces.
     * @param facing The camera facing.
     */
    public void setFacing(@CameraUtils.Facing int facing) {
        mImpl.setFacing(facing);
    }

    /**
     * Gets the direction that the current camera faces.
     *
     * @return The camera facing.
     */
    @CameraUtils.Facing
    public int getFacing() {
        //noinspection WrongConstant
        return mImpl.getFacing();
    }
    //add camera1-----------------
    /**
     * 获取当前Camera对象
     * @return
     */
    public Camera getCurrentCamera() {
        if (mImpl != null){
            return mImpl.getCurrentCamera();
        }
        return null;
    }
    /**
     * 获取Surface对象
     * @return
     */
    public Surface getSurface() {
        if (preview != null){
            return preview.getSurface();
        }
        return null;
    }

    /**
     * 判断当前是否正在录像
     * @return
     */
    public boolean isRecording() {
        if (mImpl != null){
            return mImpl.isRecording();
        }
        return false;
    }

    /**
     * 返回当前视频录像
     * @return
     */
    public String getNextVideoPath() {
        if (mImpl != null){
            return mImpl.getNextVideoPath();
        }
        return null;
    }
    /**
     * 开始录像
     */
    public void startRecording(){
        if (mImpl != null){
            Log.d("prepareVideoRecorder","startRecording--------------------------------------");
            mImpl.startRecording();
        }
    }
    /**
     * 停止录像
     */
    public void stopRecording(){
        if (mImpl != null){
            Log.d("prepareVideoRecorder","stopRecording--------------------------------------");
            mImpl.stopRecording();
        }
    }

    /**
     * Gets all the aspect ratios supported by the current camera.
     */
    public Set<AspectRatio> getSupportedAspectRatios() {
        return mImpl.getSupportedAspectRatios();
    }

    /**
     * Sets the aspect ratio of camera.
     *
     * @param ratio The {@link AspectRatio} to be set.
     */
    public void setAspectRatio(@NonNull AspectRatio ratio) {
        if (mImpl.setAspectRatio(ratio)) {
            requestLayout();
        }
    }

    /**
     * Gets the current aspect ratio of camera.
     *
     * @return The current {@link AspectRatio}. Can be {@code null} if no camera is opened yet.
     */
    @Nullable
    public AspectRatio getAspectRatio() {
        return mImpl.getAspectRatio();
    }

    /**
     * Enables or disables the continuous auto-focus mode. When the current camera doesn't support
     * auto-focus, calling this method will be ignored.
     *
     * @param autoFocus {@code true} to enable continuous auto-focus mode. {@code false} to
     *                  disable it.
     */
    public void setAutoFocus(boolean autoFocus) {
        mImpl.setAutoFocus(autoFocus);
    }

    /**
     * Returns whether the continuous auto-focus mode is enabled.
     *
     * @return {@code true} if the continuous auto-focus mode is enabled. {@code false} if it is
     * disabled, or if it is not supported by the current camera.
     */
    public boolean getAutoFocus() {
        return mImpl.getAutoFocus();
    }

    /**
     * Sets the flash mode.
     *
     * @param flash The desired flash mode.
     */
    public void setFlash(@CameraUtils.Flash int flash) {
        mImpl.setFlash(flash);
    }
    /**
     * Gets the current flash mode.
     *
     * @return The current flash mode.
     */
    @CameraUtils.Flash
    public int getFlash() {
        //noinspection WrongConstant
        return mImpl.getFlash();
    }

    public int getMaxZoom(){
        return mImpl.getMaxZoom();
    }
    /**
     * 设置zoom缩放
     * @param aFloat
     */
    public void setZoom(float aFloat) {
        mImpl.setZoom(aFloat);
    }
    /**
     * get zoomValues
     */
    public float getZoom() {
        return mImpl.getZoom();
    }
    /**
     * Take a picture. The result will be returned to
     * {@link CallbackBridge.Callback#onPictureTaken(CameraView, byte[])}.
     */
    public void takePicture() {
        mImpl.takePicture();
    }

    protected static class SavedState extends BaseSavedState {
        @CameraUtils.Facing
        private int facing;
        private AspectRatio ratio;
        private boolean autoFocus;
        @CameraUtils.Flash
        private int flash;
        @SuppressWarnings("WrongConstant")
        public SavedState(Parcel source, ClassLoader loader) {
            super(source);
            facing = source.readInt();
            ratio = source.readParcelable(loader);
            autoFocus = source.readByte() != 0;
            flash = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(facing);
            out.writeParcelable(ratio, 0);
            out.writeByte((byte) (autoFocus ? 1 : 0));
            out.writeInt(flash);
        }

        public static final Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        });
    }

    //缩放对焦事件监听---------------------------------------------------------------------
    /**
     * 初始化UI事件
     * @param context
     */
    private void initUIEventLsn(Context context) {
        mUIEventGlobal = new UIEventGlobal(context);
    }
    /**
     * 设置对焦和测光亮度
     * @param event
     */
    public void handleFocus(MotionEvent event){
        mImpl.handleFocus(event);
    }
    public UIEventGlobal getUIEventGlobal(){
        return mUIEventGlobal;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mUIEventGlobal.getGestureDetector().onTouchEvent(event) || mUIEventGlobal.getScaleGestureDetector().onTouchEvent(event)) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("singleTap","mImpl.isZoomSupported()==="+mImpl.isZoomSupported());
                if (null != mUIEventGlobal.getOnGestureListener() && mImpl.isZoomSupported()) {
                    mUIEventGlobal.getOnGestureListener().onActionUp();
                }
            }
            return true;
        }
        if (event.getPointerCount() > 1) {
            return true;
        }
        return super.onTouchEvent(event);
    }
    //缩放对焦事件监听---------------------------------------------------------------------
}
