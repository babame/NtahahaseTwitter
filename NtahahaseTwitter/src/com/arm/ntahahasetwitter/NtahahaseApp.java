package com.arm.ntahahasetwitter;

import android.app.Application;
import android.util.Log;

public class NtahahaseApp extends Application {
	private static final String TAG = NtahahaseApp.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.i(TAG, "onLowMemory");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminate");
	}
}
