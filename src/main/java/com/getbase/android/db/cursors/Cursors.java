package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import android.database.Cursor;

import java.util.List;

public class Cursors {

  public static <T> FluentIterable<T> toFluentIterable(Cursor cursor, Function<Cursor, T> function) {
    List<T> transformed = Lists.newArrayList();
    for (int i = 0; cursor.moveToPosition(i); i++) {
      transformed.add(function.apply(cursor));
    }
    return FluentIterable.from(transformed);
  }

  public static void closeQuietly(Cursor cursor) {
    if (cursor != null) {
      cursor.close();
    }
  }
}
