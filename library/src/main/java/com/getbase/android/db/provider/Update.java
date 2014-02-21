package com.getbase.android.db.provider;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;

import java.util.Collection;

public class Update extends ProviderAction<Integer> implements ConvertibleToOperation {

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

  public <T> Update whereIn(String column, Collection<T> collection) {
    this.selection.append(column + " IN (" + Joiner.on(",").join(Collections2.transform(collection, Utils.toEscapedSqlFunction())) + ")");
    return this;
  }

  @Override
  public Integer perform(CrudHandler crudHandler) throws RemoteException {
    return crudHandler.update(getUri(), values, selection.getSelection(), selection.getSelectionArgs());
  }

  @Override
  public ContentProviderOperation toContentProviderOperation() {
    return toContentProviderOperationBuilder().build();
  }

  @Override
  public Builder toContentProviderOperationBuilder() {
    return ContentProviderOperation.newUpdate(getUri())
        .withSelection(selection.getSelection(), selection.getSelectionArgs())
        .withValues(values);
  }
}
