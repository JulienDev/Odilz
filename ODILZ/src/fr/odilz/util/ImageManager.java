package fr.odilz.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

public class ImageManager {

	private static final int MAX_POOL_SIZE = 10;
	private static final int MAX_QUEUE_SIZE = 100;

	private Context mContext;

	private LruCache<String, Bitmap> mMemoryCache;
	private ThreadPoolExecutor mExecutor;
	private LinkedBlockingQueue mQueue;
	private File mCacheDir;

	public ImageManager(Context context) {
		mContext = context;
		mQueue = new LinkedBlockingQueue(MAX_QUEUE_SIZE);
		mExecutor = new ThreadPoolExecutor(MAX_POOL_SIZE, MAX_POOL_SIZE, 5000, TimeUnit.MILLISECONDS, mQueue);
		mCacheDir = mContext.getCacheDir();
		
		// Get memory class of this device, exceeding this amount will throw an
	    // OutOfMemory exception.
	    final int memClass = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = 1024 * 1024 * memClass / 8;
	    
	    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
	        @Override
	        protected int sizeOf(String key, Bitmap value) {
	        	// The cache size will be measured in bytes rather than number of items.
	        	ByteArrayOutputStream bao = new ByteArrayOutputStream();
	        	value.compress(Bitmap.CompressFormat.PNG, 100, bao);
	        	byte[] ba = bao.toByteArray();
	        	return ba.length;	        	
	        }
	    };
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (key != null) {
			if (getBitmapFromMemCache(key) == null) {
		        mMemoryCache.put(key, bitmap);
		    }
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}

	public interface OnImageDownloaderListener {
		void onImageDownloadSuccess(Bitmap bitmap, ImageView imageView, String url);
		void onImageDownloadFail();
	}

	public void download(OnImageDownloaderListener listener, ImageView imageView, String url, boolean cache) {
		AsyncTask task = new DownloadTask(listener, imageView, url, cache);
		task.executeOnExecutor(mExecutor, null);
	}

	private class DownloadTask extends AsyncTask<Void, Void, Bitmap> {

		private OnImageDownloaderListener mListener;
		private ImageView mImageView;
		private String mUrl;
		private boolean mCache;

		public DownloadTask(OnImageDownloaderListener listener, ImageView imageView, String url, boolean cache) {
			mListener = listener;
			mImageView = imageView;
			mUrl = url;
			mCache = cache;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			try {
				Bitmap bitmap = null;
				String hashUrl = null;
				if (mCache) {
					hashUrl = hashUrl(mUrl);
					//Memory cache
					bitmap = getBitmapFromMemCache(hashUrl);
					if (bitmap == null) {
						//Disk cache
						bitmap = decodeFile(hashUrl);
					}
				}
				if (bitmap == null) {
					//Internet
					URL url = new URL(mUrl);
					URLConnection connection = url.openConnection();
					HttpURLConnection httpConnection = (HttpURLConnection) connection;
					int responseCode = httpConnection.getResponseCode();
					if (responseCode == HttpURLConnection.HTTP_OK) {
						InputStream is = httpConnection.getInputStream();
						bitmap = BitmapFactory.decodeStream(is);
						if (mCache && bitmap!= null) {
							saveFile(bitmap, hashUrl);
						}
					}
				}
				
				if (bitmap != null) {
					//Add Bitmap to LruCache
					addBitmapToMemoryCache(hashUrl, bitmap);
				}
				
				return bitmap;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);

			if (bitmap != null) {
				mListener.onImageDownloadSuccess(bitmap, mImageView, mUrl);
			}
			mListener.onImageDownloadFail();
		}
	}
	
	private void saveFile(Bitmap bitmap, String hash) throws IOException {
		mCacheDir.mkdir();
		File file = new File(mCacheDir, hash);
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
	}
	
	private Bitmap decodeFile(String hash) {
		File file = new File(mCacheDir, hash);
		return BitmapFactory.decodeFile(file.getPath());
	}

	public String hashUrl(String url) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(url.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
}