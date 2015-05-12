package com.getbase.android.db.loaders;

import com.getbase.android.db.cursors.Cursors;
import com.google.common.base.Function;

import android.database.Cursor;
import android.support.v4.util.LruCache;

import java.io.Closeable;
import java.util.AbstractList;
import java.util.RandomAccess;

public class LazyCursorList<T> extends AbstractList<T> implements RandomAccess, Closeable {

  private final Cursor cursor;
  private final LruCache<Integer, T> cache;

  public LazyCursorList(final Cursor cursor, final Function<? super Cursor, T> function) {
    this.cursor = Cursors.returnSameOrEmptyIfNull(cursor);

    cache = new LruCache<Integer, T>(256) {
      @Override
      protected T create(Integer key) {
        cursor.moveToPosition(key);
        return function.apply(cursor);
      }
    };
  }

  @Override
  public T get(int i) {
    return cache.get(i);
  }

  @Override
  public int size() {
    return cursor.getCount();
  }

  @Override
  public void close() {
    cursor.close();
  }
}
