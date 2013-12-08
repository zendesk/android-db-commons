package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.NoSuchElementException;

public class FluentCursor extends CursorWrapper {

  public FluentCursor(Cursor cursor) {
    super(Cursors.returnSameOrEmptyIfNull(cursor));
  }

  /**
   * Transforms Cursor to FluentIterable of T applying given function
   * WARNING: This method closes cursor. Do not use this from onLoadFinished()
   *
   * @param singleRowTransform Function to apply on every single row of this cursor
   * @param <T> Type of Iterable's single element
   * @return Transformed iterable
   */
  public <T> FluentIterable<T> toFluentIterable(Function<? super Cursor, T> singleRowTransform) {
    try {
      return Cursors.toFluentIterable(this, singleRowTransform);
    } finally {
      close();
    }
  }

  /**
   * Returns the only row of this cursor transformed using the given function.
   * WARNING: This method closes cursor. Do not use this from onLoadFinished()
   *
   * @param singleRowTransform Function to apply on the only row of this cursor
   * @param <T> Type of returned element
   * @return Transformed first row of the cursor. If the cursor is empty,
   * NoSuchElementException is thrown. If the cursor contains more than one
   * row, IllegalArgumentException is thrown.
   */
  public <T> T toOnlyElement(Function<? super Cursor, T> singleRowTransform) {
    try {
      switch (getCount()) {
      case 0:
        throw new NoSuchElementException();
      case 1:
        moveToFirst();
        return singleRowTransform.apply(this);
      default:
        throw new IllegalArgumentException("expected one element but was: " + getCount());
      }
    } finally {
      close();
    }
  }

  /**
   * Returns the only row of this cursor transformed using the given function,
   * or the supplied default value if cursor is empty.
   * WARNING: This method closes cursor. Do not use this from onLoadFinished()
   *
   * @param singleRowTransform Function to apply on the only row of this cursor
   * @param <T> Type of returned element
   * @return Transformed first row of the cursor or the supplied default
   * value if the cursor is empty. If the cursor contains more than one
   * row, IllegalArgumentException is thrown.
   */
  public <T> T toOnlyElement(Function<? super Cursor, T> singleRowTransform, T defaultValue) {
    if (moveToFirst()) {
      return toOnlyElement(singleRowTransform);
    } else {
      close();
      return defaultValue;
    }
  }

  /**
   * Returns number of rows in this cursor and closes it.
   * WARNING: This method closes cursor. Do not use this from onLoadFinished()
   *
   * @return Row count from this cursor
   */
  public int toRowCount() {
    try {
      return getCount();
    } finally {
      close();
    }
  }
}
