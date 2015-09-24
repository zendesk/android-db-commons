package com.getbase.android.db.provider;

import java.util.Arrays;

public class BackRefBuilder extends BatcherWrapper {

  private BatcherImpl batcher;
  private final Iterable<ConvertibleToOperation> convertibles;

  public BackRefBuilder(BatcherImpl batcher, Iterable<ConvertibleToOperation> convertibles) {
    super(batcher);
    this.batcher = batcher;
    this.convertibles = convertibles;
  }

  public BackRefBuilder(BatcherImpl batcher, ConvertibleToOperation... convertible) {
    this(batcher, Arrays.asList(convertible));
  }

  public BackRefBuilder withValueBackReference(Insert previousInsert, String columnName) {
    for (ConvertibleToOperation convertible : convertibles) {
      batcher.putValueBackRef(convertible, new ValueBackRef(previousInsert, columnName));
    }
    return this;
  }
}
