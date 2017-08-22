package com.getbase.android.db.loaders.functions;


import android.support.annotation.Nullable;

import com.google.common.base.Function;

public class SingleFunctionWithCompletionListener<A, B> implements FunctionWithCompletionListener<A, B> {

  public static <A, B> FunctionWithCompletionListener<A, B> wrapIfNeeded(Function<A, B> function) {
    return (function instanceof SingleFunctionWithCompletionListener) ? (SingleFunctionWithCompletionListener<A, B>) function : new SingleFunctionWithCompletionListener<>(function);
  }

  @Nullable
  private CompletionListener mCompletionListener;
  private Function<A, B> mWrappedFunction;

  private SingleFunctionWithCompletionListener(Function<A, B> function) {
    mWrappedFunction = function;
  }

  @Override
  public B apply(A a) {
    B result = mWrappedFunction.apply(a);
    if (mCompletionListener != null) {
      mCompletionListener.onFunctionComplete();
    }
    return result;
  }

  @Override
  public void setCompletionListener(@Nullable CompletionListener listener) {
    mCompletionListener = listener;
  }
}
