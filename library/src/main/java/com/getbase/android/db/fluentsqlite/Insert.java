package com.getbase.android.db.fluentsqlite;

import static com.google.common.base.Preconditions.checkNotNull;

import com.getbase.android.db.fluentsqlite.Query.QueryBuilder;
import com.getbase.android.db.provider.Utils;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Collections;
import java.util.List;

public class Insert implements InsertValuesBuilder {
  final String mTable;
  final ContentValues mValues;

  private Insert(String table, ContentValues values) {
    mTable = table;
    mValues = values;
  }

  public static InsertTableSelector insert() {
    return new InsertBuilder();
  }

  static class InsertBuilder implements InsertTableSelector, InsertFormSelector {
    String mTable;
    List<String> mQueryFormColumns = Lists.newArrayList();

    @Override
    public InsertFormSelector into(String table) {
      mTable = checkNotNull(table);
      return this;
    }

    @Override
    public DefaultValuesInsert defaultValues(String nullColumnHack) {
      return new DefaultValuesInsert(mTable, checkNotNull(nullColumnHack));
    }

    @Override
    public InsertSubqueryForm columns(String... columns) {
      Preconditions.checkArgument(columns != null, "Column list cannot be null");
      Collections.addAll(mQueryFormColumns, columns);

      return this;
    }

    @Override
    public InsertWithSelect resultOf(Query query) {
      checkNotNull(query);

      return new InsertWithSelect(mTable, query.toRawQuery(), mQueryFormColumns);
    }

    @Override
    public InsertWithSelect resultOf(QueryBuilder queryBuilder) {
      checkNotNull(queryBuilder);
      return resultOf(queryBuilder.build());
    }

    @Override
    public Insert values(ContentValues values) {
      return new Insert(mTable, new ContentValues(values));
    }

    @Override
    public Insert value(String column, Object value) {
      ContentValues values = new ContentValues();
      Utils.addToContentValues(column, value, values);

      return new Insert(mTable, values);
    }
  }

  public long perform(SQLiteDatabase db) {
    return db.insert(mTable, null, mValues);
  }

  public long performOrThrow(SQLiteDatabase db) {
    return db.insertOrThrow(mTable, null, mValues);
  }

  public static class InsertWithSelect {
    private final String mTable;
    private final RawQuery mQuery;
    private final List<String> mQueryFormColumns;

    InsertWithSelect(String table, RawQuery query, List<String> queryFormColumns) {
      mTable = table;
      mQuery = query;
      mQueryFormColumns = queryFormColumns;
    }

    public long perform(SQLiteDatabase db) {
      StringBuilder builder = new StringBuilder();
      builder.append("INSERT INTO ").append(mTable).append(" ");
      if (!mQueryFormColumns.isEmpty()) {
        builder
            .append("(")
            .append(Joiner.on(", ").join(mQueryFormColumns))
            .append(") ");
      }
      builder.append(mQuery.mRawQuery);

      SQLiteStatement statement = db.compileStatement(builder.toString());
      try {
        int argIndex = 1;
        for (String arg : mQuery.mRawQueryArgs) {
          Utils.bindContentValueArg(statement, argIndex++, arg);
        }

        return statement.executeInsert();
      } finally {
        statement.close();
      }
    }

    public long performOrThrow(SQLiteDatabase db) {
      long result = perform(db);
      if (result == -1) {
        throw new RuntimeException("Insert failed");
      }
      return result;
    }
  }

  public static class DefaultValuesInsert {
    final String mTable;
    final String mNullColumnHack;

    private DefaultValuesInsert(String table, String nullColumnHack) {
      mTable = table;
      mNullColumnHack = nullColumnHack;
    }

    public long perform(SQLiteDatabase db) {
      return db.insert(mTable, mNullColumnHack, null);
    }

    public long performOrThrow(SQLiteDatabase db) {
      return db.insertOrThrow(mTable, mNullColumnHack, null);
    }
  }

  @Override
  public Insert values(ContentValues values) {
    mValues.putAll(values);
    return this;
  }

  @Override
  public Insert value(String column, Object value) {
    Utils.addToContentValues(column, value, mValues);
    return this;
  }
}
