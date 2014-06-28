package com.getbase.android.db.provider;

import com.google.common.base.Objects;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

public class Insert extends ProviderAction<Uri> implements ConvertibleToOperation, Parcelable {

  public static final Creator<Insert> CREATOR = new Creator<Insert>() {
    @Override
    public Insert createFromParcel(Parcel parcel) {
      Uri uri = parcel.readParcelable(null);
      final Insert insert = new Insert(uri);
      final ContentValues values = parcel.readParcelable(null);
      insert.values(values);
      return insert;
    }

    @Override
    public Insert[] newArray(int size) {
      return new Insert[size];
    }
  };

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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeParcelable(getUri(), flags);
    parcel.writeParcelable(contentValues, flags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    Insert insert = (Insert) o;

    return Objects.equal(contentValues, insert.contentValues);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(contentValues.valueSet(), super.hashCode());
  }
}
