package com.getbase.android.db.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

import com.getbase.android.db.common.QueryData;
import com.getbase.android.db.cursors.Cursors;
import com.getbase.android.db.loaders.functions.FunctionWithCompletionListener;
import com.getbase.android.db.loaders.functions.FunctionWithCompletionListenerComposition;
import com.getbase.android.db.loaders.functions.SingleFunctionWithCompletionListener;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

public class TransformedRowCancellableLoaderBuilder<T> {

  private final QueryData queryData;
  private final ImmutableList<Uri> notificationUris;
  private final FunctionWithCompletionListener<Cursor, T> cursorTransformation;

  TransformedRowCancellableLoaderBuilder(QueryData queryData, ImmutableList<Uri> notificationUris, FunctionWithCompletionListener<Cursor, T> transformation) {
    this.queryData = queryData;
    this.notificationUris = notificationUris;
    this.cursorTransformation = transformation;
  }

  public <Out> TransformedRowCancellableLoaderBuilder<Out> cancellableTransformRow(final Function<T, Out> rowTransformer) {
    return new TransformedRowCancellableLoaderBuilder<>(queryData, notificationUris, new FunctionWithCompletionListenerComposition<>(SingleFunctionWithCompletionListener.wrapIfNeeded(rowTransformer), cursorTransformation));
  }

  public TransformedCancellableLoaderBuilder<List<T>> lazy() {
    return new TransformedCancellableLoaderBuilder<>(queryData, notificationUris, getLazyTransformationFunction());
  }

  public <Out> TransformedCancellableLoaderBuilder<Out> cancellableTransform(final Function<List<T>, Out> transformer) {
    return new TransformedCancellableLoaderBuilder<>(queryData, notificationUris, new FunctionWithCompletionListenerComposition<>(SingleFunctionWithCompletionListener.wrapIfNeeded(transformer), getEagerTransformationFunction()));
  }

  public TransformedRowCancellableLoaderBuilder<T> addNotificationUri(Uri uri) {
    return new TransformedRowCancellableLoaderBuilder<>(queryData, ImmutableList.<Uri>builder().addAll(notificationUris).add(uri).build(), cursorTransformation);
  }

  public Loader<List<T>> build(Context context) {
    return new ComposedCursorLoader<>(context, queryData, ImmutableList.copyOf(notificationUris), getEagerTransformationFunction(), true);
  }

  private FunctionWithCompletionListener<Cursor, List<T>> getEagerTransformationFunction() {
    return new FunctionWithCompletionListener<Cursor, List<T>>() {
      @Override
      public void setCompletionListener(CompletionListener listener) {
        cursorTransformation.setCompletionListener(listener);
      }

      @Override
      public List<T> apply(Cursor cursor) {
        return Lists.newArrayList(Cursors.toFluentIterable(cursor, cursorTransformation));
      }
    };
  }

  private FunctionWithCompletionListener<Cursor, List<T>> getLazyTransformationFunction() {
    return new FunctionWithCompletionListener<Cursor, List<T>>() {
      @Override
      public void setCompletionListener(CompletionListener listener) {
        cursorTransformation.setCompletionListener(listener);
      }

      @Override
      public List<T> apply(Cursor cursor) {
        return new LazyCursorList<>(cursor, cursorTransformation);
      }
    };
  }
}
