package com.getbase.android.db.provider;

import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;

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
  public Uri perform(CrudHandler crudHandler) throws RemoteException {
    return crudHandler.insert(getUri(), contentValues);
  }
}
