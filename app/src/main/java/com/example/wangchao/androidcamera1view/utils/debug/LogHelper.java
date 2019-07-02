
package com.example.wangchao.androidcamera1view.utils.debug;

import com.example.wangchao.androidcamera1view.utils.debug.LogUtil.Tag;

/**
 * A wrapper class for AOSP Log.java, All other classes logging should use this class.
 */
public class LogHelper {
    /**
     * Send a ui log message, it will have a prefix for ui log.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void ui(Tag tag, String msg) {
        msg = new StringBuilder("[CamUI] ").append(msg).toString();
        i(tag, msg);
    }

    /**
    * Send a DEBUG log message.
    * @param tag Used to identify the source of a log message.  It usually identifies
    *        the class or activity where the log call occurs.
    * @param msg The message you would like logged.
    */
    public static void d(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag.toString(), msg);
        }
    }

    /**
     * Send a DEBUG log message.
     * @param tag tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void d(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag.toString(), LogUtil.addTags(
                    instance, msg));
        }
    }

    /***
     * Send a DEBUG log message.
     * @param tag  Used to identify the source of a log message.
     * @param instance instance Prefixes a message with with a hashcode tag of the object.
     * @param msg  The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void d(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag.toString(), LogUtil.addTags(
                    instance, msg, tags));
        }
    }

    /**
     * Send a DEBUG log message.
     * @param tag tag tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void d(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag.toString(), msg, tr);
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void e(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag.toString(), msg);
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void e(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag.toString(), LogUtil.addTags(
                    instance, msg));
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void e(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.e(tag.toString(), LogUtil.addTags(
                    instance, msg, tags));
        }
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void e(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag.toString(), msg, tr);
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void i(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.i(tag.toString(), msg);
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void i(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.i(tag.toString(), LogUtil.addTags(
                    instance, msg));
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void i(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.i(tag.toString(), LogUtil.addTags(
                    instance, msg, tags));
        }
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void i(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.i(tag.toString(), msg, tr);
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void v(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(tag.toString(), msg);
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void v(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(tag.toString(), LogUtil.addTags(instance, msg));
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void v(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.v(tag.toString(), LogUtil.addTags(
                    instance, msg, tags));
        }
    }

    /**
     * Send an VERBOSE log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void v(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(tag.toString(), msg, tr);
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void w(Tag tag, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(tag.toString(), msg);
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     */
    public static void w(Tag tag, Object instance, String msg) {
        if (LogUtil.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(tag.toString(), LogUtil.addTags(instance, msg));
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param instance Prefixes a message with with a hashcode tag of the object.
     * @param msg The message you would like logged.
     * @param tags Prefixes a message with the bracketed tags specified in the
     *             tag list.
     */
    public static void w(Tag tag, Object instance, String msg, String tags) {
        if (LogUtil.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.w(tag.toString(), LogUtil.addTags(
                    instance, msg, tags));
        }
    }

    /**
     * Send an WARNNING log message.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    public static void w(Tag tag, String msg, Throwable tr) {
        if (LogUtil.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(tag.toString(), msg, tr);
        }
    }

}