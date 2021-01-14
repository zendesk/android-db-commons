package com.getbase.android.db.loaders;

import androidx.core.os.CancellationSignal;

class ComposedCancellableFunction<TIn, T, TOut> implements CancellableFunction<TIn, TOut> {
  private final CancellableFunction<TIn, T> mFirst;
  private final CancellableFunction<T, TOut> mThen;

  ComposedCancellableFunction(CancellableFunction<TIn, T> first, CancellableFunction<T, TOut> then) {
    mFirst = first;
    mThen = then;
  }

  @Override
  public final TOut apply(TIn input, CancellationSignal signal) {
    return mThen.apply(mFirst.apply(input, signal), signal);
  }
}
