package com.getbase.android.db.provider;

import com.getbase.android.db.common.QueryData;
import com.getbase.android.db.cursors.FluentCursor;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.Collection;

public class Query extends ProviderAction<FluentCursor> {

  private final Selection selection = new Selection();
  private final Projection projection = new Projection();
  private String orderBy;

  Query(Uri uri) {
    super(uri);
  }

  public Query projection(String... projection) {
    this.projection.append(projection);
    return this;
  }

  @SafeVarargs
  public final <T> Query where(String selection, T... selectionArgs) {
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Query query = (Query) o;

    return Objects.equal(getUri(), query.getUri()) &&
        Objects.equal(orderBy, query.orderBy) &&
        Objects.equal(projection, query.projection) &&
        Objects.equal(selection, query.selection);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        getUri(),
        projection,
        selection,
        orderBy
    );
  }
}