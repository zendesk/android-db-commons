package com.getbase.android.db.common;

import android.net.Uri;

public class QueryData {

  private final Uri uri;
  private final String[] projection;
  private final String selection;
  private final String[] selectionArgs;
  private final String orderBy;

  public QueryData(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
    this.uri = uri;
    this.projection = projection;
    this.selection = selection;
    this.selectionArgs = selectionArgs;
    this.orderBy = orderBy;
  }

  public Uri getUri() {
    return uri;
  }

  public String[] getProjection() {
    return projection;
  }

  public String getSelection() {
    return selection;
  }

  public String[] getSelectionArgs() {
    return selectionArgs;
  }

  public String getOrderBy() {
    return orderBy;
  }
}
