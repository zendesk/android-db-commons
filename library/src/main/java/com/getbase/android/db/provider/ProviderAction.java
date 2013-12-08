package com.getbase.android.db.provider;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

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

  public T perform(ContentProvider contentProvider) {
    try {
      return perform(new ContentProviderCrudHandler(contentProvider));
    } catch (RemoteException e) {
      throw new RuntimeException("Unexpected exception", e);
    }
  }

  public T perform(Context context) {
    return perform(context.getContentResolver());
  }

  public T perform(ContentResolver contentResolver) {
    try {
      return perform(new ContentResolverCrudHandler(contentResolver));
    } catch (RemoteException e) {
      throw new RuntimeException("Unexpected exception: ", e);
    }
  }

  public T perform(ContentProviderClient contentProviderClient) throws RemoteException {
    return perform(new ContentProviderClientCrudHandler(contentProviderClient));
  }

  protected abstract T perform(CrudHandler crudHandler) throws RemoteException;
}
