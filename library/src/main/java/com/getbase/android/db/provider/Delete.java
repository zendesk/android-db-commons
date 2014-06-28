package com.getbase.android.db.provider;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.Collection;

public class Delete extends ProviderAction<Integer> implements ConvertibleToOperation, Parcelable {

  public static final Creator<Delete> CREATOR = new Creator<Delete>() {
    @Override
    public Delete createFromParcel(Parcel parcel) {
      final Uri uri = parcel.readParcelable(null);
      final Delete delete = ProviderAction.delete(uri);
      final Selection selection = parcel.readParcelable(null);
      delete.selection = Objects.firstNonNull(selection, delete.selection);
      return delete;
    }

    @Override
    public Delete[] newArray(int size) {
      return new Delete[size];
    }
  };

  private Selection selection = new Selection();

  Delete(Uri uri) {
    super(uri);
  }

  public Delete where(String selection, Object... selectionArgs) {
    this.selection.append(selection, selectionArgs);
    return this;
  }

  public <T> Delete whereIn(String column, Collection<T> collection) {
    this.selection.append(column + " IN (" + Joiner.on(",").join(Collections2.transform(collection, Utils.toEscapedSqlFunction())) + ")");
    return this;
  }

  @Override
  public Integer perform(CrudHandler crudHandler) throws RemoteException {
    return crudHandler.delete(getUri(), selection.getSelection(), selection.getSelectionArgs());
  }

  @Override
  public ContentProviderOperation toContentProviderOperation() {
    return toContentProviderOperationBuilder().build();
  }

  @Override
  public Builder toContentProviderOperationBuilder() {
    return ContentProviderOperation.newDelete(getUri())
        .withSelection(selection.getSelection(), selection.getSelectionArgs());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeParcelable(getUri(), flags);
    parcel.writeParcelable(selection, flags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    Delete delete = (Delete) o;

    return Objects.equal(selection, delete.selection);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), selection);
  }
}
