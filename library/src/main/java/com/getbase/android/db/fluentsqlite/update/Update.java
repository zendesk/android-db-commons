package com.getbase.android.db.fluentsqlite.update;

import static com.google.common.base.Preconditions.checkNotNull;

import com.getbase.android.db.fluentsqlite.Expressions.Expression;
import com.getbase.android.db.provider.Utils;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class Update implements UpdateTableSelector {
  private String mTable;
  private List<String> mSelections = Lists.newArrayList();
  private List<Object> mSelectionArgs = Lists.newArrayList();
  private ContentValues mValues = new ContentValues();
  private LinkedListMultimap<String, Object> mCustomExpressionsArgs = LinkedListMultimap.create();
  private LinkedHashMap<String, String> mCustomExpressions = Maps.newLinkedHashMap();

  private Update() {
  }

  public static UpdateTableSelector update() {
    return new Update();
  }

  public int perform(SQLiteDatabase db) {
    String mSelection = Joiner.on(" AND ").join(mSelections);
    if (mCustomExpressions.isEmpty()) {
      return db.update(mTable, mValues, mSelection, FluentIterable.from(mSelectionArgs).transform(Functions.toStringFunction()).toArray(String.class));
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
      args.addAll(mSelectionArgs);

      if (!Strings.isNullOrEmpty(mSelection)) {
        builder
            .append(" WHERE ")
            .append(mSelection);
      }

      SQLiteStatement statement = db.compileStatement(builder.toString());
      try {
        int argIndex = 1;
        for (String customColumn : mCustomExpressions.keySet()) {
          for (java.lang.Object arg : mCustomExpressionsArgs.get(customColumn)) {
            Utils.bindContentValueArg(statement, argIndex++, arg);
          }
        }
        for (Object arg : args) {
          Utils.bindContentValueArg(statement, argIndex++, arg);
        }

        return statement.executeUpdateDelete();
      } finally {
        statement.close();
      }
    }
  }

  @Override
  public Update table(String table) {
    mTable = checkNotNull(table);
    return this;
  }

  public Update values(ContentValues values) {
    for (Entry<String, Object> value : values.valueSet()) {
      mCustomExpressions.remove(value.getKey());
    }
    mValues.putAll(values);
    return this;
  }

  public Update value(String column, Object value) {
    mCustomExpressions.remove(column);
    mCustomExpressionsArgs.removeAll(column);
    Utils.addToContentValues(column, value, mValues);
    return this;
  }

  public Update setColumn(String column, String expression) {
    mValues.remove(column);
    mCustomExpressionsArgs.removeAll(column);
    mCustomExpressions.put(column, "(" + expression + ")");
    return this;
  }

  public Update setColumn(String column, Expression expression) {
    setColumn(column, expression.toRawSql());

    mCustomExpressionsArgs.putAll(column, Arrays.asList(expression.getMergedArgs()));

    return this;
  }

  public Update where(String selection, Object... selectionArgs) {
    mSelections.add("(" + selection + ")");
    Collections.addAll(mSelectionArgs, selectionArgs);

    return this;
  }

  public Update where(Expression expression, Object... selectionArgs) {
    return where(expression.toRawSql(), expression.getMergedArgs(selectionArgs));
  }
}
