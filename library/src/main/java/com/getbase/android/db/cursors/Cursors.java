package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import android.database.Cursor;

import java.util.List;

/**
 * Utility methods for Android {@link android.database.Cursor}.
 */
public final class Cursors {
  private Cursors() {
  }

  /**
   * Transforms {@code cursor} to {@link com.google.common.collect.FluentIterable}
   * of type {@code T} by applying the {@code singleRowTransform} to every row.
   */
  public static <T> FluentIterable<T> toFluentIterable(Cursor cursor, Function<? super Cursor, T> singleRowTransform) {
    List<T> transformed = Lists.newArrayList();
    if (cursor != null) {
      for (int i = 0; cursor.moveToPosition(i); i++) {
        transformed.add(singleRowTransform.apply(cursor));
      }
    }
    return FluentIterable.from(transformed);
  }

  /**
   * Closes non-null and opened {@code cursor} or does nothing in case {@code cursor}
   * is null or it's already closed.
   */
  public static void closeQuietly(Cursor cursor) {
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
  }

  /**
   * Returns {@code cursor} if it's not null, otherwise returns new instance
   * of {@link android.database.Cursor} with no data and no columns.
   */
  public static Cursor returnSameOrEmptyIfNull(Cursor cursor) {
    if (cursor == null) {
      return new EmptyCursor();
    }
    return cursor;
  }
}
