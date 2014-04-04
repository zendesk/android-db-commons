package com.getbase.android.db.loaders;

import com.getbase.android.db.common.QueryData;
import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class CursorLoaderBuilder {

  public static CursorLoaderBuilder forUri(Uri uri) {
    return new CursorLoaderBuilder(uri);
  }

  private final Query query;

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

  public CursorLoaderBuilder orderBy(String orderBy) {
    query.orderBy(orderBy);
    return this;
  }

  public <Out> TransformedRowLoaderBuilder<Out> transformRow(Function<Cursor, Out> rowTransformer) {
    return new TransformedRowLoaderBuilder<Out>(query.getQueryData(), rowTransformer);
  }

  public <Out> TransformedLoaderBuilder<Out> transform(Function<Cursor, Out> transformer) {
    return new TransformedLoaderBuilder<Out>(query.getQueryData(), transformer);
  }

  public Loader<Cursor> build(Context context) {
    final QueryData queryData = query.getQueryData();
    final CursorLoader loader = new CursorLoader(context);
    loader.setUri(queryData.getUri());
    loader.setProjection(queryData.getProjection());
    loader.setSelection(queryData.getSelection());
    loader.setSelectionArgs(queryData.getSelectionArgs());
    loader.setSortOrder(queryData.getOrderBy());
    return loader;
  }
}
