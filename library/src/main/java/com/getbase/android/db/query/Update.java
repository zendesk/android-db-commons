package com.getbase.android.db.query;

import static com.google.common.base.Preconditions.checkNotNull;

import com.getbase.android.db.provider.Utils;
import com.getbase.android.db.query.Expressions.Expression;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Update {
  final String mTable;
  final String mSelection;
  final String[] mSelectionArgs;
  final ContentValues mValues;
  final Map<String, String> mCustomExpressions;

  private Update(String table, String selection, String[] selectionArgs, ContentValues values, Map<String, String> customExpressions) {
    mTable = table;
    mSelection = selection;
    mSelectionArgs = selectionArgs;
    mValues = values;
    mCustomExpressions = customExpressions;
  }

  public static TableSelector update() {
    return new UpdateBuilderImpl();
  }

  public int perform(SQLiteDatabase db) {
    if (mCustomExpressions.isEmpty()) {
      return db.update(mTable, mValues, mSelection, mSelectionArgs);
    } else {
      List<Object> args = Lists.newArrayList();

      StringBuilder builder = new StringBuilder();
      builder
          .append("UPDATE ")
          .append(mTable)
          .append(" SET ")
          .append(Joiner.on(", ").join(Collections2.transform(mCustomExpressions.entrySet(), new Function<Entry<String, String>, String>() {
            @Override
            public String apply(Entry<String, String> entry) {
              return entry.getKey() + "=" + entry.getValue();
            }
          })));

      if (mValues.size() != 0) {
        builder.append(", ");
      }

      Set<Entry<String, Object>> values = mValues.valueSet();
      builder.append(Joiner.on(", ").join(Collections2.transform(values, new Function<Entry<String, Object>, Object>() {
        @Override
        public Object apply(Entry<String, Object> value) {
          return value.getKey() + "=?";
        }
      })));

      args.addAll(Collections2.transform(values, new Function<Entry<String, Object>, Object>() {
        @Override
        public Object apply(Entry<String, Object> value) {
          return value.getValue();
        }
      }));
      Collections.addAll(args, mSelectionArgs);

      if (!Strings.isNullOrEmpty(mSelection)) {
        builder
            .append(" WHERE ")
            .append(mSelection);
      }

      SQLiteStatement statement = db.compileStatement(builder.toString());
      try {
        for (int i = 0; i < args.size(); i++) {
          Utils.bindContentValueArg(statement, i + 1, args.get(i));
        }

        return statement.executeUpdateDelete();
      } finally {
        statement.close();
      }
    }
  }

  public static class UpdateBuilderImpl implements TableSelector, UpdateBuilder {
    private String mTable;
    private List<String> mSelections = Lists.newArrayList();
    private List<String> mSelectionArgs = Lists.newArrayList();
    private ContentValues mContentValues = new ContentValues();
    private Map<String, String> mCustomExpressions = Maps.newHashMap();

    private UpdateBuilderImpl() {
    }

    @Override
    public UpdateBuilder table(String table) {
      mTable = checkNotNull(table);
      return this;
    }

    @Override
    public UpdateBuilder where(String selection, Object... selectionArgs) {
      mSelections.add("(" + selection + ")");
      mSelectionArgs.addAll(Collections2.transform(Arrays.asList(selectionArgs), Functions.toStringFunction()));

      return this;
    }

    @Override
    public UpdateBuilder where(Expression expression, Object... selectionArgs) {
      return where(expression.toRawSql(), selectionArgs);
    }

    @Override
    public Update build() {
      return new Update(
          mTable,
          Joiner.on(" AND ").join(mSelections),
          mSelectionArgs.toArray(new String[mSelectionArgs.size()]),
          mContentValues,
          mCustomExpressions);
    }

    @Override
    public UpdateBuilder values(ContentValues values) {
      for (Entry<String, Object> value : values.valueSet()) {
        mCustomExpressions.remove(value.getKey());
      }
      mContentValues.putAll(values);
      return this;
    }

    @Override
    public UpdateBuilder value(String column, Object value) {
      mCustomExpressions.remove(column);
      Utils.addToContentValues(column, value, mContentValues);
      return this;
    }

    @Override
    public UpdateBuilder setColumn(String column, String expression) {
      mContentValues.remove(column);
      mCustomExpressions.put(column, "(" + expression + ")");
      return this;
    }

    @Override
    public UpdateBuilder setColumn(String column, Expression expression) {
      return setColumn(column, expression.toRawSql());
    }
  }

  public interface TableSelector {
    UpdateBuilder table(String table);
  }

  public interface UpdateBuilder {
    UpdateBuilder values(ContentValues values);
    UpdateBuilder value(String column, Object value);
    UpdateBuilder setColumn(String column, String expression);
    UpdateBuilder setColumn(String column, Expression expression);
    UpdateBuilder where(String selection, Object... selectionArgs);
    UpdateBuilder where(Expression expression, Object... selectionArgs);
    Update build();
  }
}
