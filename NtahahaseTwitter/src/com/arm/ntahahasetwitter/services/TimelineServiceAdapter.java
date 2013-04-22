package com.arm.ntahahasetwitter.services;

import android.os.RemoteException;
import android.util.Log;

public class TimelineServiceAdapter {
	private static final String TAG = TimelineServiceAdapter.class.getSimpleName();
	private ITimelineService timelineServiceStub;

	public TimelineServiceAdapter(ITimelineService timelineServiceStub) {
		this.timelineServiceStub = timelineServiceStub;
	}
	
	public void fetchTimeline(int page, int count, long sinceId, long maxId) {
		try {
			timelineServiceStub.fetchStatus(page, count, sinceId, maxId);
		} catch (RemoteException e) {
			Log.e(TAG, "caught RemoteException: " + e.getMessage());
		}
	}

	public void fetchTimeline() {
		try {
			timelineServiceStub.futchStatus();
		} catch (RemoteException e) {
			Log.e(TAG, "caught RemoteException: " + e.getMessage());
		}
	}
	
	public void updateStatus(String status, boolean isLocationEnabled) {
		try {
			timelineServiceStub.updateStatus(status, isLocationEnabled);
		} catch (RemoteException e) {
			Log.e(TAG, "caught RemoteException: " + e.getMessage());
		}
	}

}
