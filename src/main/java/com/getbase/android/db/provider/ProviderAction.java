package com.getbase.android.db.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public abstract class ProviderAction<T> {

  public static Query query(Uri uri) {
    return new Query(uri);
  }

  public static Insert insert(Uri uri) {
    return new Insert(uri);
  }

  public static Delete delete(Uri uri) {
    return new Delete(uri);
  }

  public static Update update(Uri uri) {
    return new Update(uri);
  }

  private final Uri mUri;

  protected ProviderAction(Uri uri) {
    mUri = uri;
  }

  protected Uri getUri() {
    return mUri;
  }

  public T perform(Context context) {
    return perform(context.getContentResolver());
  }

  public abstract T perform(ContentResolver contentResolver);
}
