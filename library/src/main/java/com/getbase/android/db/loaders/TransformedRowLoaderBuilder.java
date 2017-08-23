package com.getbase.android.db.loaders;

import com.getbase.android.db.common.QueryData;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.support.v4.os.CancellationSignal;

import java.util.ArrayList;
import java.util.List;

public class TransformedRowLoaderBuilder<T> {

  private final QueryData queryData;
  private final ImmutableList<Uri> notificationUris;
  private final CancellableFunction<Cursor, T> cursorTransformation;

  TransformedRowLoaderBuilder(QueryData queryData, ImmutableList<Uri> notificationUris, CancellableFunction<Cursor, T> transformation) {
    this.queryData = queryData;
    this.notificationUris = notificationUris;
    this.cursorTransformation = transformation;
  }

  public <Out> TransformedRowLoaderBuilder<Out> transformRow(final Function<T, Out> rowTransformer) {
    return new TransformedRowLoaderBuilder<>(
        queryData,
        notificationUris,
        new ComposedCancellableFunction<>(cursorTransformation, new SimpleCancellableFunction<>(rowTransformer))
    );
  }

  public <Out> TransformedLoaderBuilder<Out> transform(final Function<List<T>, Out> transformer) {
    return new TransformedLoaderBuilder<>(
        queryData,
        notificationUris,
        new ComposedCancellableFunction<>(getEagerTransformationFunction(), new SimpleCancellableFunction<>(transformer)));
  }

  public TransformedRowLoaderBuilder<T> addNotificationUri(Uri uri) {
    return new TransformedRowLoaderBuilder<>(
        queryData,
        ImmutableList.<Uri>builder().addAll(notificationUris).add(uri).build(),
        cursorTransformation);
  }

  public Loader<List<T>> build(Context context) {
    return new ComposedCursorLoader<>(
        context,
        queryData,
        ImmutableList.copyOf(notificationUris),
        getEagerTransformationFunction());
  }

  @NonNull
  private CancellableFunction<Cursor, List<T>> getEagerTransformationFunction() {
    return new CancellableFunction<Cursor, List<T>>() {
      @Override
      public List<T> apply(Cursor input, CancellationSignal signal) {
        List<T> result = new ArrayList<>(input.getCount());
        for (input.moveToFirst(); !input.isAfterLast(); input.moveToNext()) {
          result.add(cursorTransformation.apply(input, signal));
        }
        return result;
      }
    };
  }
}
