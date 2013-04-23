package com.arm.ntahahasetwitter.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.jakewharton.DiskLruCache;
import com.jakewharton.DiskLruCache.Snapshot;

public class TCLImageLoader {
	private TCLruCache cache;
	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 5; // 10MB

	public TCLImageLoader(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		int memoryClass = am.getMemoryClass() * 1024 * 1024;
		cache = new TCLruCache(memoryClass / 8);
		File cacheDir = Utils.getExternalCacheDir(context);
		new InitDiskCacheTask().execute(cacheDir);
	}

	public void display(String url, ImageView imageview, int defaultResource) {
		imageview.setImageResource(defaultResource);
		Bitmap image = cache.get(url);
		if (null != image) {
			imageview.setImageBitmap(image);
		} else {
			new SetImageTask(imageview).execute(url);
		}
	}

	private class InitDiskCacheTask extends AsyncTask<File, Void, Void> {

		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				try {
					mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1,
							DISK_CACHE_SIZE);
					mDiskCacheStarting = false; // Finished initialization
					mDiskCacheLock.notifyAll(); // Wake any waiting threads
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	private class TCLruCache extends LruCache<String, Bitmap> {

		public TCLruCache(int maxSize) {
			super(maxSize);
		}

		@SuppressLint("NewApi")
		@Override
		protected int sizeOf(String key, Bitmap value) {
			if (Integer.valueOf(Build.VERSION.SDK_INT) >= 12)
				return value.getByteCount();
			else
				return value.getRowBytes() * value.getHeight();
		}
	}

	private class SetImageTask extends AsyncTask<String, Void, Integer> {
		private ImageView imageview;
		private Bitmap bmp;

		public SetImageTask(ImageView imageview) {
			this.imageview = imageview;
		}

		@Override
		protected Integer doInBackground(String... params) {
			String url = params[0];
			bmp = getBitmapFromDiskCache(url);
			if (bmp == null) {
				try {
					bmp = getBitmapFromURL(url);
					if (bmp != null)
						addBitmapToCache(url, bmp);
					else
						return 0;
				} catch (Exception e) {
					e.printStackTrace();
					return 0;
				}
			}
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == 1)
				imageview.setImageBitmap(bmp);
			super.onPostExecute(result);
		}

		public Bitmap getBitmapFromDiskCache(String key) {
			synchronized (mDiskCacheLock) {
				while (mDiskCacheStarting) {
					try {
						mDiskCacheLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (mDiskLruCache != null) {
					Snapshot snapshot = null;
					Bitmap bitmap = null;
					try {
						snapshot = mDiskLruCache.get(String.valueOf(key.hashCode()));
						if (null == snapshot)
							return null;
						final InputStream in = snapshot.getInputStream(0);
						if (null != in) {
							final BufferedInputStream buffIn = new BufferedInputStream(
									in, 8 * 1024);
							bitmap = BitmapFactory.decodeStream(buffIn);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (null != snapshot)
							snapshot.close();
					}
					return bitmap;
				}
			}
			return null;
		}

		public void addBitmapToCache(String key, Bitmap bitmap) {
			// add to memory cache
			if (cache.get(key) == null)
				cache.put(key, bitmap);

			// add to disk cache
			synchronized (mDiskCacheLock) {
				try {
					if (mDiskLruCache != null && mDiskLruCache.get(String.valueOf(key.hashCode())) == null) {
						DiskLruCache.Editor editor = null;
						try {
							editor = mDiskLruCache.edit(String.valueOf(key.hashCode()));
							if (editor == null)
								return;
							if (writeBitmapToFile(bitmap, editor)) {
								mDiskLruCache.flush();
								editor.commit();
							} else
								editor.abort();
						} catch (IOException e) {
							try {
								if (editor != null) {
									editor.abort();
								}
							} catch (IOException ignored) {
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private boolean writeBitmapToFile(Bitmap bitmap,
				DiskLruCache.Editor editor) throws IOException,
				FileNotFoundException {
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(editor.newOutputStream(0),
						8 * 1024);
				return bitmap.compress(CompressFormat.PNG, 90, out);
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}

		private Bitmap getBitmapFromURL(String src) {
			try {
				URL url = new URL(src);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				Bitmap myBitmap = BitmapFactory.decodeStream(input);
				return myBitmap;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
