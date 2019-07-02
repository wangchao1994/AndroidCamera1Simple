package com.example.wangchao.androidcamera1view.utils.debug.profiler;

import com.example.wangchao.androidcamera1view.utils.debug.LogUtil.Tag;

/**
 * Used to write strings to an arbitrary output source.
 */
public interface ILogWriter {
    /**
     * Used to write messages to another stream or object.
     * @param tag the class tag.
     * @param message the log message.
     */
    public void write(Tag tag, String message);
}
