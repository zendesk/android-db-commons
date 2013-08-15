package com.getbase.android.cursors;

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
      if (i != cursor.getPosition()) {
        throw new IllegalArgumentException("Moving cursor inside function is forbidden");
      }
    }
    return FluentIterable.from(transformed);
  }

  public static <T> FluentIterable<T> toLazyFluentIterable(Cursor cursor, Function<Cursor, T> function) {
    return null;
  }

  public static void closeQuietly(Cursor cursor) {
    if (cursor != null) {
      cursor.close();
    }
  }
}
