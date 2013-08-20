package com.getbase.android.db.example.content;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PeopleProvider extends ContentProvider {

  private static UriMatcher sUriMatcher = new UriMatcher(0);

  public static final int PEOPLE_DIR = 1;

  static {
    sUriMatcher.addURI(Contract.AUTHORITY, Contract.Paths.PEOPLE, PEOPLE_DIR);
  }

  private Database mDatabase;

  @Override
  public boolean onCreate() {
    mDatabase = new Database(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
    switch (sUriMatcher.match(uri)) {
    case PEOPLE_DIR:
      final Cursor result = mDatabase.getReadableDatabase().query(Database.Tables.PEOPLE,
          projection,
          selection, selectionArgs,
          null, null, orderBy);
      result.setNotificationUri(getContentResolver(), Contract.People.CONTENT_URI);
      return result;
    default:
      throw new IllegalArgumentException("Couldn't match uri: " + uri);
    }
  }

  @Override
  public String getType(Uri uri) {
    switch (sUriMatcher.match(uri)) {
    case PEOPLE_DIR:
      return "vnd.android.cursor.dir/people";
    }
    throw new IllegalArgumentException("Couldn't match uri: uri");
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    switch (sUriMatcher.match(uri)) {
    case PEOPLE_DIR:
      final SQLiteDatabase db = mDatabase.getWritableDatabase();
      final long id = db.insert(Database.Tables.PEOPLE, null, contentValues);
      getContentResolver().notifyChange(Contract.People.CONTENT_URI, null);
      return Contract.People.buildUriForItem(id);
    }
    throw new IllegalArgumentException("Couldn't match uri: uri");
  }

  @Override
  public int delete(Uri uri, String s, String[] strings) {
    throw new UnsupportedOperationException("This operation is not yet supported");
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
    throw new UnsupportedOperationException("This operation is not yet supported");
  }

  private ContentResolver getContentResolver() {
    return getContext().getContentResolver();
  }
}
