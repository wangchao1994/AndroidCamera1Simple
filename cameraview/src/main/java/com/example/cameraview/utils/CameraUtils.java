package com.example.cameraview.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.Constants;
import com.google.android.cameraview.SizeMap;

import java.io.File;
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
    public  static Uri getItemContentUri(Context context,String path) {
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
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return itemUri;
    }
    public static String getFileExtension(String fileName) {
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
