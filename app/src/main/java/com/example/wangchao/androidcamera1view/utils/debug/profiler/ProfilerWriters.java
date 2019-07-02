package com.example.wangchao.androidcamera1view.utils.debug.profiler;

import com.example.wangchao.androidcamera1view.utils.debug.LogHelper;
import com.example.wangchao.androidcamera1view.utils.debug.LogUtil.Tag;

/**
 * To provide log writers.
 */
public class ProfilerWriters {
    private static ILogWriter sDebugWriter = new DebugWriter();

    /**
     * Get the log writer.
     * @return the log writer to record log.
     */
    public static ILogWriter getLogWriter() {
        return sDebugWriter;
    }

    /**
     * Debug log writer.
     */
    private static class DebugWriter implements ILogWriter {
        @Override
        public void write(Tag tag, String message) {
            LogHelper.d(tag, message);
        }
    }
}