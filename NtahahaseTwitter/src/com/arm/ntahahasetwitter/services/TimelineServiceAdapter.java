package com.arm.ntahahasetwitter.services;

import android.os.RemoteException;
import android.util.Log;

public class TimelineServiceAdapter {
	private static final String TAG = TimelineServiceAdapter.class.getSimpleName();
	private ITimelineService timelineServiceStub;

	public TimelineServiceAdapter(ITimelineService timelineServiceStub) {
		this.timelineServiceStub = timelineServiceStub;
	}
	
	public void fetchTimeline() {
		try {
			timelineServiceStub.fetchStatus();
		} catch (RemoteException e) {
			Log.e(TAG, "caught RemoteException: " + e.getMessage());
		}
	}

}
