package com.arm.ntahahasetwitter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

public class NtahahaseConfig implements OnSharedPreferenceChangeListener {
	private static final String TAG = NtahahaseConfig.class.getSimpleName();
	
	private final SharedPreferences prefs;
	
	public String accessToken;
	public String consumerSecret;
	
	public NtahahaseConfig(SharedPreferences _prefs) {
		super();
		prefs = _prefs;
		prefs.registerOnSharedPreferenceChangeListener(this);
		loadPrefs(prefs);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.i(TAG, "onSharedPreferenceChanged(): " + key);
		loadPrefs(sharedPreferences);
	}
	
	private void loadPrefs(SharedPreferences prefs) {
		this.accessToken = prefs.getString(Constant.ACCESS_TOKEN, null);
		this.consumerSecret = prefs.getString(Constant.CONSUMER_SECRET, null);
	}
}
