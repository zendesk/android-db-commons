package com.getbase.android.db.fluentsqlite.delete;

import static com.google.common.base.Preconditions.checkNotNull;

import com.getbase.android.db.fluentsqlite.Expressions.Expression;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.List;

public class Delete implements DeleteTableSelector {
  private String mTable;
  private List<String> mSelections = Lists.newArrayList();
  private List<Object> mSelectionArgs = Lists.newArrayList();

  private Delete() {
  }

  public static DeleteTableSelector delete() {
    return new Delete();
  }

  public int perform(SQLiteDatabase db) {
    return db.delete(
        mTable,
        Joiner.on(" AND ").join(mSelections),
        FluentIterable.from(mSelectionArgs).transform(Functions.toStringFunction()).toArray(String.class)
    );
  }

  @Override
  public Delete from(String table) {
    mTable = checkNotNull(table);
    return this;
  }

  public Delete where(String selection, Object... selectionArgs) {
    mSelections.add("(" + selection + ")");
    mSelectionArgs.addAll(Arrays.asList(selectionArgs));

    return this;
  }

  public Delete where(Expression expression, Object... selectionArgs) {
    mSelections.add("(" + expression.toRawSql() + ")");
    addExpressionArgs(mSelectionArgs, expression, selectionArgs);
    return this;
  }

  private void addExpressionArgs(List<Object> args, Expression expression, Object... boundArgs) {
    Preconditions.checkArgument(
        expression.getArgsCount() == boundArgs.length + expression.getBoundArgs().size(),
        "Invalid number of arguments: expression has %s arg placeholders and %s bound args, so I need %s additional args specified, but there was %s args",
        expression.getArgsCount(),
        expression.getBoundArgs().size(),
        (expression.getArgsCount() - expression.getBoundArgs().size()),
        boundArgs.length
    );

    int boundArgsIndex = 0;
    for (int i = 0; i < expression.getArgsCount(); i++) {
      final Object arg;

      if (expression.getBoundArgs().containsKey(i)) {
        arg = expression.getBoundArgs().get(i);
      } else {
        arg = boundArgs[boundArgsIndex++];
      }

      args.add(arg);
    }
  }
}
