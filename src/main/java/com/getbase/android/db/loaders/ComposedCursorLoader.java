package com.getbase.android.db.loaders;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.getbase.android.db.common.QueryData;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.WeakHashMap;

public class ComposedCursorLoader<T> extends AsyncTaskLoader<T> {
  final ForceLoadContentObserver mObserver;

  Uri mUri;
  String[] mProjection;
  String mSelection;
  String[] mSelectionArgs;
  String mSortOrder;

  T mResult;

  private final Function<Cursor, T> mCursorTransformation;
  private WeakHashMap<T, Cursor> cursorsForResults = new WeakHashMap<T, Cursor>();

  /* Runs on a worker thread */
  @Override
  public T loadInBackground() {
    final Cursor cursor = loadCursorInBackground();
    final T result = mCursorTransformation.apply(cursor);
    Preconditions.checkNotNull(result, "Function passed to this loader should never return null.");
    cursorsForResults.put(result, cursor);
    return result;
  }

  private Cursor loadCursorInBackground() {
    Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
        mSelectionArgs, mSortOrder);
    if (cursor != null) {
      // Ensure the cursor window is filled
      cursor.getCount();
      cursor.registerContentObserver(mObserver);
    }
    return cursor;
  }

  @Override
  public void deliverResult(T data) {
    final Cursor cursor = cursorsForResults.get(data);
    if (isReset()) {
      // An async query came in while the loader is stopped
      if (cursor != null) {
        cursor.close();
      }
      return;
    }
    T oldResult = mResult;
    mResult = data;

    if (isStarted()) {
      super.deliverResult(data);
    }

    if (oldResult != null) {
      Cursor oldCursor = cursorsForResults.get(oldResult);
      if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
        oldCursor.close();
      }
      cursorsForResults.remove(oldResult);
    }
  }

  public ComposedCursorLoader(Context context, QueryData queryData, Function<Cursor, T> cursorTransformation) {
    super(context);
    mObserver = new ForceLoadContentObserver();
    mUri = queryData.getUri();
    mProjection = queryData.getProjection();
    mSelection = queryData.getSelection();
    mSelectionArgs = queryData.getSelectionArgs();
    mSortOrder = queryData.getOrderBy();
    mCursorTransformation = cursorTransformation;
  }

  @Override
  protected void onStartLoading() {
    if (mResult != null) {
      deliverResult(mResult);
    }
    if (takeContentChanged() || mResult == null) {
      forceLoad();
    }
  }

  @Override
  protected void onStopLoading() {
    cancelLoad();
  }

  @Override
  public void onCanceled(T result) {
    Cursor cursor = cursorsForResults.get(result);
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
      cursorsForResults.remove(result);
    }
  }

  @Override
  protected void onReset() {
    super.onReset();
    onStopLoading();
    if (mResult != null) {
      Cursor cursor = cursorsForResults.get(mResult);
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
      cursorsForResults.remove(mResult);
      mResult = null;
    }
  }

  @Override
  public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
    super.dump(prefix, fd, writer, args);
    writer.print(prefix);
    writer.print("mUri=");
    writer.println(mUri);
    writer.print(prefix);
    writer.print("mProjection=");
    writer.println(Arrays.toString(mProjection));
    writer.print(prefix);
    writer.print("mSelection=");
    writer.println(mSelection);
    writer.print(prefix);
    writer.print("mSelectionArgs=");
    writer.println(Arrays.toString(mSelectionArgs));
    writer.print(prefix);
    writer.print("mSortOrder=");
    writer.println(mSortOrder);
    writer.print(prefix);
    writer.print("mResult=");
    writer.println(mResult);
  }
}
