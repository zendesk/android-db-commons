package com.getbase.android.db.loaders.functions;

public class FunctionWithCompletionListenerComposition<A, B, C> implements FunctionWithCompletionListener<A, C> {

  private FunctionWithCompletionListener<B, C> mG;
  private FunctionWithCompletionListener<A, ? extends B> mF;

  public FunctionWithCompletionListenerComposition(FunctionWithCompletionListener<B, C> g, FunctionWithCompletionListener<A, ? extends B> f) {
    mG = g;
    mF = f;
  }

  @Override
  public C apply(A a) {
    return mG.apply(mF.apply(a));
  }

  @Override
  public void setCompletionListener(FunctionWithCompletionListener.CompletionListener listener) {
    mG.setCompletionListener(listener);
    mF.setCompletionListener(listener);
  }
}
