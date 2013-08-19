package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import android.database.Cursor;
import android.database.CursorWrapper;

public class FluentCursor extends CursorWrapper {

  private static Cursor returnSameOrEmptyIfNull(Cursor cursor) {
    if (cursor == null) {
      return new EmptyCursor();
    }
    return cursor;
  }

  public FluentCursor(Cursor cursor) {
    super(returnSameOrEmptyIfNull(cursor));
  }

  /**
   * Transforms Cursor to FluentIterable of T applying given function
   * WARNING: This method closes cursor. Do not use this from onLoadFinished()
   *
   * @param singleRowTransform Function to apply on every single row of this cursor
   * @param <T> Type of Iterable's single element
   * @return Transformed iterable
   */
  public <T> FluentIterable<T> transform(Function<Cursor, T> singleRowTransform) {
    final FluentIterable<T> fi = Cursors.toFluentIterable(getWrappedCursor(), singleRowTransform);
    close();
    return fi;
  }
}
