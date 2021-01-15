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

import java.util.Collection;
import java.util.List;

import androidx.loader.content.Loader;

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

  @SafeVarargs
  public final <T> CursorLoaderBuilder where(String selection, T... selectionArgs) {
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
    return new TransformedRowLoaderBuilder<>(
        query.getQueryData(),
        ImmutableList.copyOf(notificationUris),
        new SimpleCancellableFunction<>(rowTransformer));
  }

  public <Out> TransformedLoaderBuilder<Out> transform(Function<Cursor, Out> transformer) {
    return new TransformedLoaderBuilder<>(
        query.getQueryData(),
        ImmutableList.copyOf(notificationUris),
        new SimpleCancellableFunction<>(transformer));
  }

  public <Out> TransformedLoaderBuilder<Out> cancellableTransform(CancellableFunction<Cursor, Out> transformer) {
    return new TransformedLoaderBuilder<>(
        query.getQueryData(),
        ImmutableList.copyOf(notificationUris),
        transformer);
  }

  public Loader<Cursor> build(Context context) {
    return new ComposedCursorLoader<>(
        context,
        query.getQueryData(),
        ImmutableList.copyOf(notificationUris),
        new SimpleCancellableFunction<>(Functions.<Cursor>identity()));
  }
}
