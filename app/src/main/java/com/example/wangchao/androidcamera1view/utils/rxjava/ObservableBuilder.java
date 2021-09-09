package com.example.wangchao.androidcamera1view.utils.rxjava;

import android.content.Context;

import com.example.cameraview.utils.file.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import rx.Observable;
import rx.Subscriber;

public class ObservableBuilder {
    /**
     * 将byte数据写入图片文件中
     * @param context
     * @param bytes
     * @return
     */
    public static Observable<String> createWriteCaptureData(final Context context, final byte[] bytes) {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                File file = FileUtils.createPictureDiskFile(context, FileUtils.createBitmapFileName());
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(file);
                    output.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                subscriber.onNext(file.getAbsolutePath());
            }
        });
        return observable;
    }
    /**
     * 合并多个视频文件，到一个新的视频中
     *
     * @param filePath1
     * @param filePath2
     * @return
     */
    public static Observable<String> createMergeMuiltFile(final Context context, final String filePath1, final String filePath2) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String newFilePath = FileUtils.mergeMultipleVideoFile(context, filePath1, filePath2);
                subscriber.onNext(newFilePath);
            }
        });
    }
    /**
     * 录制的视频文件，的存储路径
     *
     * @param videoPath
     * @return
     */
    public static Observable<String> createVideo(String videoPath) {
        return Observable.just(videoPath);
    }
}
