package com.getbase.android.db.provider;

import com.google.common.collect.Lists;

public class BackRefBuilder extends BatcherWrapper {

  private BatcherImpl batcher;
  private final Iterable<ConvertibleToOperation> convertibles;

  public BackRefBuilder(BatcherImpl batcher, Iterable<ConvertibleToOperation> convertibles) {
    super(batcher);
    this.batcher = batcher;
    this.convertibles = convertibles;
  }

  public BackRefBuilder(BatcherImpl batcher, ConvertibleToOperation... convertible) {
    this(batcher, Lists.newArrayList(convertible));
  }

  public BackRefBuilder withValueBackReference(Insert previousInsert, String columnName) {
    for (ConvertibleToOperation convertible : convertibles) {
      batcher.putBackRef(convertible, new BackRef(previousInsert, columnName));
    }
    return this;
  }
}
