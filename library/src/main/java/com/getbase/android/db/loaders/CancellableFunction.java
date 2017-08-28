package com.getbase.android.db.loaders;

import android.support.v4.os.CancellationSignal;

public interface CancellableFunction<TIn, TOut> {
  TOut apply(TIn input, CancellationSignal signal);
}
