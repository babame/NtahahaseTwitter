package com.arm.ntahahasetwitter.data;

import java.util.ArrayList;

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
	public static final  String TABLE_TIMELINE = "timeline";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTH + "/" + TABLE_TIMELINE);
	
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	private static final int TIMELINE = 1;
	private static final int TIMELINE_ID = 2;
	
	static {
		uriMatcher.addURI(AUTH, "timeline", TIMELINE);
		uriMatcher.addURI(AUTH, "timeline/#", TIMELINE_ID);
	}
	
	private SQLiteOpenHelper mOpenHelper;

	/**
	 * Empty constructor
	 */
	public TimelineProvider() {}

	@Override
	public boolean onCreate() {
		mOpenHelper = new TimelineHelper(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		int match = uriMatcher.match(uri);
		switch (match) {
		case TIMELINE:
			return TimelineConstant.CONTENT_TYPE;
		case TIMELINE_ID:
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
		if (uriMatcher.match(uri) != TIMELINE) {
			throw new IllegalArgumentException("Cannot insert into URI: " + uri);
		}
		
		ContentValues newValues = (null != values) ? new ContentValues(values) : new ContentValues();
		
		for (String colName : TimelineConstant.getRequiredColumns()) {
			if (values.containsKey(colName) == false) {
				throw new IllegalArgumentException("Missing column: " + colName);
			}
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		long rowId = db.insert(TABLE_TIMELINE, TimelineConstant.T_ID, newValues);
		
		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + uri);
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
		case TIMELINE:
			qBuilder.setTables(TABLE_TIMELINE);
			break;
		case TIMELINE_ID:
			qBuilder.setTables(TABLE_TIMELINE);
			qBuilder.appendWhere(TimelineConstant.T_ID);
			qBuilder.appendWhere(uri.getPathSegments().get(1));
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder) && TIMELINE == match) {
			orderBy = TimelineConstant.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qBuilder.query(db, projection, selection, selectionArgs, groupBy, null, orderBy);
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
		
		switch (match) {
		case TIMELINE:
			count = db.update(TABLE_TIMELINE, values, selection, selectionArgs);
			break;
		case TIMELINE_ID:
			String segment = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			count = db.update(TABLE_TIMELINE, values, TimelineConstant.T_ID + " = " + rowId, selectionArgs);
		default:
			throw new UnsupportedOperationException("Cannot update URI: " + uri);
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
			String tableTwit = "create table " + TABLE_TIMELINE + " ("
					+ TimelineConstant.T_ID + " integer primary key, "
					+ TimelineConstant.T_CREATED_AT + " int, "
					+ TimelineConstant.T_USER + " text, "
					+ TimelineConstant.T_USER_SCREEN + " text, "
					+ TimelineConstant.T_TEXT + " text)";
			db.execSQL(tableTwit);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "onUpgrade: from " + oldVersion + " to " + newVersion);
			db.execSQL("drop table if exist " + TABLE_TIMELINE);
			onCreate(db);
		}
	}
	
	public static final class TimelineConstant implements BaseColumns {
		
		private TimelineConstant() {}
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ntahahasetwitter.timeline";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ntahahasetwitter.timeline";
		
		/* Timeline Table */
		public static final String T_ID = _ID;
		public static final String T_CREATED_AT = "twit_created_at";
		public static final String T_USER = "twit_user";
		public static final String T_USER_SCREEN = "twit_user_screen";
		public static final String T_TEXT = "twit_text";
		
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
