package com.example.cameraview.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.view.Surface;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.Constants;
import com.google.android.cameraview.PreviewImpl;
import com.google.android.cameraview.SizeMap;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();
    private static final String MIMETYPE_EXTENSION_NULL = "unknown_ext_null_mimeType";

    private static final int INVALID_CAMERA_ID = -1;

    public static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    static {
        FLASH_MODES.put(Constants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(Constants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(Constants.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(Constants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(Constants.FLASH_RED_EYE, Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    /**-----------cameraview----------------------------*/
    /** The camera device faces the opposite direction as the device's screen. */
    public static final int FACING_BACK = Constants.FACING_BACK;

    /** The camera device faces the same direction as the device's screen. */
    public static final int FACING_FRONT = Constants.FACING_FRONT;


    /** Direction the camera faces relative to device screen. */
    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    /** Flash will not be fired. */
    public static final int FLASH_OFF = Constants.FLASH_OFF;

    /** Flash will always be fired during snapshot. */
    public static final int FLASH_ON = Constants.FLASH_ON;

    /** Constant emission of light during preview, auto-focus and snapshot. */
    public static final int FLASH_TORCH = Constants.FLASH_TORCH;

    /** Flash will be fired automatically when required. */
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    /** Flash will be fired in red-eye reduction mode. */
    public static final int FLASH_RED_EYE = Constants.FLASH_RED_EYE;

    /** The mode for for the camera device's flash control */
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    public @interface Flash {
    }
    /**-----------cameraview----------------------------*/

    /**
     * Calculate display orientation
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     *
     * This calculation is used for orienting the preview
     *
     * Note: This is not the same calculation as the camera rotation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees required to rotate preview
     */
    public static int calcDisplayOrientation(Camera.CameraInfo mCameraInfo,int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
        } else {  // back-facing
            return (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
    }

    /**
     * Calculate camera rotation
     *
     * This calculation is applied to the output JPEG either via Exif Orientation tag
     * or by actually transforming the bitmap. (Determined by vendor camera API implementation)
     *
     * Note: This is not the same calculation as the display orientation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees to rotate image in order for it to view correctly.
     */
    public static int calcCameraRotation(Camera.CameraInfo mCameraInfo ,int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        } else {  // back-facing
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    /**
     * Test if the supplied orientation is in landscape.
     *
     * @param orientationDegrees Orientation in degrees (0,90,180,270)
     * @return True if in landscape, false if portrait
     */
    public static boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == Constants.LANDSCAPE_90 ||
                orientationDegrees == Constants.LANDSCAPE_270);
    }

    /**
     * This rewrites cameraId.
     */
    public static int chooseCamera(Camera.CameraInfo mCameraInfo, int mFacing) {
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == mFacing) {
                return i;
            }
        }
        return INVALID_CAMERA_ID;
    }

    /**
     * select aspectration
     * @param mPreviewSizes
     * @return
     */
    public static AspectRatio chooseAspectRatio(SizeMap mPreviewSizes) {
        AspectRatio r = null;
        for (AspectRatio ratio : mPreviewSizes.ratios()) {
            r = ratio;
            if (ratio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                return ratio;
            }
        }
        return r;
    }
    /**
     * 获取旋转角度
     * @param context
     * @return
     */
    public static int getRotateDegree(Context context, int mCameraId, Camera.CameraInfo mCameraInfo){
        int phoneDegree = 0;
        int result = 0;
        //获得手机方向
        Activity activity = (Activity) context;
        int phoneRotate =activity.getWindowManager().getDefaultDisplay().getOrientation();
        //得到手机的角度
        switch (phoneRotate) {
            case Surface.ROTATION_0: phoneDegree = 0; break;        //0
            case Surface.ROTATION_90: phoneDegree = 90; break;      //90
            case Surface.ROTATION_180: phoneDegree = 180; break;    //180
            case Surface.ROTATION_270: phoneDegree = 270; break;    //270
        }
        //分别计算前后置摄像头需要旋转的角度
        if(mCameraId == 1){
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, mCameraInfo);
            result = (mCameraInfo.orientation + phoneDegree) % 360;
            result = (360 - result) % 360;
        }else{
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, mCameraInfo);
            result = (mCameraInfo.orientation - phoneDegree +360) % 360;
        }
        return result;
    }
    /**
     * 计算对焦区域
     * @param x
     * @param y
     * @param coefficient
     * @param width
     * @param height
     * @return
     */
    public static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }
    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
    /**
     * supported area
     * @param value
     * @param supported
     * @return
     */
    public static boolean isSupported(String value, List<String> supported) {
        return supported != null && supported.indexOf(value) >= 0;
    }
    /**
     * getSupportedVideoSizes
     * @param supportedVideoSizes
     * @param previewSizes
     * @param w
     * @param h
     * @return
     */
    public static Camera.Size getOptimalVideoSize(List<Camera.Size> supportedVideoSizes,
                                                  List<Camera.Size> previewSizes, int w, int h) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;

        // Supported video sizes list might be null, it means that we are allowed to use the preview
        // sizes
        List<Camera.Size> videoSizes;
        if (supportedVideoSizes != null) {
            videoSizes = supportedVideoSizes;
        } else {
            videoSizes = previewSizes;
        }
        Camera.Size optimalSize = null;

        // Start with max value and refine as we iterate over available video sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        int targetHeight = h;

        // Try to find a video size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Camera.Size size : videoSizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find video size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : videoSizes) {
                if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    /**
     * 通知图库更新图片
     * @param context
     * @param filePath
     */
    public static  void sendBroadcastNotify(Context context, String filePath){
        //扫描指定文件
        String action=Intent.ACTION_MEDIA_SCANNER_SCAN_FILE;
        //生成问价路径对应的uri
        Uri uri=Uri.fromFile(new File(filePath));
        Intent intent=new Intent(action);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 跳转系统Gallery
     * @param context
     * @param path
     */
    public static void OnIntentGallery(Context context,String path){
        Uri uri = null;
        String mimeType = getSystemMimeType(context,path);
        Log.d("OnIntentGallery","OnIntentGallery--------------------mimeType="+mimeType);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (mimeType.equals("video/mp4")){
            uri = Uri.parse(path);
        }else{//image/png
            uri = getItemContentUri(context,path);
        }
        if (uri!=null) {
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG,"ActivityNotFoundException-------exception="+e.getMessage());
        }
    }

    private static String getSystemMimeType(Context context, String path) {
        File mFile = new File(path);
        String fileName = mFile.getName();
        String extension = getFileExtension(fileName);
        if (extension == null) {
            return MIMETYPE_EXTENSION_NULL;
        }
        String mimeType = null;
        final String[] projection = {MediaStore.MediaColumns.MIME_TYPE};
        final String where = MediaStore.MediaColumns.DATA + " = ?";
        Uri baseUri = MediaStore.Files.getContentUri("external");
        String provider = "com.android.providers.media.MediaProvider";
        context.grantUriPermission(provider, baseUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Cursor c = null;
        try {
            c = context.getContentResolver().query(baseUri,
                    projection,
                    where,
                    new String[]{path},
                    null);
            if (c != null && c.moveToNext()) {
                String type = c.getString(c.getColumnIndexOrThrow(
                        MediaStore.MediaColumns.MIME_TYPE));
                if (type != null) {
                    mimeType = type;
                }
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        Log.d(TAG,"mimeType====="+mimeType);
        return mimeType;
    }
    private static Uri getItemContentUri(Context context, String path) {
        final String[] projection = {MediaStore.MediaColumns._ID};
        final String where = MediaStore.MediaColumns.DATA + " = ?";
        Uri baseUri = MediaStore.Files.getContentUri("external");
        Cursor c = null;
        String provider = "com.android.providers.media.MediaProvider";
        Uri itemUri = null;
        context.grantUriPermission(provider, baseUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            c = context.getContentResolver().query(baseUri,
                    projection,
                    where,
                    new String[]{path},
                    null);
            if (c != null && c.moveToNext()) {
                int type = c.getInt(c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                if (type != 0) {
                    long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                    itemUri =  Uri.withAppendedPath(baseUri, String.valueOf(id));
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return itemUri;
    }
    private static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String extension = null;
        final int lastDot = fileName.lastIndexOf('.');
        if ((lastDot >= 0)) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        return extension;
    }

}
