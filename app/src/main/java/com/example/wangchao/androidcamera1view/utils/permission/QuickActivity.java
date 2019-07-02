package com.example.wangchao.androidcamera1view.utils.permission;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.example.wangchao.androidcamera1view.utils.debug.LogHelper;
import com.example.wangchao.androidcamera1view.utils.debug.LogUtil;
import com.example.wangchao.androidcamera1view.utils.debug.profiler.IPerformanceProfile;
import com.example.wangchao.androidcamera1view.utils.debug.profiler.PerformanceTracker;

/**
 * The KeyguardManager service can be queried to determine which state we are in.
 * If started from the lock screen, the activity may be quickly started,
 * resumed, paused, stopped, and then started and resumed again. This is
 * problematic for launch time from the lock screen because we typically open the
 * camera in onResume() and close it in onPause(). These camera operations take
 * a long time to complete. To workaround it, this class filters out
 * high-frequency onResume()->onPause() sequences if the KeyguardManager
 * indicates that we have started from the lock screen.
 *
 * Subclasses should override the appropriate onPermission[Create|Start...]Tasks()
 * method in place of the original.
 *
 * Sequences of onResume() followed quickly by onPause(), when the activity is
 * started from a lockscreen will result in a quick no-op.
 */
public abstract class QuickActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final LogUtil.Tag TAG = new LogUtil.Tag(QuickActivity.class.getSimpleName());
    /** onResume tasks delay from secure lockscreen. */
    private static final long ON_RESUME_DELAY_SECURE_MILLIS = 30;
    /** onResume tasks delay from non-secure lockscreen. */
    private static final long ON_RESUME_DELAY_NON_SECURE_MILLIS = 15;

    /** A reference to the main handler on which to run lifecycle methods. */
    private Handler mMainHandler;

    /**
     * True if onResume tasks have been skipped, and made false again once they
     * are executed within the onResume() method or from a delayed Runnable.
     */
    private boolean mSkippedFirstOnResume = false;

    /** Was this session started with onCreate(). */
    protected boolean mStartupOnCreate = false;

    /** Handle to Keyguard service. */
    private KeyguardManager mKeyguardManager = null;
    /**
     * A runnable for deferring tasks to be performed in onResume() if starting
     * from the lockscreen.
     */
    private final Runnable mOnResumeTasks = new Runnable() {
        @Override
        public void run() {
            if (mSkippedFirstOnResume) {
                LogHelper.d(TAG, "delayed Runnable --> onPermissionResumeTasks()");
                // Doing the tasks, can set to false.
                mSkippedFirstOnResume = false;
                onPermissionResumeTasks();
            }
        }
    };

    @Override
    protected final void onNewIntent(Intent intent) {
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onNewIntent").start();
        setIntent(intent);
        super.onNewIntent(intent);
        onNewIntentTasks(intent);
        profile.stop();
    }

    @Override
    protected final void onCreate(Bundle bundle) {
        LogHelper.i(TAG, "onCreate()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onCreate").start();
        mStartupOnCreate = true;
        super.onCreate(bundle);
        mMainHandler = new Handler(getMainLooper());
        onPermissionCreateTasks(bundle);
        profile.stop();
    }

    @Override
    protected final void onStart() {
        LogHelper.i(TAG, "onStart()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onStart").start();
        onPermissionStartTasks();
        super.onStart();
        profile.stop();
    }

    @Override
    protected final void onResume() {
        LogHelper.i(TAG, "onResume()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onResume").start();
        // For lockscreen launch, there are two possible flows:
        // 1. onPause() does not occur before mOnResumeTasks is executed:
        //      Runnable mOnResumeTasks sets mSkippedFirstOnResume to false
        // 2. onPause() occurs within ON_RESUME_DELAY_*_MILLIS:
        //     a. Runnable mOnResumeTasks is removed
        //     b. onPermissionPauseTasks() is skipped, mSkippedFirstOnResume remains true
        //     c. next onResume() will immediately execute onPermissionResumeTasks()
        //        and set mSkippedFirstOnResume to false

        mMainHandler.removeCallbacks(mOnResumeTasks);
        if (isKeyguardLocked() && mSkippedFirstOnResume == false) {
            // Skipping onPermissionResumeTasks; set to true.
            mSkippedFirstOnResume = true;
            long delay = isKeyguardSecure() ? ON_RESUME_DELAY_SECURE_MILLIS :
                    ON_RESUME_DELAY_NON_SECURE_MILLIS;
            LogHelper.d(TAG, "onResume() --> postDelayed(mOnResumeTasks," + delay + ")");
            mMainHandler.postDelayed(mOnResumeTasks, delay);
        } else {
            LogHelper.d(TAG, "onResume --> onPermissionResumeTasks()");
            // Doing the tasks, can set to false.
            mSkippedFirstOnResume = false;
            onPermissionResumeTasks();
        }
        super.onResume();
        profile.stop();
    }

    @Override
    protected final void onPause() {
        LogHelper.i(TAG, "onPause()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onPause").start();
        mMainHandler.removeCallbacks(mOnResumeTasks);
        // Only run onPermissionPauseTasks if we have not skipped onPermissionResumeTasks in a
        // first call to onResume.  If we did skip onPermissionResumeTasks (note: we
        // just killed any delayed Runnable), we also skip onPermissionPauseTasks to
        // adhere to lifecycle state machine.
        if (mSkippedFirstOnResume == false) {
            LogHelper.d(TAG, "onPause --> onPermissionPauseTasks()");
            onPermissionPauseTasks();
        }
        super.onPause();
        mStartupOnCreate = false;
        profile.stop();
    }

    @Override
    protected final void onStop() {
        LogHelper.i(TAG, "onStop()");
        if (isChangingConfigurations()) {
            LogHelper.d(TAG, "changing configurations");
        }
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onStop").start();
        onPermissionStopTasks();
        super.onStop();
        profile.stop();
    }

    @Override
    protected final void onRestart() {
        LogHelper.i(TAG, "onRestart()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onRestart").start();
        super.onRestart();
        profile.stop();
    }

    @Override
    protected final void onDestroy() {
        LogHelper.i(TAG, "onDestroy()");
        IPerformanceProfile profile = PerformanceTracker.create(TAG, "onDestroy").start();
        onPermissionDestroyTasks();
        super.onDestroy();
        profile.stop();
    }

    protected boolean isKeyguardLocked() {
        boolean isLocked = false;
        KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (kgm != null) {
            isLocked = kgm.isKeyguardLocked();
        }
        LogHelper.d(TAG, "isKeyguardLocked = " + isLocked);
        return isLocked;
    }

    protected boolean isKeyguardSecure() {
        boolean isSecure = false;
        KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (kgm != null) {
            isSecure = kgm.isKeyguardSecure();
        }
        LogHelper.d(TAG, "isKeyguardSecure = " + isSecure);
        return isSecure;
    }

    /**
     * Subclasses should override this.
     */
    protected void onNewIntentTasks(Intent newIntent) {
    }

    /**
     * These are for permission check flow.
     * @param savedInstanceState create bundle to sub class.
     */
    protected void onPermissionCreateTasks(Bundle savedInstanceState) {
    }

    protected void onPermissionStartTasks() {
    }

    protected void onPermissionResumeTasks() {
    }

    protected void onPermissionPauseTasks() {
    }

    protected void onPermissionStopTasks() {
    }

    protected void onPermissionDestroyTasks() {
    }
}
