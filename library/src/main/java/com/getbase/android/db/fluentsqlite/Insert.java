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
import java.util.Set;

public class Insert implements InsertValuesBuilder {

  enum ConflictAlgorithm {
    IGNORE("OR IGNORE"), REPLACE("OR REPLACE");

    private final String mSuffix;

    ConflictAlgorithm(String suffix) {
      mSuffix = suffix;
    }

    String suffix() {
      return mSuffix;
    }
  }

  final String mTable;
  final ContentValues mValues;

  private Insert(String table, ContentValues values) {
    mTable = table;
    mValues = values;
  }

  public static InsertTableWithAlgorithmSelector insert() {
    return new InsertBuilder();
  }

  static class InsertBuilder implements InsertTableWithAlgorithmSelector, InsertFormSelector {

    ConflictAlgorithm mConflictAlgorithm;
    String mTable;
    List<String> mQueryFormColumns = Lists.newArrayList();

    @Override
    public InsertFormSelector into(String table) {
      mTable = checkNotNull(table);
      return this;
    }

    @Override
    public InsertTableSelector orIgnore() {
      return withOnConflictAlgorithm(ConflictAlgorithm.IGNORE);
    }

    @Override
    public InsertTableSelector orReplace() {
      return withOnConflictAlgorithm(ConflictAlgorithm.REPLACE);
    }

    private InsertTableSelector withOnConflictAlgorithm(ConflictAlgorithm conflictAlgorithm) {
      mConflictAlgorithm = conflictAlgorithm;
      return this;
    }

    @Override
    public DefaultValuesInsert defaultValues(String nullColumnHack) {
      if (mConflictAlgorithm != null) {
        return new DefaultValuesInsertWithOnConflict(mTable, nullColumnHack, mConflictAlgorithm);
      } else {
        return new DefaultValuesInsert(mTable, checkNotNull(nullColumnHack));
      }
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

      return new InsertWithSelect(mTable, mConflictAlgorithm, query.toRawQuery(), mQueryFormColumns);
    }

    @Override
    public InsertWithSelect resultOf(QueryBuilder queryBuilder) {
      checkNotNull(queryBuilder);
      return resultOf(queryBuilder.build());
    }

    @Override
    public Insert values(ContentValues values) {
      return valuesImpl(new ContentValues(values));
    }

    @Override
    public Insert value(String column, Object value) {
      ContentValues values = new ContentValues();
      Utils.addToContentValues(column, value, values);
      return valuesImpl(values);
    }

    private Insert valuesImpl(ContentValues values) {
      if (mConflictAlgorithm != null) {
        return new InsertWithOnConflict(mTable, mConflictAlgorithm, values);
      } else {
        return new Insert(mTable, values);
      }
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
    private final ConflictAlgorithm mConflictAlgorithm;

    InsertWithSelect(String table, ConflictAlgorithm conflictAlgorithm, RawQuery query, List<String> queryFormColumns) {
      mTable = table;
      mQuery = query;
      mConflictAlgorithm = conflictAlgorithm;
      mQueryFormColumns = queryFormColumns;
    }

    public long perform(SQLiteDatabase db) {
      StringBuilder builder = new StringBuilder();
      builder.append("INSERT");
      if (mConflictAlgorithm != null) {
        builder
            .append(" ")
            .append(mConflictAlgorithm.suffix());
      }
      builder.append(" INTO ");
      builder.append(mTable).append(" ");
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
      return performOrThrowFromResult(perform(db));
    }
  }

  public static class InsertWithOnConflict extends Insert {

    private final ConflictAlgorithm mConflictAlgorithm;

    public InsertWithOnConflict(String table, ConflictAlgorithm conflictAlgorithm, ContentValues contentValues) {
      super(table, contentValues);
      mConflictAlgorithm = conflictAlgorithm;
    }

    public long perform(SQLiteDatabase db) {
      StringBuilder builder = new StringBuilder();
      Set<String> keys = mValues.keySet();
      builder.append("INSERT ")
          .append(mConflictAlgorithm.suffix())
          .append(" INTO ")
          .append(mTable)
          .append(" (")
          .append(Joiner.on(",").join(keys))
          .append(") VALUES (")
          .append(Joiner.on(",").join(new RepeatingIterable<String>("?", keys.size())))
          .append(")");

      SQLiteStatement statement = db.compileStatement(builder.toString());
      try {
        int index = 0;
        for (String key : keys) {
          Utils.bindContentValueArg(statement, index++, mValues.get(key));
        }
        return statement.executeInsert();
      } finally {
        statement.close();
      }
    }

    public long performOrThrow(SQLiteDatabase db) {
      return performOrThrowFromResult(perform(db));
    }
  }

  public static class DefaultValuesInsertWithOnConflict extends DefaultValuesInsert {

    private final ConflictAlgorithm mConflictAlgorithm;

    private DefaultValuesInsertWithOnConflict(String table, String nullColumnHack, ConflictAlgorithm conflictAlgorithm) {
      super(table, nullColumnHack);
      mConflictAlgorithm = conflictAlgorithm;
    }

    @Override
    public long perform(SQLiteDatabase db) {
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT ")
          .append(mConflictAlgorithm.suffix())
          .append(" INTO ")
          .append(mTable)
          .append(" (").append(mNullColumnHack).append(")")
          .append(" VALUES (NULL)");
      SQLiteStatement statement = db.compileStatement(sb.toString());
      try {
        return statement.executeInsert();
      } finally {
        statement.close();
      }
    }

    @Override
    public long performOrThrow(SQLiteDatabase db) {
      return performOrThrowFromResult(perform(db));
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

  private static long performOrThrowFromResult(long result) {
    if (result == -1) {
      throw new RuntimeException("Insert failed");
    }
    return result;
  }
}
