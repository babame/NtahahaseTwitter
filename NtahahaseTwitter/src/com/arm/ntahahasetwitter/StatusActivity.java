/**
 * 
 */
package com.arm.ntahahasetwitter;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author adrianbabame
 * 
 */
public class StatusActivity extends SherlockActivity {
	private static final String TAG = StatusActivity.class.getSimpleName();
	private EditText edit_status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_activity);
		edit_status = (EditText) findViewById(R.id.edit_status);
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onCreate");
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
		if (BuildConfig.DEBUG)
			Log.d(TAG, "status: " + status);
		return true;
	}
}
