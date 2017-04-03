package com.getbase.android.db.test;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

public class TestContentProvider extends ContentProvider {
  public TestContentProvider() {
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public String getType(@NonNull Uri uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    Log.d("JCH", "query");
    MatrixCursor cursor = new MatrixCursor(new String[] { BaseColumns._ID });
    cursor.setNotificationUri(getContext().getContentResolver(), TestContract.BASE_URI);
    return cursor;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    throw new UnsupportedOperationException();
  }
}
