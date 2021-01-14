package com.getbase.android.db.loaders;

import com.google.common.base.Function;

import androidx.core.os.CancellationSignal;

class SimpleCancellableFunction<TIn, TOut> implements CancellableFunction<TIn, TOut> {
  final Function<TIn, TOut> transform;

  SimpleCancellableFunction(Function<TIn, TOut> transform) {
    this.transform = transform;
  }

  @Override
  public final TOut apply(TIn input, CancellationSignal signal) {
    TOut result = transform.apply(input);
    signal.throwIfCanceled();
    return result;
  }
}
