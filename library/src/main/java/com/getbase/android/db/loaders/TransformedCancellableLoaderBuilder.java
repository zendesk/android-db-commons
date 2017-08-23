package com.getbase.android.db.loaders;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

import com.getbase.android.db.common.QueryData;
import com.getbase.android.db.loaders.functions.FunctionWithCompletionListener;
import com.getbase.android.db.loaders.functions.FunctionWithCompletionListenerComposition;
import com.getbase.android.db.loaders.functions.SingleFunctionWithCompletionListener;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public class TransformedCancellableLoaderBuilder<To> {

  private final QueryData queryData;
  private final ImmutableList<Uri> notificationUris;
  private final FunctionWithCompletionListener<Cursor, To> wrapperFunction;

  TransformedCancellableLoaderBuilder(QueryData queryData, ImmutableList<Uri> notificationUris, FunctionWithCompletionListener<Cursor, To> wrapperFunction) {
    this.queryData = queryData;
    this.notificationUris = notificationUris;
    this.wrapperFunction = wrapperFunction;
  }

  public TransformedCancellableLoaderBuilder<To> addNotificationUri(Uri uri) {
    return new TransformedCancellableLoaderBuilder<>(queryData, ImmutableList.<Uri>builder().addAll(notificationUris).add(uri).build(), wrapperFunction);
  }

  public <NewTo> TransformedCancellableLoaderBuilder<NewTo> cancellableTransform(Function<To, NewTo> wrapper) {
    return new TransformedCancellableLoaderBuilder<>(queryData, notificationUris, new FunctionWithCompletionListenerComposition<>(SingleFunctionWithCompletionListener.wrapIfNeeded(wrapper), wrapperFunction));
  }

  public Loader<To> build(Context context) {
    return new ComposedCursorLoader<>(context, queryData, notificationUris, wrapperFunction, true);
  }
}
