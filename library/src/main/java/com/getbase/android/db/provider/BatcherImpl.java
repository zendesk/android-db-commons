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
  private final Multimap<ConvertibleToOperation, BackRef> backRefs = HashMultimap.create();
  private UriDecorator mUriDecorator = Utils.DUMMY_URI_DECORATOR;

  @Override
  public BackRefBuilder append(ConvertibleToOperation... convertibles) {
    Collections.addAll(operations, convertibles);
    return new BackRefBuilder(this, convertibles);
  }

  @Override
  public BackRefBuilder append(Iterable<ConvertibleToOperation> convertibles) {
    operations.addAll(Lists.newArrayList(convertibles));
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
      final Collection<BackRef> backRefs = this.backRefs.get(convertible);
      if (!backRefs.isEmpty()) {
        ContentValues values = new ContentValues();
        for (BackRef backRef : backRefs) {
          final Collection<Integer> positions = parentsPositions.get(backRef.parent);
          values.put(backRef.column, assertThatThereIsOnlyOneParentPosition(positions));
        }
        builder.withValueBackReferences(values);
      }
      parentsPositions.put(convertible, providerOperations.size());
      providerOperations.add(builder.build());
    }
    return providerOperations;
  }

  public void putBackRef(ConvertibleToOperation convertible, BackRef backRef) {
    backRefs.put(convertible, backRef);
  }
}
