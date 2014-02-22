package com.getbase.android.db.provider;

import com.google.common.collect.Multimap;

import android.content.ContentProviderOperation;

import java.util.ArrayList;
import java.util.List;

class BatcherWrapper extends Batcher {

  private final Batcher realBatcher;

  BatcherWrapper(Batcher realBatcher) {
    this.realBatcher = realBatcher;
  }

  @Override
  public Batcher append(Batcher Batcher) {
    return realBatcher.append(Batcher);
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
  public ArrayList<ContentProviderOperation> operations() {
    return realBatcher.operations();
  }

  @Override
  protected Multimap<ConvertibleToOperation, BackRef> getBackRefsMultimap() {
    return realBatcher.getBackRefsMultimap();
  }

  @Override
  protected List<ConvertibleToOperation> getConvertibles() {
    return realBatcher.getConvertibles();
  }
}
