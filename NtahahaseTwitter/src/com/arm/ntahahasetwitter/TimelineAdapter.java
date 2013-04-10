package com.arm.ntahahasetwitter;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arm.ntahahasetwitter.data.TimelineProvider;
import com.arm.ntahahasetwitter.data.TimelineProvider.TimelineConstant;
import com.arm.ntahahasetwitter.utils.TCLImageLoader;

public class TimelineAdapter extends SimpleCursorAdapter {

	private static final String[] FROM = { TimelineConstant.T_CREATED_AT,
			TimelineConstant.T_USER, TimelineConstant.T_TEXT };
	private static final int[] TO = { R.id.textCreatedAt, R.id.textUser,
			R.id.textMessage };

	private final LayoutInflater mInflater;
	private TCLImageLoader imageLoader;

	static class ViewHolder {
		ImageView imgUser;
		TextView textCreatedAt;
		TextView textMessage;
	}

	public TimelineAdapter(Context context, int layout_id, Cursor c,
			int flagRegisterContentObserver, TCLImageLoader imageLoader) {
		super(context, layout_id, c, FROM, TO, flagRegisterContentObserver);
		this.imageLoader = imageLoader;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		super.bindView(row, context, cursor);
		ViewHolder holder = (ViewHolder) row.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.imgUser = (ImageView) row.findViewById(R.id.pictUser);
			;
			holder.textCreatedAt = (TextView) row
					.findViewById(R.id.textCreatedAt);
			holder.textMessage = (TextView) row.findViewById(R.id.textMessage);
			row.setTag(holder);
		}
		long createdTimestamp = cursor.getLong(cursor
				.getColumnIndexOrThrow(TimelineConstant.T_CREATED_AT));
		Date currentDate = new Date();
		long currentDateLong = currentDate.getTime();
		CharSequence relTimeCreated = DateUtils.getRelativeTimeSpanString(
				createdTimestamp, currentDateLong, 0L,
				DateUtils.FORMAT_ABBREV_ALL);
		holder.textCreatedAt.setText(relTimeCreated);
		String screen_name = cursor.getString(cursor
				.getColumnIndexOrThrow(TimelineConstant.T_USER_SCREEN));
		String image_url = "https://api.twitter.com/1/users/profile_image/"
				+ screen_name + "?size=bigger";
		imageLoader.display(image_url, holder.imgUser, R.drawable.ic_launcher);

		long id = cursor.getLong(0);
		SpannableString span = new SpannableString(cursor.getString(cursor
				.getColumnIndexOrThrow(TimelineConstant.T_TEXT)));
		Cursor mentions = null;
		try {
			mentions = context.getContentResolver().query(
					TimelineProvider.TWIT_MENTIONS_URI,
					new String[] { TimelineConstant.M_START,
							TimelineConstant.M_END },
					TimelineProvider.TABLE_TWIT_MENTIONS + "."
							+ TimelineConstant.T_ID + " = " + id, null, null);
			if (mentions.getCount() > 0) {
				for (int i = 0; i < mentions.getCount(); i++) {
					mentions.moveToPosition(i);
					int start = mentions.getInt(0);
					int end = mentions.getInt(1);
					span.setSpan(
							new ForegroundColorSpan(Color.parseColor("#0000FF")),
							start, end,
							SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		} finally {
			mentions.close();
		}
		Cursor hashtags = null;
		try {
			hashtags = context.getContentResolver().query(
					TimelineProvider.TWIT_HASHTAGS_URI,
					new String[] { TimelineConstant.H_START,
							TimelineConstant.H_END },
					TimelineProvider.TABLE_TWIT_HASHTAG + "."
							+ TimelineConstant.T_ID + " = " + id, null, null);
			if (hashtags.getCount() > 0) {
				for (int j = 0; j < hashtags.getCount(); j++) {
					hashtags.moveToPosition(j);
					int start = hashtags.getInt(0);
					int end = hashtags.getInt(1);
					span.setSpan(
							new ForegroundColorSpan(Color.parseColor("#0000FF")),
							start, end,
							SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		} finally {
			hashtags.close();
		}
		Cursor urls = null;
		try {
			urls = context.getContentResolver()
					.query(TimelineProvider.TWIT_URL_URI,
							new String[] { TimelineConstant.U_START,
									TimelineConstant.U_END },
							TimelineProvider.TABLE_TWIT_URLS + "."
									+ TimelineConstant.T_ID + " = " + id, null,
							null);
			if (urls.getCount() > 0) {
				for (int i = 0; i < urls.getCount(); i++) {
					urls.moveToPosition(i);
					int start = urls.getInt(0);
					int end = urls.getInt(1);
					span.setSpan(
							new ForegroundColorSpan(Color.parseColor("#0000FF")),
							start, end,
							SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		} finally {
			urls.close();
		}
		holder.textMessage.setText(span);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View inflater = mInflater.inflate(R.layout.row_timeline, parent,
				false);
		return inflater;
	}

}
