package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.LinkedHashMultimap;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

/**
 * Wrapper for Android {@link android.database.Cursor} providing methods
 * for transforming Cursor into other Java types. It also wraps null Cursors
 * into valid Cursor instance with no data and no columns.
 */
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
   * Transforms Cursor to LinkedHashMultimap<TKey, TValue> by applying given
   * functions. The iteration order for the returned map is the same as
   * the iteration order over rows of Cursor.
   * WARNING: This method closes cursor. Do not use this from onLoadFinished()
   *
   * @param keyTransform Function to apply on every single row of this cursor
   * to get the key of the entry representing this row.
   * @param valueTransform Function to apply on every single row of this cursor
   * to get the value of the entry representing this row.
   * @param <TKey> Type of keys in the returned multimap
   * @param <TValue> Type of values in the returned multimap
   * @return Transformed map
   */
  public <TKey, TValue> LinkedHashMultimap<TKey, TValue> toMultimap(Function<? super Cursor, TKey> keyTransform, Function<? super Cursor, TValue> valueTransform) {
    try {
      LinkedHashMultimap<TKey, TValue> result = LinkedHashMultimap.create(getCount(), 1);

      for (moveToFirst(); !isAfterLast(); moveToNext()) {
        result.put(keyTransform.apply(this), valueTransform.apply(this));
      }

      return result;
    } finally {
      close();
    }
  }

  /**
   * Transforms Cursor to LinkedHashMap<TKey, TValue> by applying given
   * functions. The iteration order for the returned map is the same as
   * the iteration order over rows of Cursor.
   * WARNING: This method closes cursor. Do not use this from onLoadFinished()
   *
   * @param keyTransform Function to apply on every single row of this cursor
   * to get the key of the entry representing this row.
   * @param valueTransform Function to apply on every single row of this cursor
   * to get the value of the entry representing this row.
   * @param <TKey> Type of keys in the returned map
   * @param <TValue> Type of values in the returned map
   * @return Transformed map
   * @throws IllegalArgumentException if Cursor contains duplicate keys
   */
  public <TKey, TValue> LinkedHashMap<TKey, TValue> toMap(Function<? super Cursor, TKey> keyTransform, Function<? super Cursor, TValue> valueTransform) {
    try {
      LinkedHashMap<TKey, TValue> result = new LinkedHashMap<TKey, TValue>(getCount(), 1);

      for (moveToFirst(); !isAfterLast(); moveToNext()) {
        final TKey key = keyTransform.apply(this);
        final TValue value = valueTransform.apply(this);

        final TValue previousValue = result.put(key, value);

        Preconditions.checkArgument(previousValue == null, "Duplicate key %s found on position %s", key, getPosition());
      }

      return result;
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

  /**
   * Sets the notification {@code Uri} on wrapped {@code Cursor}.
   *
   * @return this {@code FluentCursor}
   */
  public FluentCursor withNotificationUri(ContentResolver resolver, Uri uri) {
    setNotificationUri(resolver, uri);
    return this;
  }
}
