package com.getbase.android.db.provider;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.Collection;

public class Update extends ProviderAction<Integer> implements ConvertibleToOperation, Parcelable {

  public static final Creator<Update> CREATOR = new Creator<Update>() {
    @Override
    public Update createFromParcel(Parcel parcel) {
      final Uri uri = parcel.readParcelable(null);
      final Update update = ProviderAction.update(uri);
      final ContentValues values = parcel.readParcelable(null);
      update.values(values);
      final Selection selection = parcel.readParcelable(null);
      update.selection = Objects.firstNonNull(selection, update.selection);
      return update;
    }

    @Override
    public Update[] newArray(int size) {
      return new Update[size];
    }
  };

  private Selection selection = new Selection();
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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeParcelable(getUri(), flags);
    parcel.writeParcelable(values, flags);
    parcel.writeParcelable(selection, flags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    Update update = (Update) o;

    return Objects.equal(selection, update.selection) && Objects.equal(values, update.values);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), selection, values);
  }
}
