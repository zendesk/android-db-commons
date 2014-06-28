package com.getbase.android.db.provider;

import com.getbase.android.db.common.QueryData;
import com.getbase.android.db.cursors.FluentCursor;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.Collection;

public class Query extends ProviderAction<FluentCursor> implements Parcelable {

  public static final Creator<Query> CREATOR = new Creator<Query>() {
    @Override
    public Query createFromParcel(Parcel parcel) {
      final Uri uri = parcel.readParcelable(null);
      final Query query = ProviderAction.query(uri);
      final Selection selection = parcel.readParcelable(null);
      final Projection projection = parcel.readParcelable(null);
      query.selection = Objects.firstNonNull(selection, query.selection);
      query.projection = Objects.firstNonNull(projection, query.projection);
      query.orderBy(parcel.readString());
      return query;
    }

    @Override
    public Query[] newArray(int size) {
      return new Query[size];
    }
  };

  private Selection selection = new Selection();
  private Projection projection = new Projection();
  private String orderBy;

  Query(Uri uri) {
    super(uri);
  }

  public Query projection(String... projection) {
    this.projection.append(projection);
    return this;
  }

  public Query where(String selection, Object... selectionArgs) {
    this.selection.append(selection, selectionArgs);
    return this;
  }

  public <T> Query whereIn(String column, Collection<T> collection) {
    this.selection.append(column + " IN (" + Joiner.on(",").join(Collections2.transform(collection, Utils.toEscapedSqlFunction())) + ")");
    return this;
  }

  public Query orderBy(String orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  public QueryData getQueryData() {
    return new QueryData(getUri(),
        projection.getProjection(),
        selection.getSelection(),
        selection.getSelectionArgs(),
        orderBy
    );
  }

  @Override
  public FluentCursor perform(CrudHandler crudHandler) throws RemoteException {
    final Cursor queryResult = crudHandler.query(getUri(),
        projection.getProjection(),
        selection.getSelection(),
        selection.getSelectionArgs(),
        orderBy
    );
    return new FluentCursor(queryResult);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeParcelable(getUri(), flags);
    parcel.writeParcelable(selection, flags);
    parcel.writeParcelable(projection, flags);
    parcel.writeString(orderBy);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    Query query = (Query) o;

    return Objects.equal(orderBy, query.orderBy)
        && Objects.equal(projection, query.projection)
        && Objects.equal(selection, query.selection);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), selection, projection, orderBy);
  }
}