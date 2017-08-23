package com.getbase.android.db.loaders;

import com.getbase.android.db.common.QueryData;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

public class TransformedLoaderBuilder<To> {

  private final QueryData queryData;
  private final ImmutableList<Uri> notificationUris;
  private final CancellableFunction<Cursor, To> cursorTransformation;

  TransformedLoaderBuilder(QueryData queryData, ImmutableList<Uri> notificationUris, CancellableFunction<Cursor, To> transformer) {
    this.queryData = queryData;
    this.notificationUris = notificationUris;
    this.cursorTransformation = transformer;
  }

  public TransformedLoaderBuilder<To> addNotificationUri(Uri uri) {
    return new TransformedLoaderBuilder<>(queryData, ImmutableList.<Uri>builder().addAll(notificationUris).add(uri).build(), cursorTransformation);
  }

  public <NewTo> TransformedLoaderBuilder<NewTo> transform(Function<To, NewTo> transformer) {
    return new TransformedLoaderBuilder<>(
        queryData,
        notificationUris,
        new ComposedCancellableFunction<>(cursorTransformation, new SimpleCancellableFunction<>(transformer)));
  }

  public Loader<To> build(Context context) {
    return new ComposedCursorLoader<>(context, queryData, notificationUris, cursorTransformation);
  }
}
