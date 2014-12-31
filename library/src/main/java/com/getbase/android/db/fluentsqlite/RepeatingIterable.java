package com.getbase.android.db.fluentsqlite;

import java.util.Iterator;

class RepeatingIterable<T> implements Iterable<T> {

  private final int mCount;
  private final T mElement;

  public RepeatingIterable(T element, int count) {
    mCount = count;
    mElement = element;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      int mIndex = -1;

      @Override
      public boolean hasNext() {
        return mIndex != mCount - 1;
      }

      @Override
      public T next() {
        mIndex++;
        return mElement;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Unsupported");
      }
    };
  }
}
