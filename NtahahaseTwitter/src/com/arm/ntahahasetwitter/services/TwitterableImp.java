package com.arm.ntahahasetwitter.services;

import java.util.List;

import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.arm.ntahahasetwitter.BuildConfig;
import com.arm.ntahahasetwitter.Constant;
import com.arm.ntahahasetwitter.NtahahaseConfig;
import com.arm.ntahahasetwitter.data.TimelineProvider;
import com.arm.ntahahasetwitter.data.TimelineProvider.TimelineConstant;

public class TwitterableImp implements Twitterable {
	private static final String TAG = TwitterableImp.class.getSimpleName();

	/* Twitter things */
	private Twitter mTwitter;

	private ContentResolver mContentResolver;

	public TwitterableImp(NtahahaseConfig mConfig,
			ContentResolver contentResolver) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setDebugEnabled(true);
		builder.setOAuthConsumerKey(Constant.TWITTER_KEY);
		builder.setOAuthConsumerSecret(Constant.TWITTER_SECRET);
		builder.setGZIPEnabled(false);
		builder.setHttpConnectionTimeout(5000);
		builder.setIncludeEntitiesEnabled(true);
		builder.setIncludeRTsEnabled(true);
		builder.setUseSSL(true);

		TwitterFactory factory = new TwitterFactory(builder.build());
		mTwitter = factory.getInstance();
		AccessToken accessToken = new AccessToken(mConfig.accessToken,
				mConfig.consumerSecret);
		mTwitter.setOAuthAccessToken(accessToken);
		mContentResolver = contentResolver;
	}

	@Override
	public void fetchStatus() {
		new AsyncFetch().execute();
	}

	private int updateTwitEntryInDB(final Status status) {
		final ContentValues values = getContentValuesForTimelineEntry(status);
		int ret = mContentResolver.update(TimelineProvider.CONTENT_URI, values,
				TimelineConstant.T_ID + " = ?",
				new String[] { String.valueOf(status.getId()) });
		if (ret == 0)
			insertTwit2DB(values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "updateTwitEntryInDB(): " + ret);
		return ret;
	}

	private void insertTwit2DB(final ContentValues values) {
		Uri ret = mContentResolver.insert(TimelineProvider.CONTENT_URI, values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "tryToAddTwitToDB(): " + ret.getLastPathSegment());
	}

	private void insertTwitMentions2DB(final ContentValues values) {
		Uri ret = mContentResolver.insert(TimelineProvider.TWIT_MENTIONS_URI,
				values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "insertTwitMentions2DB(): " + ret.getLastPathSegment());
	}

	private int insertMentionEntity2DB(final UserMentionEntity mention) {
		final ContentValues values = getContentValuesForMentionEntity(mention);
		Uri ret = mContentResolver.insert(TimelineProvider.MENTION_URI, values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "insertMentionEntity2DB(): " + ret.getLastPathSegment());
		return Integer.parseInt(ret.getLastPathSegment());
	}

	private int insertHashTagEntity2DB(final HashtagEntity hashtag) {
		final ContentValues values = getContentValuesForHashEntity(hashtag);
		Uri ret = mContentResolver.insert(TimelineProvider.HASHTAG_URI, values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "insertHashtagEntity2DB(): " + ret.getLastPathSegment());
		return Integer.parseInt(ret.getLastPathSegment());
	}

	private void insertTwitHashtag2DB(final ContentValues values) {
		Uri ret = mContentResolver.insert(TimelineProvider.TWIT_HASHTAGS_URI,
				values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "insertTwitHashtag2DB(): " + ret.getLastPathSegment());
	}

	private int insertUrlEntity2DB(final URLEntity url) {
		final ContentValues values = getContentValuesForUrlEntity(url);
		Uri ret = mContentResolver.insert(TimelineProvider.URL_URI, values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "insertUrlEntity2DB(): " + ret.getLastPathSegment());
		return Integer.parseInt(ret.getLastPathSegment());
	}

	private void insertTwitUrl2DB(final ContentValues values) {
		Uri ret = mContentResolver
				.insert(TimelineProvider.TWIT_URL_URI, values);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "insertTwitUrl2DB(): " + ret.getLastPathSegment());
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

	private ContentValues getContentValuesForMentionEntity(
			final UserMentionEntity mention) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.M_MID, mention.getId());
		values.put(TimelineConstant.M_U_SN, mention.getScreenName());
		values.put(TimelineConstant.M_START, mention.getStart());
		values.put(TimelineConstant.M_END, mention.getEnd());
		return values;
	}

	private ContentValues getContentValuesForTwitMentions(final long twit_id,
			final long mention_id) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.T_ID, twit_id);
		values.put(TimelineConstant.M_ID, mention_id);
		return values;
	}

	private ContentValues getContentValuesForHashEntity(
			final HashtagEntity hashtag) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.H_START, hashtag.getStart());
		values.put(TimelineConstant.H_END, hashtag.getEnd());
		values.put(TimelineConstant.H_TEXT, hashtag.getText());
		return values;
	}

	private ContentValues getContentValuesForTwitHashtags(final long twit_id,
			final long hashtag_id) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.T_ID, twit_id);
		values.put(TimelineConstant.H_ID, hashtag_id);
		return values;
	}

	private ContentValues getContentValuesForUrlEntity(final URLEntity url) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.U_URL, url.getURL());
		values.put(TimelineConstant.U_DISPLAY, url.getDisplayURL());
		values.put(TimelineConstant.U_EXPANDED, url.getExpandedURL());
		values.put(TimelineConstant.U_START, url.getStart());
		values.put(TimelineConstant.U_END, url.getEnd());
		return values;
	}

	private ContentValues getContentValuesForTwitUrls(final long twit_id,
			final long url_id) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.T_ID, twit_id);
		values.put(TimelineConstant.U_ID, url_id);
		return values;
	}

	private class AsyncFetch extends AsyncTask<Void, Void, List<Status>> {

		@Override
		protected List<twitter4j.Status> doInBackground(Void... params) {
			Paging paging = new Paging(1, 40);
			try {
				List<twitter4j.Status> statuses = mTwitter
						.getHomeTimeline(paging);
				return statuses;
			} catch (TwitterException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<twitter4j.Status> statuses) {
			if (null != statuses) {
				for (twitter4j.Status status : statuses) {
					long status_id = status.getId();
					int ret = updateTwitEntryInDB(status);
					/* if not updating, store all entities */
					if (ret == 0) {
						/* User Mentions Entity */
						UserMentionEntity[] mentions = status
								.getUserMentionEntities();
						if (mentions.length > 0) {
							for (UserMentionEntity mention : mentions) {
								int mention_ret = insertMentionEntity2DB(mention);
								insertTwitMentions2DB(getContentValuesForTwitMentions(
										status_id, mention_ret));
							}
						}
						/* HashTag Entity */
						HashtagEntity[] hashtags = status.getHashtagEntities();
						if (hashtags.length > 0) {
							for (HashtagEntity hashtag : hashtags) {
								int hashtag_ret = insertHashTagEntity2DB(hashtag);
								insertTwitHashtag2DB(getContentValuesForTwitHashtags(
										status_id, hashtag_ret));
							}
						}
						/* URL Entity */
						URLEntity[] urls = status.getURLEntities();
						if (urls.length > 0) {
							for (URLEntity url : urls) {
								int url_ret = insertUrlEntity2DB(url);
								insertTwitUrl2DB(getContentValuesForTwitUrls(
										status_id, url_ret));
							}
						}
						/* Media Entity */
						// MediaEntity[] medias = status.getMediaEntities();
						// if (medias.length > 0) {
						// for (MediaEntity media : medias) {
						// }
						// }
					}
				}
				super.onPostExecute(statuses);
			}
		}
	}
}
