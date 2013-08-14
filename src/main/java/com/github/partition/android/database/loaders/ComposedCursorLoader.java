package com.github.partition.android.database.loaders;

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

import com.github.partition.android.database.common.QueryData;
import com.google.common.base.Function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class ComposedCursorLoader<T> extends AsyncTaskLoader<List<T>> {
  final ForceLoadContentObserver mObserver;

  Uri mUri;
  String[] mProjection;
  String mSelection;
  String[] mSelectionArgs;
  String mSortOrder;

  Cursor mCursor;

  private final Function<Cursor, T> mCursorTransformation;

  /* Runs on a worker thread */
  @Override
  public List<T> loadInBackground() {
    final Cursor cursor = loadCursorInBackground();
    return new LazyCursorList<T>(cursor, mCursorTransformation);
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
  public void deliverResult(List<T> data) {
    final LazyCursorList lazyCursorList = (LazyCursorList) data;
    Cursor cursor = lazyCursorList.getCursor();
    if (isReset()) {
      // An async query came in while the loader is stopped
      if (cursor != null) {
        cursor.close();
      }
      return;
    }
    Cursor oldCursor = mCursor;
    mCursor = cursor;

    if (isStarted()) {
      super.deliverResult(data);
    }

    if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
      oldCursor.close();
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
    if (mCursor != null) {
      // TODO - caching
      deliverResult(new LazyCursorList<T>(mCursor, mCursorTransformation));
    }
    if (takeContentChanged() || mCursor == null) {
      forceLoad();
    }
  }

  @Override
  protected void onStopLoading() {
    cancelLoad();
  }

  @Override
  public void onCanceled(List<T> list) {
    Cursor cursor = ((LazyCursorList<T>)list).getCursor();
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
  }

  @Override
  protected void onReset() {
    super.onReset();
    onStopLoading();
    if (mCursor != null && !mCursor.isClosed()) {
      mCursor.close();
    }
    mCursor = null;
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
    writer.print("mCursor=");
    writer.println(mCursor);
  }
}
