package com.getbase.android.db.example.content;

import com.getbase.android.db.query.Insert;
import com.getbase.android.db.query.Query;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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
      final Cursor result = Query.select()
          .columns(projection)
          .from(Database.Tables.PEOPLE)
          .where(selection, selectionArgs)
          .orderBy(orderBy)
          .build()
          .perform(mDatabase.getReadableDatabase());

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
      final long id = Insert.insert()
          .into(Database.Tables.PEOPLE)
          .values(contentValues)
          .build()
          .perform(mDatabase.getWritableDatabase());

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
