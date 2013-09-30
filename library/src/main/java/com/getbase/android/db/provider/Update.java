package com.getbase.android.db.provider;

import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;

public class Update extends ProviderAction<Integer> {

  private final Selection selection = new Selection();
  private ContentValues values;

  Update(Uri uri) {
    super(uri);
  }

  public Update values(ContentValues values) {
    if (this.values == null) {
      this.values = values;
    } else {
      this.values.putAll(values);
    }
    return this;
  }

  public Update value(String key, Object value) {
    if (values == null) {
      values = new ContentValues();
    }
    if (value == null) {
      values.putNull(key);
    } else if (value instanceof String) {
      values.put(key, (String) value);
    } else if (value instanceof Byte) {
      values.put(key, (Byte) value);
    } else if (value instanceof Short) {
      values.put(key, (Short) value);
    } else if (value instanceof Integer) {
      values.put(key, (Integer) value);
    } else if (value instanceof Long) {
      values.put(key, (Long) value);
    } else if (value instanceof Float) {
      values.put(key, (Float) value);
    } else if (value instanceof Double) {
      values.put(key, (Double) value);
    } else if (value instanceof Boolean) {
      values.put(key, (Boolean) value);
    } else if (value instanceof byte[]) {
      values.put(key, (byte[]) value);
    } else {
      throw new IllegalArgumentException("bad value type: " + value.getClass().getName());
    }
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
