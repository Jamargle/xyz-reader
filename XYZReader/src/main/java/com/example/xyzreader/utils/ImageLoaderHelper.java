package com.example.xyzreader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public final class ImageLoaderHelper {

    private final static int MAX_LRU_CACHE_SIZE = 20;
    private final static LruCache<String, Bitmap> BITMAP_LRU_CACHE = new LruCache<>(MAX_LRU_CACHE_SIZE);

    private static ImageLoaderHelper imageLoaderHelperInstance;
    private ImageLoader imageLoader;

    private ImageLoaderHelper(final Context applicationContext) {
        final RequestQueue queue = Volley.newRequestQueue(applicationContext);
        final ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(final String key, final Bitmap value) {
                BITMAP_LRU_CACHE.put(key, value);
            }

            @Override
            public Bitmap getBitmap(final String key) {
                return BITMAP_LRU_CACHE.get(key);
            }
        };
        imageLoader = new ImageLoader(queue, imageCache);
    }

    public static ImageLoaderHelper getInstance(final Context context) {
        if (imageLoaderHelperInstance == null) {
            imageLoaderHelperInstance = new ImageLoaderHelper(context.getApplicationContext());
        }

        return imageLoaderHelperInstance;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

}
