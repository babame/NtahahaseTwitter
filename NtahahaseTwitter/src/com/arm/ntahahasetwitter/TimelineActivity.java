package com.arm.ntahahasetwitter;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.widget.ListView;

import com.arm.ntahahasetwitter.data.TimelineProvider;
import com.arm.ntahahasetwitter.data.TimelineProvider.TimelineConstant;
import com.arm.ntahahasetwitter.services.ITimelineService;
import com.arm.ntahahasetwitter.services.NtahahaseService;
import com.arm.ntahahasetwitter.services.TimelineServiceAdapter;
import com.arm.ntahahasetwitter.utils.TCLImageLoader;

public class TimelineActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = TimelineActivity.class.getSimpleName();

	private ServiceConnection mServiceConnection;
	private Intent mNtahahaseService;

	private TimelineServiceAdapter timelineAdapter;
	
	private TimelineAdapter adapter;
	
	private static final String[] projection = { TimelineConstant.T_ID + " as _id", TimelineConstant.T_CREATED_AT, TimelineConstant.T_USER, TimelineConstant.T_USER_SCREEN, TimelineConstant.T_TEXT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		ListView lTimeline = (ListView) findViewById(R.id.list_timeline);

		TCLImageLoader imageLoader = new TCLImageLoader(this);
		adapter = new TimelineAdapter(this, 0, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, imageLoader);
		lTimeline.setAdapter(adapter);
		getSupportLoaderManager().initLoader(0, null, this);
		registerNtahahaseService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindService(mNtahahaseService, mServiceConnection, BIND_AUTO_CREATE);
		Log.i(TAG, "onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mServiceConnection);
		Log.i(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
		adapter.getCursor().close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(mNtahahaseService);
		Log.i(TAG, "onDestroy");
	}

	private void registerNtahahaseService() {
		mNtahahaseService = new Intent(getApplicationContext(), NtahahaseService.class);
		
		mNtahahaseService
				.setAction("com.arm.ntahahasetwitter.NTAHAHASESERVICE");
		mServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "called onServiceDisconnected()");
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				timelineAdapter = new TimelineServiceAdapter(
						ITimelineService.Stub.asInterface(service));
				timelineAdapter.fetchTimeline();
			}
		};
		startService(mNtahahaseService);
		Log.i(TAG, "called startNtahahaseService");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader cursorLoader = new CursorLoader(this, TimelineProvider.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}
}
