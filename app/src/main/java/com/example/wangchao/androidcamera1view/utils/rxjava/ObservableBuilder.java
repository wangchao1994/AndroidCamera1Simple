package com.example.wangchao.androidcamera1view.utils.rxjava;

import android.content.Context;
import com.example.wangchao.androidcamera1view.utils.file.FileUtils;
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
}
