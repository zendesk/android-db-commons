package com.getbase.android.db.query;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.getbase.android.db.provider.Utils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collections;
import java.util.List;

public class Insert {
  final String mTable;
  final String mNullColumnHack;
  final ContentValues mValues;
  final String mQueryFormString;

  private Insert(String table, String nullColumnHack, ContentValues values) {
    mTable = table;
    mNullColumnHack = nullColumnHack;
    mValues = values;
    mQueryFormString = null;
  }

  private Insert(String queryFormString) {
    mTable = null;
    mNullColumnHack = null;
    mValues = null;
    mQueryFormString = queryFormString;
  }

  public static InsertTableSelector insert() {
    return new InsertBuilder();
  }

  public Long perform(SQLiteDatabase db) {
    if (mQueryFormString == null) {
      return db.insert(mTable, mNullColumnHack, mValues);
    } else {
      db.execSQL(mQueryFormString);
      return null;
    }
  }

  public static class InsertBuilder implements InsertTableSelector, InsertFormSelector, InsertValuesBuilderForm {
    private String mTable;
    private String mNullColumnHack;
    private ContentValues mContentValues = new ContentValues();
    private List<String> mQueryFormColumns = Lists.newArrayList();

    InsertBuilder() {
    }

    @Override
    public InsertFormSelector into(String table) {
      mTable = checkNotNull(table);
      return this;
    }

    @Override
    public Insert defaultValues(String nullColumnHack) {
      mNullColumnHack = checkNotNull(nullColumnHack);
      return build();
    }

    @Override
    public InsertSubqueryForm columns(String... columns) {
      Collections.addAll(mQueryFormColumns, columns);

      return this;
    }

    @Override
    public Insert select(Query query) {
      checkNotNull(query);
      checkArgument(query.mRawQueryArgs.isEmpty());

      StringBuilder builder = new StringBuilder();
      builder.append("INSERT INTO ").append(mTable).append(" ");
      if (!mQueryFormColumns.isEmpty()) {
        builder
            .append("(")
            .append(Joiner.on(", ").join(mQueryFormColumns))
            .append(") ");
      }
      builder.append(query.mRawQuery);

      return new Insert(builder.toString());
    }

    @Override
    public InsertValuesBuilderForm values(ContentValues values) {
      mContentValues.putAll(values);
      return this;
    }

    @Override
    public InsertValuesBuilderForm value(String column, Object value) {
      Utils.addToContentValues(column, value, mContentValues);
      return this;
    }

    @Override
    public Insert build() {
      return new Insert(mTable, mNullColumnHack, mContentValues);
    }
  }

  public interface InsertTableSelector {
    InsertFormSelector into(String table);
  }

  public interface InsertFormSelector extends InsertValuesBuilder, InsertSubqueryForm {
    Insert defaultValues(String nullColumnHack);
  }

  public interface InsertSubqueryForm {
    InsertSubqueryForm columns(String... columns);
    Insert select(Query query);
  }

  public interface InsertValuesBuilder {
    InsertValuesBuilderForm values(ContentValues values);
    InsertValuesBuilderForm value(String column, Object value);
  }

  public interface InsertValuesBuilderForm extends InsertValuesBuilder {
    Insert build();
  }
}
