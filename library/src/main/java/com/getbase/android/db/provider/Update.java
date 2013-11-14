package com.getbase.android.db.provider;

import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;

public class Update extends ProviderAction<Integer> {

  private final Selection selection = new Selection();
  private ContentValues values = new ContentValues();

  Update(Uri uri) {
    super(uri);
  }

  public Update values(ContentValues values) {
    this.values.putAll(values);
    return this;
  }

  public Update value(String key, Object value) {
    Utils.addToContentValues(key, value, values);
    return this;
  }

  public Update where(String selection, Object... selectionArgs) {
    this.selection.append(selection, selectionArgs);
    return this;
  }

  @Override
  public Integer perform(CrudHandler crudHandler) throws RemoteException {
    return crudHandler.update(getUri(), values, selection.getSelection(), selection.getSelectionArgs());
  }
}
