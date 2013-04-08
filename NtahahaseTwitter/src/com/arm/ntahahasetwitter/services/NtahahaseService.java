package com.arm.ntahahasetwitter.services;

import com.arm.ntahahasetwitter.NtahahaseConfig;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class NtahahaseService extends Service {
	private static final String TAG = NtahahaseService.class.getSimpleName();

	private ITimelineService.Stub mService2Timeline;

	private TwitterAble twitterAble;
	private NtahahaseConfig mConfig;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "onBind");
		return mService2Timeline;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mConfig = new NtahahaseConfig(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
		createTimelineStub();
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		twitterAble = new TwitterAbleImp(mConfig, getContentResolver());
		Log.i(TAG, "onStartCommand");
		return START_STICKY;
	}

	private void createTimelineStub() {
		mService2Timeline = new ITimelineService.Stub() {

			@Override
			public void fetchStatus() throws RemoteException {
				twitterAble.fetchStatus();
			}
		};
	}

}
