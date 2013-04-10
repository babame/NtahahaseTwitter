package com.arm.ntahahasetwitter.data;

import java.util.ArrayList;

import com.arm.ntahahasetwitter.BuildConfig;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class TimelineProvider extends ContentProvider {
	private static final String TAG = TimelineProvider.class.getSimpleName();

	public static final String AUTH = "com.arm.ntahahasetwitter.provider.timeline";
	public static final String TABLE_TWIT = "twits";
	public static final String TABLE_MENTIONS = "mentions";
	public static final String TABLE_TWIT_MENTIONS = "twit_mentions";
	public static final String TABLE_HASHTAGS = "hashtags";
	public static final String TABLE_TWIT_HASHTAG = "twit_hashtag";
	public static final String TABLE_URLS = "urls";
	public static final String TABLE_TWIT_URLS = "twit_urls";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTH + "/"
			+ TABLE_TWIT);
	public static final Uri MENTION_URI = Uri.parse("content://" + AUTH + "/"
			+ TABLE_MENTIONS);
	public static final Uri TWIT_MENTIONS_URI = Uri.parse("content://" + AUTH
			+ "/" + TABLE_TWIT_MENTIONS);
	public static final Uri HASHTAG_URI = Uri.parse("content://" + AUTH + "/"
			+ TABLE_HASHTAGS);
	public static final Uri TWIT_HASHTAGS_URI = Uri.parse("content://" + AUTH
			+ "/" + TABLE_TWIT_HASHTAG);
	public static final Uri URL_URI = Uri.parse("content://" + AUTH + "/"
			+ TABLE_URLS);
	public static final Uri TWIT_URL_URI = Uri.parse("content://" + AUTH + "/"
			+ TABLE_TWIT_URLS);

	private static final UriMatcher uriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int TWITS = 1;
	private static final int TWIT_ID = 2;
	private static final int MENTIONS = 3;
	private static final int MENTION_ID = 4;
	private static final int TWIT_MENTIONS = 5;
	private static final int TWIT_MENTION_ID = 6;
	private static final int HASHTAGS = 7;
	private static final int HASHTAG_ID = 8;
	private static final int TWIT_HASHTAGS = 9;
	private static final int TWIT_HASHTAG_ID = 10;
	private static final int URLS = 11;
	private static final int URL_ID = 12;
	private static final int TWIT_URLS = 13;
	private static final int TWIT_URL_ID = 14;

	static {
		uriMatcher.addURI(AUTH, TABLE_TWIT, TWITS);
		uriMatcher.addURI(AUTH, TABLE_TWIT + "/#", TWIT_ID);
		uriMatcher.addURI(AUTH, TABLE_MENTIONS, MENTIONS);
		uriMatcher.addURI(AUTH, TABLE_MENTIONS + "/#", MENTION_ID);
		uriMatcher.addURI(AUTH, TABLE_TWIT_MENTIONS, TWIT_MENTIONS);
		uriMatcher.addURI(AUTH, TABLE_TWIT_MENTIONS + "/#", TWIT_MENTION_ID);
		uriMatcher.addURI(AUTH, TABLE_HASHTAGS, HASHTAGS);
		uriMatcher.addURI(AUTH, TABLE_HASHTAGS + "/#", HASHTAG_ID);
		uriMatcher.addURI(AUTH, TABLE_TWIT_HASHTAG, TWIT_HASHTAGS);
		uriMatcher.addURI(AUTH, TABLE_TWIT_HASHTAG + "/#", TWIT_HASHTAG_ID);
		uriMatcher.addURI(AUTH, TABLE_URLS, URLS);
		uriMatcher.addURI(AUTH, TABLE_URLS + "/#", URL_ID);
		uriMatcher.addURI(AUTH, TABLE_TWIT_URLS, TWIT_URLS);
		uriMatcher.addURI(AUTH, TABLE_TWIT_URLS + "/#", TWIT_URL_ID);
	}

	private SQLiteOpenHelper mOpenHelper;

	/**
	 * Empty constructor
	 */
	public TimelineProvider() {
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new TimelineHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		int match = uriMatcher.match(uri);
		switch (match) {
		case TWITS:
			return TimelineConstant.CONTENT_TYPE;
		case TWIT_ID:
			return TimelineConstant.CONTENT_ITEM_TYPE;
		case MENTIONS:
			return TimelineConstant.CONTENT_TYPE;
		case MENTION_ID:
			return TimelineConstant.CONTENT_ITEM_TYPE;
		case TWIT_MENTIONS:
			return TimelineConstant.CONTENT_TYPE;
		case TWIT_MENTION_ID:
			return TimelineConstant.CONTENT_ITEM_TYPE;
		case HASHTAGS:
			return TimelineConstant.CONTENT_TYPE;
		case HASHTAG_ID:
			return TimelineConstant.CONTENT_ITEM_TYPE;
		case TWIT_HASHTAGS:
			return TimelineConstant.CONTENT_TYPE;
		case TWIT_HASHTAG_ID:
			return TimelineConstant.CONTENT_ITEM_TYPE;
		case URLS:
			return TimelineConstant.CONTENT_TYPE;
		case URL_ID:
			return TimelineConstant.CONTENT_ITEM_TYPE;
		case TWIT_URLS:
			return TimelineConstant.CONTENT_TYPE;
		case TWIT_URL_ID:
			return TimelineConstant.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = uriMatcher.match(uri);
		Log.d(TAG, "match: " + match);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = 0;
		try {
			switch (match) {
			case TWITS:
				ContentValues newValues = (null != values) ? new ContentValues(
						values) : new ContentValues();
				rowId = db.insert(TABLE_TWIT, TimelineConstant.T_ID, newValues);
				for (String colName : TimelineConstant.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: "
								+ colName);
					}
				}
				break;
			case MENTIONS:
				rowId = db
						.insert(TABLE_MENTIONS, TimelineConstant.M_ID, values);
				break;
			case TWIT_MENTIONS:
				rowId = db.insert(TABLE_TWIT_MENTIONS, TimelineConstant.TM_ID,
						values);
				break;
			case HASHTAGS:
				rowId = db
						.insert(TABLE_HASHTAGS, TimelineConstant.H_ID, values);
				break;
			case TWIT_HASHTAGS:
				rowId = db.insert(TABLE_TWIT_HASHTAG, TimelineConstant.TH_ID,
						values);
				break;
			case URLS:
				rowId = db.insert(TABLE_URLS, TimelineConstant.U_ID, values);
				break;
			case TWIT_URLS:
				rowId = db.insert(TABLE_TWIT_URLS, TimelineConstant.TU_ID,
						values);
				break;
			default:
				throw new IllegalArgumentException("Cannot insert into URI: "
						+ uri);
			}
			if (rowId < 0) {
				throw new SQLException("Failed to insert row into " + uri);
			}
		} finally {
			db.close();
		}
		Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		return noteUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = uriMatcher.match(uri);
		String groupBy = null;

		switch (match) {
		case TWITS:
			qBuilder.setTables(TABLE_TWIT);
			break;
		case TWIT_ID:
			qBuilder.setTables(TABLE_TWIT);
			qBuilder.appendWhere(TimelineConstant.T_ID);
			qBuilder.appendWhere(uri.getPathSegments().get(1));
			break;
		case MENTIONS:
			qBuilder.setTables(TABLE_MENTIONS);
			break;
		case MENTION_ID:
			qBuilder.setTables(TABLE_MENTIONS);
			qBuilder.appendWhere(TimelineConstant.M_ID);
			qBuilder.appendWhere(uri.getPathSegments().get(1));
			break;
		case TWIT_MENTIONS:
			String mentionJunction = TimelineProvider.TABLE_TWIT_MENTIONS
					+ " join " + TimelineProvider.TABLE_TWIT + " on "
					+ TimelineProvider.TABLE_TWIT_MENTIONS + "."
					+ TimelineConstant.T_ID + " = "
					+ TimelineProvider.TABLE_TWIT + "." + TimelineConstant.T_ID
					+ " join " + TimelineProvider.TABLE_MENTIONS + " on "
					+ TimelineProvider.TABLE_TWIT_MENTIONS + "."
					+ TimelineConstant.M_ID + " = "
					+ TimelineProvider.TABLE_MENTIONS + "."
					+ TimelineConstant.M_ID;
			qBuilder.setTables(mentionJunction);
			break;
		case HASHTAGS:
			qBuilder.setTables(TABLE_HASHTAGS);
			break;
		case HASHTAG_ID:
			qBuilder.setTables(TABLE_HASHTAGS);
			qBuilder.appendWhere(TimelineConstant.H_ID);
			qBuilder.appendWhere(uri.getPathSegments().get(1));
			break;
		case TWIT_HASHTAGS:
			String hashtagJunction = TimelineProvider.TABLE_TWIT_HASHTAG
					+ " join " + TimelineProvider.TABLE_TWIT + " on "
					+ TimelineProvider.TABLE_TWIT_HASHTAG + "."
					+ TimelineConstant.T_ID + " = "
					+ TimelineProvider.TABLE_TWIT + "." + TimelineConstant.T_ID
					+ " join " + TimelineProvider.TABLE_HASHTAGS + " on "
					+ TimelineProvider.TABLE_TWIT_HASHTAG + "."
					+ TimelineConstant.H_ID + " = "
					+ TimelineProvider.TABLE_HASHTAGS + "."
					+ TimelineConstant.H_ID;
			qBuilder.setTables(hashtagJunction);
			break;
		case URLS:
			qBuilder.setTables(TABLE_URLS);
			break;
		case URL_ID:
			qBuilder.setTables(TABLE_URLS);
			qBuilder.appendWhere(TimelineConstant.U_ID);
			qBuilder.appendWhere(uri.getPathSegments().get(1));
			break;
		case TWIT_URLS:
			String urlJuction = TimelineProvider.TABLE_TWIT_URLS + " join "
					+ TimelineProvider.TABLE_TWIT + " on "
					+ TimelineProvider.TABLE_TWIT_URLS + "."
					+ TimelineConstant.T_ID + " = "
					+ TimelineProvider.TABLE_TWIT + "." + TimelineConstant.T_ID
					+ " join " + TimelineProvider.TABLE_URLS + " on "
					+ TimelineProvider.TABLE_TWIT_URLS + "."
					+ TimelineConstant.U_ID + " = "
					+ TimelineProvider.TABLE_URLS + "." + TimelineConstant.U_ID;
			qBuilder.setTables(urlJuction);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder) && TWITS == match) {
			orderBy = TimelineConstant.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = null;
		ret = qBuilder.query(db, projection, selection, selectionArgs, groupBy,
				null, orderBy);
		if (null == ret) {
			Log.e(TAG, "TimelineProvider.query: failed");
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		}

		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
		long rowId = 0;
		int match = uriMatcher.match(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		try {
			String segment = null;
			switch (match) {
			case TWITS:
				count = db.update(TABLE_TWIT, values, selection, selectionArgs);
				break;
			case TWIT_ID:
				segment = uri.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_TWIT, values, TimelineConstant.T_ID
						+ " = " + rowId, selectionArgs);
			case MENTIONS:
				count = db.update(TABLE_MENTIONS, values, selection,
						selectionArgs);
				break;
			case MENTION_ID:
				segment = uri.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_MENTIONS, values, TimelineConstant.M_ID
						+ " = " + rowId, selectionArgs);
			default:
				throw new UnsupportedOperationException("Cannot update URI: "
						+ uri);
			}
		} finally {
			db.close();
		}
		return count;
	}

	private static class TimelineHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "timeline.db";
		private static final int DATABASE_VERSION = 4;

		public TimelineHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String tableTwit = "create table " + TABLE_TWIT + " ("
					+ TimelineConstant.T_ID + " integer primary key, "
					+ TimelineConstant.T_CREATED_AT + " int, "
					+ TimelineConstant.T_USER + " text, "
					+ TimelineConstant.T_USER_SCREEN + " text, "
					+ TimelineConstant.T_TEXT + " text)";

			String tableMentions = "create table " + TABLE_MENTIONS + " ("
					+ TimelineConstant.M_ID
					+ " integer primary key autoincrement, "
					+ TimelineConstant.M_U_SN + " text, "
					+ TimelineConstant.M_START + " integer, "
					+ TimelineConstant.M_END + " integer, "
					+ TimelineConstant.M_MID + " integer)";

			String tableHashTags = "create table " + TABLE_HASHTAGS + " ("
					+ TimelineConstant.H_ID
					+ " integer primary key autoincrement, "
					+ TimelineConstant.H_START + " integer, "
					+ TimelineConstant.H_END + " integer, "
					+ TimelineConstant.H_TEXT + " text)";

			String tableURLs = "create table " + TABLE_URLS + " ("
					+ TimelineConstant.U_ID
					+ " integer primary key autoincrement, "
					+ TimelineConstant.U_URL + " text, "
					+ TimelineConstant.U_DISPLAY + " text, "
					+ TimelineConstant.U_EXPANDED + " text, "
					+ TimelineConstant.U_START + " integer, "
					+ TimelineConstant.U_END + " integer)";

			String tableTimelineMentions = "create table "
					+ TABLE_TWIT_MENTIONS + " (" + TimelineConstant.T_ID
					+ " not null, " + TimelineConstant.M_ID + " not null, "
					+ "constraint " + TimelineConstant.TM_ID + " primary key ("
					+ TimelineConstant.T_ID + ", " + TimelineConstant.M_ID
					+ "), foreign key (" + TimelineConstant.T_ID
					+ ") references " + TABLE_TWIT + " ("
					+ TimelineConstant.T_ID + "), foreign key ("
					+ TimelineConstant.M_ID + ") references " + TABLE_MENTIONS
					+ " (" + TimelineConstant.M_ID + "))";

			String tableTimelineHashtags = "create table " + TABLE_TWIT_HASHTAG
					+ " (" + TimelineConstant.T_ID + " not null, "
					+ TimelineConstant.H_ID + " not null, " + "constraint "
					+ TimelineConstant.TH_ID + " primary key ("
					+ TimelineConstant.T_ID + ", " + TimelineConstant.H_ID
					+ "), foreign key (" + TimelineConstant.T_ID
					+ ") references " + TABLE_TWIT + " ("
					+ TimelineConstant.T_ID + "), foreign key ("
					+ TimelineConstant.H_ID + ") references " + TABLE_HASHTAGS
					+ " (" + TimelineConstant.H_ID + "))";

			String tableTimelineURLs = "create table " + TABLE_TWIT_URLS + " ("
					+ TimelineConstant.T_ID + " not null, "
					+ TimelineConstant.U_ID + " not null, " + "constraint "
					+ TimelineConstant.TU_ID + " primary key ("
					+ TimelineConstant.T_ID + ", " + TimelineConstant.U_ID
					+ "), foreign key (" + TimelineConstant.T_ID
					+ ") references " + TABLE_TWIT + " ("
					+ TimelineConstant.T_ID + "), foreign key ("
					+ TimelineConstant.U_ID + ") references " + TABLE_URLS
					+ " (" + TimelineConstant.U_ID + "))";

			if (BuildConfig.DEBUG)
				Log.d(TAG,
						String.format(
								"table twit query command:\n%s\ntable mentions query commnad:"
										+ "\n%s\ntable timeline_mentions query command:\n%s\ntable hashtags query command:\n%s\n"
										+ "table timeline_hashtags query command:\n%s\ntable_urls query command:\n%s\ntable_timeline_urls query command:\n%s",
								tableTwit, tableMentions,
								tableTimelineMentions, tableHashTags,
								tableTimelineHashtags, tableURLs,
								tableTimelineURLs));
			db.execSQL(tableTwit);
			db.execSQL(tableMentions);
			db.execSQL(tableTimelineMentions);
			db.execSQL(tableHashTags);
			db.execSQL(tableTimelineHashtags);
			db.execSQL(tableURLs);
			db.execSQL(tableTimelineURLs);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "onUpgrade: from " + oldVersion + " to "
						+ newVersion);
			db.execSQL("drop table if exist " + TABLE_TWIT);
			onCreate(db);
		}
	}

	public static final class TimelineConstant implements BaseColumns {

		private TimelineConstant() {
		}

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ntahahasetwitter.timeline";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ntahahasetwitter.timeline";

		/* Timeline Table */
		public static final String T_ID = "twit_id";
		public static final String T_CREATED_AT = "twit_created_at";
		public static final String T_USER = "twit_user";
		public static final String T_USER_SCREEN = "twit_user_screen";
		public static final String T_TEXT = "twit_text";

		/* Mention Table */
		public static final String M_ID = _ID;
		public static final String M_U_SN = "user_screen_name";
		public static final String M_START = "mention_start";
		public static final String M_END = "mention_end";
		public static final String M_MID = "mention_id";

		/* HashTag Table */
		public static final String H_ID = _ID;
		public static final String H_START = "hash_start";
		public static final String H_END = "hash_end";
		public static final String H_TEXT = "hash_text";

		/* Url Table */
		public static final String U_ID = _ID;
		public static final String U_START = "url_start";
		public static final String U_END = "url_end";
		public static final String U_URL = "url_text";
		public static final String U_DISPLAY = "url_display";
		public static final String U_EXPANDED = "url_expanded";

		/* Media Table (P = picture) */
		public static final String P_ID = _ID;
		public static final String P_MID = "media_id";
		public static final String P_START = "media_start";
		public static final String P_END = "media_end";
		public static final String P_TYPE = "media_type";
		public static final String P_URL = "media_url";
		public static final String P_URLS = "media_https"; // secure
		public static final String P_DISPLAY = "media_url_display";
		public static final String P_EXPANDED = "media_url_expanded";

		/* Timeline-Mention Junction Table ID */
		public static final String TM_ID = _ID;
		/* Timeline-HashTag Junction Table ID */
		public static final String TH_ID = _ID;
		/* Timeline-URL Junction Table ID */
		public static final String TU_ID = _ID;
		/* Timeline-Media Junction Table ID */
		public static final String TP_ID = _ID;

		public static final String DEFAULT_SORT_ORDER = T_CREATED_AT + " DESC";

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();
			tmpList.add(T_ID);
			tmpList.add(T_CREATED_AT);
			tmpList.add(T_USER);
			tmpList.add(T_USER_SCREEN);
			tmpList.add(T_TEXT);
			return tmpList;
		}
	}
}
