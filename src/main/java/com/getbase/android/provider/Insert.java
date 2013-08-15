package com.getbase.android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class Insert extends ProviderAction<Uri> {

  private ContentValues contentValues;

  Insert(Uri uri) {
    super(uri);
  }

  public Insert values(ContentValues contentValues) {
    this.contentValues = contentValues;
    return this;
  }

  @Override
  public Uri perform(ContentResolver contentResolver) {
    return contentResolver.insert(getUri(), contentValues);
  }
}
