package com.arm.ntahahasetwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieSyncManager;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private Twitter twitter;
	private RequestToken requestToken;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		loginIfSharedPreferencesIsEmpty();
		Log.i(TAG, "onCreate()");
	}

	private void loginIfSharedPreferencesIsEmpty() {
		String userToken = prefs.getString(Constant.ACCESS_TOKEN, null);
		if (null == userToken) {
			new AuthUserAsync().execute();
		} else {
			navigateToTimeline();
		}
	}

	private void navigateToTimeline() {
		NtahahaseApp mApp = (NtahahaseApp) getApplication();
		mApp.registerNtahahaseService();
		Intent timeline = new Intent(this, TimelineActivity.class);
		startActivity(timeline);
		this.finish();
	}

	class AuthUserAsync extends AsyncTask<Void, Void, String> {
		private boolean authDone = false;

		public boolean getListDone() {
			return authDone;
		}

		@Override
		protected String doInBackground(Void... params) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(Constant.TWITTER_KEY);
			builder.setOAuthConsumerSecret(Constant.TWITTER_SECRET);

			TwitterFactory factory = new TwitterFactory(builder.build());
			twitter = factory.getInstance();
			try {
				requestToken = twitter
						.getOAuthRequestToken(Constant.CALLBACK_URL);
			} catch (TwitterException e) {
				Log.e(TAG, "Caught TwitterException: " + e.getMessage());
			}
			return requestToken.getAuthenticationURL();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			CookieSyncManager.createInstance(getApplicationContext());
			new TwitterDialog(MainActivity.this, result, new DialogListener() {

				@Override
				public void onComplete(final String url) {
					CookieSyncManager.getInstance().sync();
					final Object syncToken = new Object();
					Thread t = new Thread(new Runnable() {
						
						@Override
						public void run() {
							synchronized (syncToken) {
								Uri uri = Uri.parse(url);
								String verifier = uri
										.getQueryParameter("oauth_verifier");
									// Get the access token
									AccessToken accessToken;
									try {
										accessToken = twitter
												.getOAuthAccessToken(requestToken,
														verifier);
										String token = accessToken.getToken();
										String tokenSecret = accessToken
												.getTokenSecret();
										Editor edit = prefs.edit();
										edit.putString(Constant.ACCESS_TOKEN, token);
										edit.putString(Constant.CONSUMER_SECRET,
												tokenSecret);
										edit.commit();
									} catch (TwitterException e) {
										Log.e(TAG, "Caught TwitterException: " + e.getMessage());
									} finally {
										syncToken.notify();
									}
							}
						}
					});
					t.start();
					synchronized (syncToken) {
						try {
							syncToken.wait();
							navigateToTimeline();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onCancel() {
					Log.d(TAG, "onCancel()");
				}
			}).show();
		}
	}
}