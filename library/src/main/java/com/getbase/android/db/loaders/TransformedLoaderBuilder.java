package com.getbase.android.db.loaders;

import com.getbase.android.db.common.QueryData;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

public class TransformedLoaderBuilder<To> {

  private final QueryData queryData;
  private final ImmutableList<Uri> notificationUris;
  private final Function<Cursor, To> wrapperFunction;

  public TransformedLoaderBuilder(QueryData queryData, ImmutableList<Uri> notificationUris, Function<Cursor, To> wrapperFunction) {
    this.queryData = queryData;
    this.notificationUris = notificationUris;
    this.wrapperFunction = wrapperFunction;
  }

  public TransformedLoaderBuilder<To> addNotificationUri(Uri uri) {
    return new TransformedLoaderBuilder<To>(queryData, ImmutableList.<Uri>builder().addAll(notificationUris).add(uri).build(), wrapperFunction);
  }

  public <NewTo> TransformedLoaderBuilder<NewTo> transform(Function<To, NewTo> wrapper) {
    return new TransformedLoaderBuilder<NewTo>(queryData, notificationUris, Functions.compose(wrapper, wrapperFunction));
  }

  public Loader<To> build(Context context) {
    return new ComposedCursorLoader<To>(context, queryData, notificationUris, wrapperFunction);
  }
}
