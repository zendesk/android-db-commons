package com.getbase.android.db.provider;

import android.content.ContentProviderOperation;

import java.util.ArrayList;

class BatcherWrapper extends Batcher {
  private final Batcher realBatcher;

  BatcherWrapper(Batcher realBatcher) {
    this.realBatcher = realBatcher;
  }

  @Override
  public BackRefBuilder append(ConvertibleToOperation... convertibles) {
    return realBatcher.append(convertibles);
  }

  @Override
  public BackRefBuilder append(Iterable<ConvertibleToOperation> convertibles) {
    return realBatcher.append(convertibles);
  }

  @Override
  public Batcher decorateUrisWith(UriDecorator uriDecorator) {
    return realBatcher.decorateUrisWith(uriDecorator);
  }

  @Override
  public ArrayList<ContentProviderOperation> operations() {
    return realBatcher.operations();
  }
}
