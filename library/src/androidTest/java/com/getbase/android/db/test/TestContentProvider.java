package com.getbase.android.db.test;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestContentProvider extends ContentProvider {

  private static QueryBlocker mQueryBlocker = null;
  private static Collection<Long> mData = null;

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
    blockQueryIfNeeded();
    MatrixCursor cursor = new MatrixCursor(new String[] { BaseColumns._ID });
    if (mData != null) {
      for (Long value : mData) {
        cursor.addRow(Collections.singleton(value));
      }
    }
    cursor.setNotificationUri(getContext().getContentResolver(), TestContract.BASE_URI);
    return cursor;
  }

  private void blockQueryIfNeeded() {
    if (mQueryBlocker != null) {
      try {
        mQueryBlocker.mStartLatch.countDown();
        mQueryBlocker.mProceedLatch.await(10, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
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

  public static void blockOnQuery() {
    mQueryBlocker = new QueryBlocker();
  }

  public static void proceedQuery() {
    mQueryBlocker.mProceedLatch.countDown();
  }

  public static void waitUntilQueryStarted() {
    try {
      mQueryBlocker.mStartLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void setDataForQuery(Collection<Long> data) {
    mData = data;
  }

  public static void clearDataForQuery() {
    mData = null;
  }

  private static class QueryBlocker {
    private CountDownLatch mStartLatch = new CountDownLatch(1);
    private CountDownLatch mProceedLatch = new CountDownLatch(1);
  }
}
