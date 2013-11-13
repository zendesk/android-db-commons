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
    if (this.contentValues == null) {
      this.contentValues = contentValues;
    } else {
      this.contentValues.putAll(contentValues);
    }
    return this;
  }

  public Insert value(String key, Object value) {
    if (contentValues == null) {
      contentValues = new ContentValues();
    }

    Utils.addToContentValues(key, value, contentValues);

    return this;
  }

  @Override
  public Uri perform(CrudHandler crudHandler) throws RemoteException {
    if (contentValues == null) {
      contentValues = new ContentValues();
    }
    return crudHandler.insert(getUri(), contentValues);
  }
}
