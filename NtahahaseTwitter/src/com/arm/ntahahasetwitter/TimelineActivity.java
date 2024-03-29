package com.arm.ntahahasetwitter;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.arm.ntahahasetwitter.data.TimelineProvider;
import com.arm.ntahahasetwitter.data.TimelineProvider.TimelineConstant;
import com.arm.ntahahasetwitter.services.ITimelineService;
import com.arm.ntahahasetwitter.services.NtahahaseService;
import com.arm.ntahahasetwitter.services.TimelineServiceAdapter;
import com.arm.ntahahasetwitter.utils.TCLImageLoader;
import com.arm.ntahahasetwitter.widget.SampleListFragment;
import com.arm.ntahahasetwitter.widget.SherlockSlidingFragmentActivity;

public class TimelineActivity extends SherlockSlidingFragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = TimelineActivity.class.getSimpleName();

	private ServiceConnection mServiceConnection;
	private Intent mNtahahaseService;

	private TimelineServiceAdapter timelineAdapter;

	private ListView lTimeline;
	private TimelineAdapter adapter;
	private TCLImageLoader imageLoader;

	private boolean loading = true;

	private static final String[] projection = {
			TimelineConstant.T_ID + " as _id", TimelineConstant.T_CREATED_AT,
			TimelineConstant.T_USER, TimelineConstant.T_USER_SCREEN,
			TimelineConstant.T_SPANNED_TEXT };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSlidingMenu();
		lTimeline = (ListView) findViewById(R.id.list_timeline);
		imageLoader = new TCLImageLoader(getApplicationContext());
		adapter = new TimelineAdapter(this, 0, null,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, imageLoader);
		lTimeline.setAdapter(adapter);
		lTimeline.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				int lastInScreen = firstVisibleItem + visibleItemCount;
				if ((lastInScreen == totalItemCount) && !loading) {
					Cursor c = null;
					long maxId = 0;
					try {
						c = getContentResolver().query(
								TimelineProvider.CONTENT_URI,
								new String[] { "min(" + TimelineConstant.T_ID
										+ ")" }, null, null, null);
						maxId = c.moveToNext() ? c.getLong(0) : Long.MAX_VALUE;
					} finally {
						c.close();
					}
					loading = true;
					timelineAdapter.fetchTimeline(1, 40, -1, maxId);
				}
			}
		});
		registerServiceConnection();
		getSupportLoaderManager().initLoader(0, null, this);
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "twit").setIcon(R.drawable.ic_tab_tweet)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int match = item.getItemId();
		switch (match) {
		case 0:
			Intent statusIntent = new Intent(getApplicationContext(),
					StatusActivity.class);
			startActivityForResult(statusIntent, 1);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (data.getExtras().containsKey("text")) {
				if (BuildConfig.DEBUG)
					Log.d(TAG,
							"statusText: "
									+ data.getStringExtra("text")
									+ "\nisLocationEnabled: "
									+ data.getBooleanExtra("isLocationEnabled",
											false));
				if (data.getExtras().containsKey("media_path"))
					timelineAdapter.updateStatus(data.getStringExtra("text"),
							data.getStringExtra("media_path"),
							data.getBooleanExtra("isLocationEnabled", false));
				else
					timelineAdapter.updateStatus(data.getStringExtra("text"), null, 
							data.getBooleanExtra("isLocationEnabled", false));
			}
		} else {
			Log.d(TAG, "canceled");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader cursorLoader = new CursorLoader(this,
				TimelineProvider.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		adapter.swapCursor(cursor);
		loading = false;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

	private void registerServiceConnection() {
		mNtahahaseService = new Intent(getApplicationContext(),
				NtahahaseService.class);
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
				if (BuildConfig.DEBUG)
					Log.i(TAG, "onServiceConnected()");
			}
		};
		NtahahaseApp mApp = (NtahahaseApp) getApplication();
		if (!mApp.isServiceRunning())
			startService(mNtahahaseService);
	}

	private void setSlidingMenu() {
		ActionBar ab = getSherlock().getActionBar();
		LayoutInflater li = LayoutInflater.from(this);
		View customView = li.inflate(R.layout.custom_bar, null);
		ab.setCustomView(customView);
		// set the Behind View
		setBehindContentView(R.layout.frame);
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.add(R.id.frame, new SampleListFragment());
		t.commit();

		// customize the SlidingMenu
		this.setSlidingActionBarEnabled(false);
		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setShadowDrawable(R.drawable.shadow);
		getSlidingMenu().setBehindOffsetRes(R.dimen.actionbar_home_width);
		getSlidingMenu().setBehindScrollScale(0.25f);
		setContentView(R.layout.activity_timeline);
		ImageButton btn_home = (ImageButton) customView
				.findViewById(R.id.bar_home);
		btn_home.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggle();
			}
		});
	}
}
