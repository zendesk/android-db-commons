package com.getbase.android.db.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;

public class Insert extends ProviderAction<Uri> implements ConvertibleToOperation {

  private ContentValues contentValues = new ContentValues();

  Insert(Uri uri) {
    super(uri);
  }

  public Insert values(ContentValues contentValues) {
    this.contentValues.putAll(contentValues);
    return this;
  }

  public Insert value(String key, Object value) {
    Utils.addToContentValues(key, value, contentValues);
    return this;
  }

  @Override
  public Uri perform(CrudHandler crudHandler) throws RemoteException {
    return crudHandler.insert(getUri(), contentValues);
  }

  @Override
  public ContentProviderOperation toContentProviderOperation() {
    return toContentProviderOperationBuilder().build();
  }

  @Override
  public Builder toContentProviderOperationBuilder() {
    return ContentProviderOperation.newInsert(getUri()).withValues(contentValues);
  }
}
