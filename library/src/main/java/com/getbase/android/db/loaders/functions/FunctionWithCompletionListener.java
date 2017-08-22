package com.getbase.android.db.loaders.functions;


import com.google.common.base.Function;

public interface FunctionWithCompletionListener<A, B> extends Function<A, B> {

  interface CompletionListener {
    void onFunctionComplete();
  }

  void setCompletionListener(CompletionListener listener);
}
