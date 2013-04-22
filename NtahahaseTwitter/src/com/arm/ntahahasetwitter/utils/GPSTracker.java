/**
 * 
 */
package com.arm.ntahahasetwitter.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.arm.ntahahasetwitter.BuildConfig;

/**
 * @author Aamir Shah
 *
 */
public class GPSTracker implements LocationListener {
	private static final String TAG = GPSTracker.class.getSimpleName();
	
	private final Context context;
	
	// flag for GPS status
	public boolean isGPSEnabled = false;
	
	// flag for network status
	public boolean isNetworkEnabled = false;
	
	// flag for GPS status
	boolean canGetLocation = false;
	
	Location location;
	double latitude;
	double longitude;
	
	// the minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	
	// the minimum time between updates in miliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	
	// Declaring a location manager
	protected LocationManager locationManager;
	
	public GPSTracker(Context context) {
		this.context = context;
		getLocation();
	}
	
	public Location getLocation() {
		try {
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (BuildConfig.DEBUG)
				Log.v(TAG, "isGPSEnabled: " + isGPSEnabled);
			
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if (BuildConfig.DEBUG)
				Log.v(TAG, "isNetworkEnabled: " + isNetworkEnabled);
			
			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					if (BuildConfig.DEBUG)
						Log.d(TAG, "NetworkEnabled");
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
					if (isGPSEnabled) {
						if (location == null) {
							locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
							if (BuildConfig.DEBUG)
								Log.d(TAG, "GPS Enabled");
							if (locationManager != null) {
								location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
								if (location != null) {
									latitude = location.getLatitude();
									longitude = location.getLongitude();
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "caught Exception: " + e.getMessage());
		}
		return location;
	}
	
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}
	
	public double getLatitude() {
		if (location != null)
			latitude = location.getLatitude();
		return latitude;
	}
	
	public double getLongitude() {
		if (location != null)
			longitude = location.getLongitude();
		return longitude;
	}
	
	public boolean canGetLocation() {
		return this.canGetLocation;
	}
	
	public static AlertDialog createSettingDialog(final Context mContext) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
		
		// Dialog Title
		alertBuilder.setTitle("Activate GPS?");
		
		// Dialog Message
		alertBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		
		// on pressing settings button
		alertBuilder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
		
		// on pressing cancel button
		alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		return alertBuilder.create();
	}
	
	/* (non-Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
