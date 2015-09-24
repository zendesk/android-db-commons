package com.getbase.android.db.provider;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class BatcherImpl extends Batcher {
  private final List<ConvertibleToOperation> operations = Lists.newArrayList();
  private final Multimap<ConvertibleToOperation, ValueBackRef> valueBackRefs = HashMultimap.create();
  private UriDecorator mUriDecorator = Utils.DUMMY_URI_DECORATOR;

  @Override
  public BackRefBuilder append(ConvertibleToOperation... convertibles) {
    Collections.addAll(operations, convertibles);
    return new BackRefBuilder(this, convertibles);
  }

  @Override
  public BackRefBuilder append(Iterable<ConvertibleToOperation> convertibles) {
    Iterables.addAll(operations, convertibles);
    return new BackRefBuilder(this, convertibles);
  }

  @Override
  public Batcher decorateUrisWith(UriDecorator uriDecorator) {
    mUriDecorator = MoreObjects.firstNonNull(uriDecorator, Utils.DUMMY_URI_DECORATOR);
    return this;
  }

  private int assertThatThereIsOnlyOneParentPosition(Collection<Integer> integers) {
    if (integers.isEmpty()) {
      throw new IllegalStateException("Could not find proper Insert for back references.");
    } else if (integers.size() > 1) {
      throw new IllegalStateException("Referenced for Insert in back references that is present twice.");
    }
    return Iterables.getOnlyElement(integers);
  }

  @Override
  public ArrayList<ContentProviderOperation> operations() {
    final Multimap<ConvertibleToOperation, Integer> parentsPositions = HashMultimap.create();
    ArrayList<ContentProviderOperation> providerOperations = Lists.newArrayListWithCapacity(operations.size());
    for (ConvertibleToOperation convertible : operations) {
      final Builder builder = convertible.toContentProviderOperationBuilder(mUriDecorator);
      final Collection<ValueBackRef> valueBackRefs = this.valueBackRefs.get(convertible);
      if (!valueBackRefs.isEmpty()) {
        ContentValues values = new ContentValues();
        for (ValueBackRef valueBackRef : valueBackRefs) {
          final Collection<Integer> positions = parentsPositions.get(valueBackRef.parent);
          values.put(valueBackRef.column, assertThatThereIsOnlyOneParentPosition(positions));
        }
        builder.withValueBackReferences(values);
      }
      parentsPositions.put(convertible, providerOperations.size());
      providerOperations.add(builder.build());
    }
    return providerOperations;
  }

  public void putValueBackRef(ConvertibleToOperation convertible, ValueBackRef valueBackRef) {
    valueBackRefs.put(convertible, valueBackRef);
  }
}
