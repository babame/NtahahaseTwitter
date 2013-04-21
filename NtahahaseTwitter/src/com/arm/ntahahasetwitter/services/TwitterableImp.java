package com.arm.ntahahasetwitter.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
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
	public void fetchStatus(int page, int count, long sinceId, long maxId) {
		Paging paging = new Paging();
		if (page > 0)
			paging.setPage(page);
		if (count > 0)
			paging.setCount(count);
		if (sinceId > 0)
			paging.setSinceId(sinceId);
		if (maxId > 0)
			paging.setMaxId(maxId);
		new AsyncFetch().execute(paging);
	}
	
	@Override
	public void updateStatus(String status) {
		new AsyncUpdateStatus().execute(status);
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

	private ContentValues getContentValuesForTimelineEntry(final Status entry) {
		final ContentValues values = new ContentValues();
		values.put(TimelineConstant.T_ID, entry.getId());
		values.put(TimelineConstant.T_CREATED_AT, entry.getCreatedAt()
				.getTime());
		if (entry.isRetweet()) {
			values.put(TimelineConstant.T_USER_SCREEN, entry
					.getRetweetedStatus().getUser().getScreenName());

			values.put(TimelineConstant.T_USER, entry.getRetweetedStatus()
					.getUser().getName());
		} else {
			values.put(TimelineConstant.T_USER_SCREEN, entry.getUser()
					.getScreenName());

			values.put(TimelineConstant.T_USER, entry.getUser().getName());
		}
		values.put(TimelineConstant.T_TEXT, entry.getText());
		values.put(TimelineConstant.T_IS_RETWEET, entry.isRetweet());
		String[] tokens;
		UserMentionEntity[] mentionEntities = null;
		HashtagEntity[] hashTagEntities = null;
		if (entry.isRetweet()) {
			tokens = entry.getRetweetedStatus().getText().split(" ");
			if (entry.getRetweetedStatus().getUserMentionEntities().length > 0)
				mentionEntities = entry.getRetweetedStatus()
						.getUserMentionEntities();
			if (entry.getRetweetedStatus().getHashtagEntities().length > 0)
				hashTagEntities = entry.getRetweetedStatus().getHashtagEntities();
		} else {
			tokens = entry.getText().split(" ");
			if (entry.getUserMentionEntities().length > 0)
				mentionEntities = entry.getUserMentionEntities();
			if (entry.getHashtagEntities().length > 0)
				hashTagEntities = entry.getHashtagEntities();
		}
		StringBuilder builder = new StringBuilder();
		for (String token : tokens) {
			boolean found = false;
			if (token.equals(" "))
				break;
			if (hashTagEntities != null) {
				for (HashtagEntity he : hashTagEntities) {
					String heText = "#" + he.getText();
					if (heText != null && token.contains(heText)) {
						int heStart = token.indexOf(heText);
						if (heStart > 0)
							builder.append(token.substring(0, heStart));
						builder.append("^^");
						builder.append(heText);
						builder.append("^^");
						if (token.length() > heText.length())
							builder.append(token.substring(heStart + heText.length(), token.length()));
						found = true;
						break;
					}
				}
			}
			if (!found && mentionEntities != null) {
				for (UserMentionEntity ume : mentionEntities) {
					String umeName = "@" + ume.getScreenName();
					if (umeName != null && token.contains(umeName)) {
						int umeStart = token.indexOf(umeName);
						if (umeStart > 0)
							builder.append(token.substring(0,
									umeStart));
						builder.append("*#");
						builder.append(umeName);
						builder.append("*#");
						if (token.length() > umeName.length())
							builder.append(token.substring(umeStart + umeName.length(), token.length()));
						found = true;
						break;
					}
				}
			}
			if (!found && entry.getMediaEntities().length > 0) {
				for (MediaEntity me : entry.getMediaEntities()) {
					String meURL = me.getURL();
					if (meURL != null && meURL.equals(token)
							&& me.getDisplayURL() != null) {
						builder.append("**");
						builder.append(me.getDisplayURL());
						builder.append("**");
						found = true;
						break;
					}
				}
			}
			if (!found && entry.getURLEntities().length > 0) {
				for (URLEntity ue : entry.getURLEntities()) {
					String ueURL = ue.getURL();
					if (ueURL != null && ueURL.equals(token)
							&& ue.getDisplayURL() != null) {
						builder.append("**");
						builder.append(ue.getDisplayURL());
						builder.append("**");
						found = true;
						break;
					}
				}
			}
			if (!found)
				builder.append(token);

			builder.append(" ");
		}
		values.put(TimelineConstant.T_SPANNED_TEXT, builder.toString());
		/* User Mentions Entity */
		UserMentionEntity[] mentions = entry.getUserMentionEntities();
		if (mentions.length > 0) {
			try {
				values.put(TimelineConstant.T_MENTION_ENTYTY,
						serializeObject(mentions));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* HashTag Entity */
		HashtagEntity[] hashtags = entry.getHashtagEntities();
		if (hashtags.length > 0) {
			try {
				values.put(TimelineConstant.T_HASHTAG_ENTITY,
						serializeObject(hashtags));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* URL Entity */
		URLEntity[] urls = entry.getURLEntities();
		if (urls.length > 0) {
			try {
				values.put(TimelineConstant.T_URL_ENTITY, serializeObject(urls));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* Media Entity */
		MediaEntity[] medias = entry.getMediaEntities();
		if (medias.length > 0) {
			try {
				values.put(TimelineConstant.T_MEDIA_ENTITY,
						serializeObject(medias));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return values;
	}

	private byte[] serializeObject(Object o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf;
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(o);
		out.close();

		// Get the bytes of the serialized object
		buf = bos.toByteArray();
		bos.close();
		return buf;
	}

	public static Object deserializeObject(byte[] b)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(
				new ByteArrayInputStream(b));
		Object object = in.readObject();
		in.close();

		return object;
	}

	private class AsyncFetch extends AsyncTask<Paging, Void, List<Status>> {

		@Override
		protected List<twitter4j.Status> doInBackground(Paging... params) {
			try {
				List<twitter4j.Status> statuses = mTwitter
						.getHomeTimeline(params[0]);
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
					updateTwitEntryInDB(status);
				}
				super.onPostExecute(statuses);
			}
		}
	}

	@Override
	public void fetchStatus() {
		new AsyncFetch().execute(new Paging());
	}
	
	private class AsyncUpdateStatus extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			try {
				mTwitter.updateStatus(params[0]);
				return 1;
			} catch (TwitterException e) {
				Log.d(TAG, "caught TwitterException: " + e.getMessage());
				return 0;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			Log.d(TAG, "result: " + result);
		}
	}
}
