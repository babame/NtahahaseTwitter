/**
 * 
 */
package com.arm.ntahahasetwitter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
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

	private boolean isLocationEnabled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar ab = getSherlock().getActionBar();
		LayoutInflater li = LayoutInflater.from(this);
		View customView = li.inflate(R.layout.custom_bar, null);
		ImageButton btn_send = (ImageButton) customView
				.findViewById(R.id.bar_settings);
		btn_send.setImageResource(R.drawable.ic_menu_send);
		btn_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String status = edit_status.getText().toString();
				if (status.length() > 0) {
					Intent i = getIntent();
					i.putExtra("text", status);
					i.putExtra("isLocationEnabled", isLocationEnabled);
					setResult(RESULT_OK, i);
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"The text is empty", Toast.LENGTH_SHORT).show();
				}
			}
		});
		ab.setCustomView(customView);
		setContentView(R.layout.status_activity);
		edit_status = (EditText) findViewById(R.id.edit_status);
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onCreate");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Location").setIcon(R.drawable.ic_menu_location)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			isLocationEnabled = true;
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = getIntent();
		setResult(RESULT_CANCELED, i);
		finish();
	}
}
