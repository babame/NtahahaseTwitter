package com.arm.ntahahasetwitter;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arm.ntahahasetwitter.data.TimelineProvider.TimelineConstant;
import com.arm.ntahahasetwitter.utils.TCLImageLoader;

public class TimelineAdapter extends SimpleCursorAdapter {

	private static final String[] FROM = { TimelineConstant.T_CREATED_AT,
			TimelineConstant.T_USER, TimelineConstant.T_USER_SCREEN,
			TimelineConstant.T_SPANNED_TEXT };
	private static final int[] TO = { R.id.textCreatedAt, R.id.textUser,
			R.id.textScreenName, R.id.textMessage };

	private final LayoutInflater mInflater;
	private TCLImageLoader imageLoader;

	static class ViewHolder {
		ImageView imgUser;
		TextView textCreatedAt;
		TextView textScreenName;
		TextView textMessage;
		ImageView imgTwitPict;
	}

	public enum RoundedCorners {
		TOP, BOTTOM, ALL, NONE
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
			holder.textCreatedAt = (TextView) row
					.findViewById(R.id.textCreatedAt);
			holder.textMessage = (TextView) row.findViewById(R.id.textMessage);
			Typeface chunk = Typeface.createFromAsset(context.getAssets(),
					"ubuntu.ttf");
			holder.textMessage.setTypeface(chunk);
			holder.textScreenName = (TextView) row
					.findViewById(R.id.textScreenName);
			row.setTag(holder);
		}
		/* Time Stamp */
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
		/* User avatar */
		String image_url = "https://api.twitter.com/1/users/profile_image/"
				+ screen_name + "?size=bigger";
		holder.textScreenName.setText("@" + screen_name);
		CharSequence textToDisplay = cursor.getString(cursor
				.getColumnIndexOrThrow(TimelineConstant.T_SPANNED_TEXT));
		/* Mention Entity */
		Pattern pm = Pattern.compile("\\*\\#");
		Matcher mpm = pm.matcher(textToDisplay);
		while (mpm.find())
			textToDisplay = setSpanBetweenTokens(textToDisplay, "*#",
					new ForegroundColorSpan(0xFFDF6E00), new StyleSpan(
							Typeface.BOLD));
		/* Hashtag Entity */
		Pattern hm = Pattern.compile("\\^\\^");
		Matcher mhm = hm.matcher(textToDisplay);
		while (mhm.find())
			textToDisplay = setSpanBetweenTokens(textToDisplay, "^^",
					new ForegroundColorSpan(0xFFBF5D30), new StyleSpan(
							Typeface.BOLD));
		/* Url and media Entity */
		Pattern pmu = Pattern.compile("\\*\\*");
		Matcher mpmu = pmu.matcher(textToDisplay);
		while (mpmu.find())
			textToDisplay = setSpanBetweenTokens(textToDisplay, "**",
					new ForegroundColorSpan(0xFFECA672));
		holder.textMessage.setText(textToDisplay);
		imageLoader.display(image_url, holder.imgUser, R.drawable.ic_launcher);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View inflater = mInflater.inflate(R.layout.row_timeline, parent,
				false);
		return inflater;
	}

	private CharSequence setSpanBetweenTokens(CharSequence text, String token,
			CharacterStyle... cs) {
		// Start and end refer to the points where the span will apply
		int tokenLen = token.length();
		int start_url = text.toString().indexOf(token) + tokenLen;
		int end_url = text.toString().indexOf(token, start_url);
		// Copy the spannable string to a mutable spannable string
		SpannableStringBuilder ssb = new SpannableStringBuilder(text);
		if (start_url > -1 && end_url > -1) {
			for (CharacterStyle c : cs)
				ssb.setSpan(c, start_url, end_url, 0);

			// Delete the tokens before and after the span
			ssb.delete(end_url, end_url + tokenLen);
			ssb.delete(start_url - tokenLen, start_url);

			text = ssb;
		}
		return text;
	}
}
