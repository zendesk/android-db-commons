package com.github.partition.android.database.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class Query extends ProviderAction<Cursor> {

  private final Selection selection = new Selection();
  private final Projection projection = new Projection();
  private String orderBy;

  Query(Uri uri) {
    super(uri);
  }

  public Query projection(String... projection) {
    this.projection.append(projection);
    return this;
  }

  public Query where(String selection, String... selectionArgs) {
    this.selection.append(selection, selectionArgs);
    return this;
  }

  public Query orderBy(String orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  @Override
  public Cursor perform(ContentResolver contentResolver) {
    return contentResolver.query(getUri(),
        projection.getProjection(),
        selection.getSelection(),
        selection.getSelectionArgs(),
        orderBy
    );
  }
}