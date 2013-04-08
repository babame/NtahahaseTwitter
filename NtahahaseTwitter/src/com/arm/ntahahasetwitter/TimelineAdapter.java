package com.arm.ntahahasetwitter;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arm.ntahahasetwitter.data.TimelineProvider.TimelineConstant;
import com.arm.ntahahasetwitter.utils.TCLImageLoader;

public class TimelineAdapter extends SimpleCursorAdapter {
	private static final String[] FROM = { TimelineConstant.T_CREATED_AT, TimelineConstant.T_USER, TimelineConstant.T_TEXT };
	private static final int[] TO = { R.id.textCreatedAt, R.id.textUser, R.id.textMessage };
	
	private final LayoutInflater mInflater;
	private TCLImageLoader imageLoader;
	
	public TimelineAdapter(Context context, int layout_id, Cursor c, int flagRegisterContentObserver, TCLImageLoader imageLoader) {
		super(context, layout_id, c, FROM, TO, flagRegisterContentObserver);
		this.imageLoader = imageLoader;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		super.bindView(row, context, cursor);
		TextView createdAt = (TextView) row.findViewById(R.id.textCreatedAt);
		ImageView imgUser = (ImageView) row.findViewById(R.id.pictUser);
		
		long createdTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(TimelineConstant.T_CREATED_AT));
		Date currentDate = new Date();
		long currentDateLong = currentDate.getTime();
		CharSequence relTimeCreated = DateUtils.getRelativeTimeSpanString(createdTimestamp, currentDateLong, 0L, DateUtils.FORMAT_ABBREV_ALL);
		createdAt.setText(relTimeCreated);
		String screen_name = cursor.getString(cursor.getColumnIndexOrThrow(TimelineConstant.T_USER_SCREEN));
		String image_url = "https://api.twitter.com/1/users/profile_image/" + screen_name + "?size=bigger";
		imageLoader.display(image_url, imgUser, R.drawable.ic_launcher);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View inflater = mInflater.inflate(R.layout.row_timeline, parent,
				false);
		return inflater;
	}
	
}
