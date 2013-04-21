/**
 * 
 */
package com.arm.ntahahasetwitter;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.arm.ntahahasetwitter.services.IStatusUpdateService;
import com.arm.ntahahasetwitter.services.StatusServiceAdapter;

/**
 * @author adrianbabame
 * 
 */
public class StatusActivity extends SherlockActivity {
	private static final String TAG = StatusActivity.class.getSimpleName();
	private EditText edit_status;
	
	private NtahahaseApp mApp;
	
	private Intent mNtahahaseService;
	private ServiceConnection mServiceConnection;
	
	private StatusServiceAdapter statusAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (NtahahaseApp) getApplication();
		mNtahahaseService = mApp.getServiceIntent();
		Uri statusUri = Uri.parse("status");
		mNtahahaseService.setData(statusUri);
		registerServiceConnection();
		setContentView(R.layout.status_activity);
		edit_status = (EditText) findViewById(R.id.edit_status);
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onCreate");
	}

	
	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mServiceConnection);
	}


	@Override
	protected void onResume() {
		super.onResume();
		bindService(mNtahahaseService, mServiceConnection, BIND_AUTO_CREATE);
		Log.i(TAG, "onResume");
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Send").setIcon(R.drawable.ic_menu_send)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String status = edit_status.getText().toString();
		statusAdapter.UpdateStatus(status);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "status: " + status);
		StatusActivity.this.finish();
		return true;
	}
	
	private void registerServiceConnection() {
		if (mApp.isServiceRunning())
			mServiceConnection = new ServiceConnection() {

				@Override
				public void onServiceDisconnected(ComponentName name) {
					Log.i(TAG, "called onServiceDisconnected()");
				}

				@Override
				public void onServiceConnected(ComponentName name,
						IBinder service) {
					statusAdapter = new StatusServiceAdapter(
							IStatusUpdateService.Stub.asInterface(service));
					if (BuildConfig.DEBUG)
						Log.i(TAG, "onServiceConnected()");
				}
			};
	}
}
