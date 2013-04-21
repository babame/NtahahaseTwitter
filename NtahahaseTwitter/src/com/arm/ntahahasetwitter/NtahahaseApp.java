package com.arm.ntahahasetwitter;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.arm.ntahahasetwitter.services.NtahahaseService;

public class NtahahaseApp extends Application {
	private static final String TAG = NtahahaseApp.class.getSimpleName();
	
	private boolean serviceRunning;
	
	public Intent mNtahahaseService;
	
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
	
	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}
	
	public Intent getServiceIntent() {
		return mNtahahaseService;
	}
	
	public void registerNtahahaseService() {
		mNtahahaseService = new Intent(getApplicationContext(),
				NtahahaseService.class);

		mNtahahaseService
				.setAction("com.arm.ntahahasetwitter.NTAHAHASESERVICE");
		if (!isServiceRunning())
			startService(mNtahahaseService);
	}
}
