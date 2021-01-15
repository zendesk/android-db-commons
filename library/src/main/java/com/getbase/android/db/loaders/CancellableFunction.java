package com.getbase.android.db.loaders;

import androidx.core.os.CancellationSignal;

public interface CancellableFunction<TIn, TOut> {
  TOut apply(TIn input, CancellationSignal signal);
}
