package com.getbase.android.db.loaders;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

import java.util.Collection;
import java.util.List;

public class CursorLoaderBuilder {

  public static CursorLoaderBuilder forUri(Uri uri) {
    return new CursorLoaderBuilder(uri);
  }

  private final Query query;
  private final List<Uri> notificationUris = Lists.newArrayList();

  private CursorLoaderBuilder(Uri uri) {
    this.query = ProviderAction.query(uri);
  }

  public CursorLoaderBuilder projection(String... projection) {
    query.projection(projection);
    return this;
  }

  public CursorLoaderBuilder where(String selection, Object... selectionArgs) {
    query.where(selection, selectionArgs);
    return this;
  }

  public <T> CursorLoaderBuilder whereIn(String column, Collection<T> collection) {
    query.whereIn(column, collection);
    return this;
  }

  public CursorLoaderBuilder orderBy(String orderBy) {
    query.orderBy(orderBy);
    return this;
  }

  public CursorLoaderBuilder addNotificationUri(Uri uri) {
    notificationUris.add(uri);
    return this;
  }

  public <Out> TransformedRowLoaderBuilder<Out> transformRow(Function<Cursor, Out> rowTransformer) {
    return new TransformedRowLoaderBuilder<Out>(query.getQueryData(), ImmutableList.copyOf(notificationUris), rowTransformer);
  }

  public <Out> TransformedLoaderBuilder<Out> transform(Function<Cursor, Out> transformer) {
    return new TransformedLoaderBuilder<Out>(query.getQueryData(), ImmutableList.copyOf(notificationUris), transformer);
  }

  public Loader<Cursor> build(Context context) {
    return new ComposedCursorLoader<Cursor>(
        context,
        query.getQueryData(),
        ImmutableList.copyOf(notificationUris),
        Functions.<Cursor>identity()
    );
  }
}
