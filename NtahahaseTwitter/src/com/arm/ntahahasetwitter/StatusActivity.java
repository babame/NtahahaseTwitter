/**
 * 
 */
package com.arm.ntahahasetwitter;

import java.io.FileNotFoundException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

	private ImageView img_toUpload;

	private boolean isLocationEnabled;

	private String filePath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar ab = getSherlock().getActionBar();
		LayoutInflater li = LayoutInflater.from(this);
		View customView = li.inflate(R.layout.custom_bar, null);
		ImageButton btn_send = (ImageButton) customView
				.findViewById(R.id.bar_settings);
		btn_send.setImageResource(R.drawable.ic_tab_send);
		btn_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String status = edit_status.getText().toString();
				if (status.length() > 0) {
					Intent i = getIntent();
					i.putExtra("text", status);
					i.putExtra("isLocationEnabled", isLocationEnabled);
					if (null != filePath) {
						i.putExtra("media_path", filePath);
						Log.d(TAG, "filePath: " + filePath);
					}
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
		img_toUpload = (ImageView) findViewById(R.id.img_toUpload);
		edit_status = (EditText) findViewById(R.id.edit_status);
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onCreate");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 1, "Location")
				.setIcon(
						isLocationEnabled ? R.drawable.ic_menu_location_selected
								: R.drawable.ic_menu_location_unselected)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(0, 1, 2, "Media").setIcon(R.drawable.ic_menu_media)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0, 2, 3, "Save").setIcon(R.drawable.ic_menu_save)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			isLocationEnabled = !isLocationEnabled;
			if (item.getItemId() == 0)
				item.setIcon(isLocationEnabled ? R.drawable.ic_menu_location_selected
						: R.drawable.ic_menu_location_unselected);
			break;
		case 1:
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, 100);
			Log.d(TAG, "media selected");
			break;
		case 2:
			Log.d(TAG, "save selected");
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 100:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();

				Cursor cursor = getContentResolver().query(selectedImage,
						new String[] { MediaStore.Images.Media.DATA }, null,
						null, null);

				cursor.moveToFirst();

				filePath = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				cursor.close();

				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inJustDecodeBounds = true;

				try {
					BitmapFactory.decodeStream(getContentResolver()
							.openInputStream(selectedImage));
					// The new size we want to scale to
					final int REQUIRED_SIZE = 100;

					// Find the correct scale value. It should be the power of 2
					int width_tmp = opt.outWidth, height_tmp = opt.outHeight;
					int scale = 1;
					while (true) {
						if (width_tmp / 2 < REQUIRED_SIZE
								|| height_tmp / 2 < REQUIRED_SIZE)
							break;
						width_tmp /= 2;
						height_tmp /= 2;
						scale *= 2;
					}
					// Decode with inSampleSize
					BitmapFactory.Options opt2 = new BitmapFactory.Options();
					opt2.inSampleSize = scale;
					img_toUpload.setImageBitmap(BitmapFactory
							.decodeStream(
									getContentResolver().openInputStream(
											selectedImage), null, opt2));
				} catch (FileNotFoundException e) {
					// should not happened
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = getIntent();
		setResult(RESULT_CANCELED, i);
		finish();
	}
}
