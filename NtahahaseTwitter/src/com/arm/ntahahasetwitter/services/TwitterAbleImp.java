package com.arm.ntahahasetwitter.services;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;

import com.arm.ntahahasetwitter.Constant;
import com.arm.ntahahasetwitter.NtahahaseConfig;
import com.arm.ntahahasetwitter.data.TimelineProvider;
import com.arm.ntahahasetwitter.data.TimelineProvider.TimelineConstant;

public class TwitterAbleImp implements TwitterAble {

	/* Twitter things */
	private Twitter mTwitter;

	private ContentResolver mContentResolver;

	public TwitterAbleImp(NtahahaseConfig mConfig,
			ContentResolver contentResolver) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(Constant.TWITTER_KEY);
		builder.setOAuthConsumerSecret(Constant.TWITTER_SECRET);
		builder.setGZIPEnabled(false);
		builder.setHttpConnectionTimeout(5000);
		builder.setIncludeEntitiesEnabled(true);
		builder.setIncludeRTsEnabled(true);

		TwitterFactory factory = new TwitterFactory(builder.build());
		mTwitter = factory.getInstance();
		AccessToken accessToken = new AccessToken(mConfig.accessToken,
				mConfig.consumerSecret);
		mTwitter.setOAuthAccessToken(accessToken);
		mContentResolver = contentResolver;
	}

	@Override
	public void fetchStatus() {
		Paging paging = new Paging(2, 40);
		try {
			List<Status> statuses = mTwitter.getHomeTimeline(paging);
			for (Status status : statuses) {
				updateTwitEntryInDB(status);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	private ContentValues getContentValuesForTimelineEntry(final Status entry) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.T_ID, entry.getId());
		values.put(TimelineConstant.T_CREATED_AT, entry.getCreatedAt()
				.getTime());
		values.put(TimelineConstant.T_USER, entry.getUser().getName());
		values.put(TimelineConstant.T_USER_SCREEN, entry.getUser()
				.getScreenName());
		values.put(TimelineConstant.T_TEXT, entry.getText());
		return values;
	}

	private void updateTwitEntryInDB(final Status status) {
		final ContentValues values = getContentValuesForTimelineEntry(status);
		if (mContentResolver.update(TimelineProvider.CONTENT_URI, values,
				TimelineConstant.T_ID + " = ?",
				new String[] { String.valueOf(status.getId()) }) == 0)
			tryToAddTwitToDB(values);
	}

	private void tryToAddTwitToDB(final ContentValues values) {
		mContentResolver.insert(TimelineProvider.CONTENT_URI, values);
	}
}
