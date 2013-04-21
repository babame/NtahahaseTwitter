package com.arm.ntahahasetwitter.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arm.ntahahasetwitter.NtahahaseApp;
import com.arm.ntahahasetwitter.NtahahaseConfig;

public class NtahahaseService extends Service {
	private static final String TAG = NtahahaseService.class.getSimpleName();

	private ITimelineService.Stub mService2Timeline;

	private Twitterable twitterable;
	private NtahahaseConfig mConfig;
	private NtahahaseApp mApp;
	private Upadater mUpdater;

	private boolean runFlag;

	@Override
	public IBinder onBind(Intent i) {
		Log.i(TAG, "onBind");
		return mService2Timeline;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mApp = (NtahahaseApp) getApplication();
		mConfig = new NtahahaseConfig(
				PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext()));
		createTimelineStub();
		mUpdater = new Upadater();
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		runFlag = false;
		mUpdater.interrupt();
		mUpdater = null;
		mApp.setServiceRunning(false);
		Log.i(TAG, "onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		twitterable = new TwitterableImp(mConfig, getContentResolver());
		runFlag = true;
		mUpdater.start();
		mApp.setServiceRunning(true);
		Log.i(TAG, "onStartCommand");
		return START_STICKY;
	}

	private void createTimelineStub() {
		mService2Timeline = new ITimelineService.Stub() {

			@Override
			public void fetchStatus(int page, int count, long sinceId,
					long maxId) throws RemoteException {
				twitterable.fetchStatus(page, count, sinceId, maxId);
			}

			@Override
			public void futchStatus() throws RemoteException {
				twitterable.fetchStatus();
			}
		};
	}

	private class Upadater extends Thread {
		public Upadater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			NtahahaseService mService = NtahahaseService.this;
			while (mService.runFlag) {
				Log.d(TAG, "Updater running");
				try {
					twitterable.fetchStatus();
					Log.d(TAG, "Updater ran");
					Thread.sleep(5 * 60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					mService.runFlag = false;
				}
			}
		}

	}

}
