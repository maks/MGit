package me.sheimi.sgit.utils;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by sheimi on 8/8/13.
 */
public class ImageCache {

	private static final int SIZE = 100 << 20;
	private static ImageCache mInstance;

	private ImageLoader mImageLoader;
	private DisplayImageOptions mDiskCacheOption;
	private DisplayImageOptions mDefaultOptions;
	private Context mContext;

	public ImageCache(Context context) {
		mContext = context;
		mDiskCacheOption = new DisplayImageOptions.Builder()
												  .cacheOnDisc(true)
		                                          .cacheInMemory(true)
		                                          .build();
		mDefaultOptions = new DisplayImageOptions.Builder()
												 .cacheOnDisc(false)
												 .cacheInMemory(true)
												 .build();
		File cacheDir = StorageUtils.getCacheDirectory(context);
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				context).defaultDisplayImageOptions(mDiskCacheOption)
		                .discCache(new TotalSizeLimitedDiscCache(cacheDir, SIZE))
		                .build();
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(configuration);
	}

	public static ImageCache getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ImageCache(context);
		}
        if (context != null) {
            mInstance.mContext = context;
        }
		return mInstance;
	}

	public static ImageLoader getImageLoader(Context context) {
		return getInstance(context).getImageLoader();
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

}
