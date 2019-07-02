package com.example.wangchao.androidcamera1view.utils.debug;
import android.os.Build;
import android.os.Environment;
import android.os.Trace;

import java.io.File;

/**
 * This class tracks the timing of important state changes
 *  in camera app (e.g launch flow duration, etc).
 */
public class CameraSysTrace {
    private static final String CAMPERFORMANCEPREFIX = "[CamPtracker]";
    private static final String PERFORMANCE_FILE = "/cameraPerformance.txt";
    private static String sFilePath = new StringBuilder(
            Environment.getExternalStorageDirectory().toString())
                .append(PERFORMANCE_FILE).toString();
    private static final boolean DEBUG = new File(sFilePath).exists();

    /**
     * This gets called when an important state change happens, based on the type
     * of the event/state change. It will record the systrace.
     * @param evtName identify the event info.
     * @param isBegin true by recording starting, false by recording ending.
     */
    public static void onEventSystrace(String evtName, boolean isBegin) {
        if (!DEBUG || LogUtil.getAndroidSDKVersion() < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }
        evtName = new StringBuilder(CAMPERFORMANCEPREFIX).append(evtName).toString();
        if (isBegin) {
            Trace.beginSection(evtName);
        } else {
            Trace.endSection();
        }
    }
}
