package com.getbase.android.db.query;

import static com.google.common.base.Preconditions.checkNotNull;

import com.getbase.android.db.query.Expressions.Expression;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.List;

public class Delete {
  final String mTable;
  final String mSelection;
  final String[] mSelectionArgs;

  private Delete(String table, String selection, String[] selectionArgs) {
    mTable = table;
    mSelection = selection;
    mSelectionArgs = selectionArgs;
  }

  public static TableSelector delete() {
    return new DeleteBuilder();
  }

  public int perform(SQLiteDatabase db) {
    return db.delete(mTable, mSelection, mSelectionArgs);
  }

  public static class DeleteBuilder implements TableSelector, SelectionBuilder {
    private String mTable;
    private List<String> mSelections = Lists.newArrayList();
    private List<String> mSelectionArgs = Lists.newArrayList();

    private DeleteBuilder() {
    }

    @Override
    public SelectionBuilder from(String table) {
      mTable = checkNotNull(table);
      return this;
    }

    @Override
    public SelectionBuilder where(String selection, Object... selectionArgs) {
      mSelections.add("(" + selection + ")");
      mSelectionArgs.addAll(Collections2.transform(Arrays.asList(selectionArgs), Functions.toStringFunction()));

      return this;
    }

    @Override
    public SelectionBuilder where(Expression expression, Object... selectionArgs) {
      return where(expression.toRawSql(), selectionArgs);
    }

    @Override
    public Delete build() {
      return new Delete(
          mTable,
          Joiner.on(" AND ").join(mSelections),
          mSelectionArgs.toArray(new String[mSelectionArgs.size()])
      );
    }
  }

  public interface TableSelector {
    SelectionBuilder from(String table);
  }

  public interface SelectionBuilder {
    SelectionBuilder where(String selection, Object... selectionArgs);
    SelectionBuilder where(Expression selection, Object... selectionArgs);
    Delete build();
  }
}
