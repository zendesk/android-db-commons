package com.getbase.android.db.fluentsqlite;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.getbase.android.db.fluentsqlite.Query.QueryBuilder;

import android.database.sqlite.SQLiteDatabase;

public final class ViewActions {
  private ViewActions() {
  }

  public static ViewSelector<ViewSelectStatementChooser> create() {
    return new CreateViewAction();
  }

  public static ViewSelector<ViewAction> dropIfExists() {
    return new DropViewAction();
  }

  public static class DropViewAction implements ViewSelector<ViewAction>, ViewAction {
    private String mView;

    DropViewAction() {
    }

    @Override
    public void perform(SQLiteDatabase db) {
      db.execSQL("DROP VIEW IF EXISTS " + mView);
    }

    @Override
    public ViewAction view(String view) {
      mView = checkNotNull(view);
      return this;
    }
  }

  public static class CreateViewAction implements ViewSelector<ViewSelectStatementChooser>, ViewAction, ViewSelectStatementChooser {
    private String mView;
    private RawQuery mQuery;

    CreateViewAction() {
    }

    @Override
    public void perform(SQLiteDatabase db) {
      db.execSQL("CREATE VIEW " + mView + " AS " + mQuery.mRawQuery);
    }

    @Override
    public ViewAction as(Query query) {
      checkNotNull(query);

      mQuery = query.toRawQuery();

      checkArgument(mQuery.mRawQueryArgs.isEmpty(), "Cannot use query with bound args for View creation");

      return this;
    }

    @Override
    public ViewAction as(QueryBuilder queryBuilder) {
      checkNotNull(queryBuilder);
      return as(queryBuilder.build());
    }

    @Override
    public ViewSelectStatementChooser view(String view) {
      mView = checkNotNull(view);
      return this;
    }
  }

  public interface ViewSelector<T> {
    T view(String view);
  }

  public interface ViewAction {
    void perform(SQLiteDatabase db);
  }

  public interface ViewSelectStatementChooser {
    ViewAction as(Query query);
    ViewAction as(QueryBuilder queryBuilder);
  }
}
