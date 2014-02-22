package com.getbase.android.db.provider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class BatcherImpl extends Batcher {

  private final List<ConvertibleToOperation> operations = Lists.newArrayList();
  private final Multimap<ConvertibleToOperation, BackRef> backRefs = HashMultimap.create();

  BatcherImpl() {
  }

  @Override
  public Batcher append(Batcher batcher) {
    backRefs.putAll(batcher.getBackRefsMultimap());
    return append(batcher.getConvertibles());
  }

  @Override
  public Batcher append(ConvertibleToOperation... convertibles) {
    operations.addAll(Lists.newArrayList(convertibles));
    return this;
  }

  @Override
  public Batcher append(Iterable<ConvertibleToOperation> convertibles) {
    operations.addAll(Lists.newArrayList(convertibles));
    return this;
  }

  @Override
  public Batcher append(ConvertibleToOperation convertible) {
    operations.add(convertible);
    return this;
  }

  @Override
  public BackRefBuilder appendWithBackRef(ConvertibleToOperation convertibleToOperation) {
    operations.add(convertibleToOperation);
    return new BackRefBuilder(convertibleToOperation);
  }

  @Override
  public ArrayList<ContentProviderOperation> operations() {
    final Map<ConvertibleToOperation, Integer> parentsPositions = Maps.newHashMap();
    ArrayList<ContentProviderOperation> providerOperations = Lists.newArrayListWithCapacity(operations.size());
    for (ConvertibleToOperation convertible : operations) {
      final Builder builder = convertible.toContentProviderOperationBuilder();
      final Collection<BackRef> backRefs = this.backRefs.get(convertible);
      if (!backRefs.isEmpty()) {
        ContentValues values = new ContentValues();
        for (BackRef backRef : backRefs) {
          values.put(backRef.column, parentsPositions.get(backRef.parent));
        }
        builder.withValueBackReferences(values);
      }
      parentsPositions.put(convertible, providerOperations.size());
      providerOperations.add(builder.build());
    }
    return providerOperations;
  }

  @Override
  protected Multimap<ConvertibleToOperation, BackRef> getBackRefsMultimap() {
    return backRefs;
  }

  @Override
  protected List<ConvertibleToOperation> getConvertibles() {
    return operations;
  }

  public class BackRefBuilder extends BatcherWrapper {

    private final ConvertibleToOperation convertible;

    public BackRefBuilder(ConvertibleToOperation convertible) {
      super(BatcherImpl.this);
      this.convertible = convertible;
    }

    public BackRefBuilder forPrevious(ConvertibleToOperation operation, String columnName) {
      backRefs.put(convertible, new BackRef(operation, columnName));
      return this;
    }
  }
}
