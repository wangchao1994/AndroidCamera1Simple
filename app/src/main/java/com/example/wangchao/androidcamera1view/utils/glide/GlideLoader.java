package com.example.wangchao.androidcamera1view.utils.glide;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wangchao.androidcamera1view.R;

/**
 * 图片异步加载的操作类
 */
public class GlideLoader {
    /**
     * 加载本地Resource图片
     */
    public static void loadLocalResource(Context context, int resourceId, ImageView imageView) {
        loadLocalResource(context, resourceId, imageView, false);
    }

    /**
     * 加载本地Resource,图片
     *
     * @param context
     * @param resourceId
     * @param imageView
     * @param isCircle   是否为圆角
     */
    public static void loadLocalResource(Context context, int resourceId, ImageView imageView, boolean isCircle) {
        if (isCircle){
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new CircleTransform(context));
            Glide.with(context).load(resourceId).apply(requestOptions).into(imageView);
        }else{
            Glide.with(context).load(resourceId).into(imageView);
        }
    }

    /**
     * 加载本地Resource,生成指定的圆角的图片
     * @param context
     * @param resourceId
     * @param imageView
     * @param cornerRadius 圆角度数
     */
    public static void loadLocalResource(Context context, int resourceId, ImageView imageView, float cornerRadius) {
        Glide.with(context).asBitmap().load(resourceId).into(new CircularBitmapImageViewTarget(context,imageView,cornerRadius));
    }

    /**
     * 加载网络资源，生成指定的圆角的图片
     *
     * @param context
     * @param url
     * @param imageView
     * @param cornerRadius 圆角度数
     */
    public static void loadNetWorkResource(Context context, String url,int defaultResource,int errorResource, ImageView imageView, float cornerRadius) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(defaultResource)
                .error(errorResource);
        Glide.with(context).asBitmap().load(url).apply(requestOptions).into(new CircularBitmapImageViewTarget(context,imageView,cornerRadius));
    }
    /**
     * 加载网络图片
     *
     * @param context
     * @param imageUrl
     * @param imageView
     */
    public static void loadNetWorkResource(Context context, String imageUrl, ImageView imageView) {
        loadNetWorkResource(context, imageUrl, imageView, false);
    }

    /**
     * 加载网络图片  , 圆角显示
     *
     * @param context
     * @param imageUrl
     * @param imageView
     * @param isCircle
     */
    public static void loadNetWorkResource(Context context, String imageUrl, ImageView imageView, boolean isCircle) {
        int defaultImageResource = R.drawable.btn_album;
        loadNetWorkResource(context, imageUrl, imageView, defaultImageResource, isCircle);
    }

    public static  void loadGifResource(Context context, String imageUrl, ImageView imageView){
        int defaultImageResource = R.drawable.btn_album;
        RequestOptions requestOptions = new RequestOptions()
                .error(defaultImageResource)
                .placeholder(defaultImageResource);
        Glide.with(context).asGif().load(imageUrl).into(imageView);
    }

    /**
     * 加载网络图片  , 圆角显示
     * @param context
     * @param imageUrl
     * @param imageView
     * @param isCircle
     */
    public static void loadNetWorkResource(Context context, String imageUrl, ImageView imageView, int defaultImageResource, boolean isCircle) {
        loadNetWorkResource(context, imageUrl, imageView, defaultImageResource, defaultImageResource, isCircle);
    }

    /**
     * 加载网络图片  , 圆角显示
     *
     * @param context
     * @param imageUrl
     * @param imageView
     * @param isCircle
     */
    public static void loadNetWorkResource(Context context, String imageUrl, ImageView imageView, int defaultImageResource, int errorResourceId, boolean isCircle) {
        loadNetWorkResource(context, imageUrl, imageView, defaultImageResource, errorResourceId, defaultImageResource, isCircle);
    }

    /**
     * 加载网络图片
     *
     * @param context
     * @param imageUrl
     * @param imageView
     * @param nullResourceId
     * @param placeResourceId
     * @param errorResourceId
     * @param isCircle
     */
    public static void loadNetWorkResource(Context context, String imageUrl, ImageView imageView, int nullResourceId, int placeResourceId, int errorResourceId, boolean isCircle) {
        if (isCircle){
            RequestOptions requestOptions = new RequestOptions()
                    .error(errorResourceId)
                    .placeholder(placeResourceId)
                    .centerCrop().transform(new CircleTransform(context))
                    .fallback(nullResourceId);
            Glide.with(context).load(imageUrl).apply(requestOptions).into(imageView);
        }else{
            RequestOptions requestOptions = new RequestOptions()
                    .error(errorResourceId)
                    .placeholder(placeResourceId)
                    .centerCrop().transform(new CircleTransform(context))
                    .fallback(nullResourceId);
            Glide.with(context).load(imageUrl).apply(requestOptions).into(imageView);
        }
    }




}
