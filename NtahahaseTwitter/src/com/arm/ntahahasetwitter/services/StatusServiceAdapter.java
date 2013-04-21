/**
 * 
 */
package com.arm.ntahahasetwitter.services;

import android.os.RemoteException;
import android.util.Log;

/**
 * @author adrianbabame
 *
 */
public class StatusServiceAdapter {
	private static final String TAG = StatusServiceAdapter.class
			.getSimpleName();
	
	private IStatusUpdateService statusUpdateServiceStub;
	
	public StatusServiceAdapter(IStatusUpdateService statusUpdateServiceStub) {
		this.statusUpdateServiceStub = statusUpdateServiceStub;
	}
	
	public void UpdateStatus(String status) {
		try {
			statusUpdateServiceStub.updateStatus(status);
		} catch (RemoteException e) {
			Log.e(TAG, "caught RemoteException: " + e.getMessage());
		}
	}
}
