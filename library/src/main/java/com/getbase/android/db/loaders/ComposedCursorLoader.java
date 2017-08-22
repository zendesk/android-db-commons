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
import com.getbase.android.db.loaders.functions.FunctionWithCompletionListener;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;
import android.support.v4.util.Pair;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ComposedCursorLoader<T> extends AbstractLoader<T> {

  private final DisableableContentObserver mObserver;
  private final ImmutableList<Uri> mNotificationUris;
  private boolean mAdditionalUrisRegistered;

  Uri mUri;
  String[] mProjection;
  String mSelection;
  String[] mSelectionArgs;
  String mSortOrder;
  boolean mTransformationCancellationSupported;

  @Nullable
  private CancellationSignal mCancellationSignal = null;
  private final Function<Cursor, T> mCursorTransformation;
  private IdentityLinkedMap<T, Cursor> cursorsForResults = new IdentityLinkedMap<>();
  private final List<Pair<T, Cursor>> pendingLoadResults = new LinkedList<>();

  @Override
  protected void onStartLoading() {
    mObserver.setEnabled(true);
    super.onStartLoading();
  }

  /* Runs on a worker thread */
  @Override
  public T loadInBackground() {
    Cursor cursor = null;
    try {
      initCancellationSignal();
      cursor = loadCursorInBackground();
      checkIfLoadNotCancelled();
      initOnTransformationCancellationIfSupported();
      final T result = mCursorTransformation.apply(cursor);
      Preconditions.checkNotNull(result, "Function passed to this loader should never return null.");

      synchronized (pendingLoadResults) {
        pendingLoadResults.add(new Pair<>(result, cursor));
      }

      return result;
    } catch (OperationCanceledException ex) {
      releaseCursor(cursor);
      throw ex;
    } catch (Throwable t) {
      throw new RuntimeException("Error occurred when running loader: " + this, t);
    } finally {
      destroyCancellationSignal();
    }
  }

  private void initOnTransformationCancellationIfSupported() {
    if (mTransformationCancellationSupported) {
      ((FunctionWithCompletionListener) mCursorTransformation).setCompletionListener(new FunctionWithCompletionListener.CompletionListener() {
        @Override
        public void onFunctionComplete() {
          checkIfLoadNotCancelled();
        }
      });
    }
  }

  private void initCancellationSignal() {
    synchronized (this) {
      if (isLoadInBackgroundCanceled()) {
        throw new OperationCanceledException();
      }
      mCancellationSignal = new CancellationSignal();
    }
  }

  private void destroyCancellationSignal() {
    synchronized (this) {
      mCancellationSignal = null;
    }
  }

  private void checkIfLoadNotCancelled() {
    synchronized (this) {
      if (mCancellationSignal != null) {
        mCancellationSignal.throwIfCanceled();
      }
    }
  }

  private Cursor loadCursorInBackground() {
    Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
        mSelectionArgs, mSortOrder);
    if (cursor != null) {
      // Ensure the cursor window is filled
      cursor.getCount();
    }
    return cursor;
  }

  @Override
  public void deliverResult(T result) {
    Cursor cursor = takePendingCursorForResult(result);
    if (cursor != null) {
      if (cursorsForResults.get(result) != null) {
        releaseCursor(cursor);
      } else {
        cursorsForResults.put(result, cursor);
      }
    }
    super.deliverResult(result);
  }

  @Override
  public void cancelLoadInBackground() {
    super.cancelLoadInBackground();
    synchronized (this) {
      if (mCancellationSignal != null) {
        mCancellationSignal.cancel();
      }
    }
  }

  @Override
  public void onCanceled(T result) {
    releaseCursor(takePendingCursorForResult(result));
  }

  @Nullable
  private Cursor takePendingCursorForResult(T result) {
    synchronized (pendingLoadResults) {
      Iterator<Pair<T, Cursor>> i = pendingLoadResults.iterator();
      while (i.hasNext()) {
        Pair<T, Cursor> entry = i.next();
        if (entry.first == result) {
          i.remove();
          return entry.second;
        }
      }
      return null;
    }
  }

  @Override
  protected void onNewDataDelivered(T data) {
    cursorsForResults.get(data).registerContentObserver(mObserver);
    if (!mAdditionalUrisRegistered) {
      ContentResolver resolver = getContext().getContentResolver();
      for (Uri notificationUri : mNotificationUris) {
        resolver.registerContentObserver(notificationUri, true, mObserver);
      }

      mAdditionalUrisRegistered = true;
    }
  }

  public ComposedCursorLoader(Context context, QueryData queryData, ImmutableList<Uri> notificationUris, Function<Cursor, T> cursorTransformation) {
    this(context, queryData, notificationUris, cursorTransformation, false);
  }

  public ComposedCursorLoader(Context context, QueryData queryData, ImmutableList<Uri> notificationUris, Function<Cursor, T> cursorTransformation, boolean supportTransformationCancellation) {
    super(context);
    mObserver = new DisableableContentObserver(new ForceLoadContentObserver());
    mUri = queryData.getUri();
    mProjection = queryData.getProjection();
    mSelection = queryData.getSelection();
    mSelectionArgs = queryData.getSelectionArgs();
    mSortOrder = queryData.getOrderBy();
    mCursorTransformation = cursorTransformation;
    mNotificationUris = notificationUris;
    mTransformationCancellationSupported = supportTransformationCancellation;
  }

  @Override
  protected void onAbandon() {
    mObserver.setEnabled(false);
    unregisterAdditionalUris();
  }

  @Override
  protected void onReset() {
    mObserver.setEnabled(false);
    unregisterAdditionalUris();
    super.onReset();
  }

  @Override
  protected void releaseResources(T result) {
    releaseCursor(cursorsForResults.remove(result));
  }

  private void unregisterAdditionalUris() {
    if (mAdditionalUrisRegistered) {
      getContext().getContentResolver().unregisterContentObserver(mObserver);
      mAdditionalUrisRegistered = false;
    }
  }

  private void releaseCursor(@Nullable Cursor cursor) {
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
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

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("mId", getId())
        .add("mUri", mUri)
        .add("mProjection", Arrays.toString(mProjection))
        .add("mSelection", mSelection)
        .add("mSelectionArgs", Arrays.toString(mSelectionArgs))
        .add("mSortOrder", mSortOrder)
        .toString();
  }
}
