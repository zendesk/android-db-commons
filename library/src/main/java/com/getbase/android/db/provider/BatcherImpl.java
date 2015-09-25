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
  private UriDecorator mUriDecorator = Utils.DUMMY_URI_DECORATOR;
  private Multimap<ConvertibleToOperation, ValueBackRef> mValueBackRefs;
  private Multimap<ConvertibleToOperation, SelectionBackRef> mSelectionBackRefs;

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

  @Override
  public ArrayList<ContentProviderOperation> operations() {
    ArrayList<ContentProviderOperation> providerOperations = Lists.newArrayListWithCapacity(operations.size());
    BackRefResolver backRefResolver = getBackRefResolver();

    for (ConvertibleToOperation convertible : operations) {
      final Builder builder = convertible.toContentProviderOperationBuilder(mUriDecorator);

      backRefResolver.resolveBackRefs(convertible, builder);

      providerOperations.add(builder.build());
    }
    return providerOperations;
  }

  public void putValueBackRef(ConvertibleToOperation convertible, ValueBackRef valueBackRef) {
    if (mValueBackRefs == null) {
      mValueBackRefs = HashMultimap.create();
    }

    mValueBackRefs.put(convertible, valueBackRef);
  }

  public void putSelectionBackRef(ConvertibleToOperation convertible, SelectionBackRef selectionBackRef) {
    if (mSelectionBackRefs == null) {
      mSelectionBackRefs = HashMultimap.create();
    }

    mSelectionBackRefs.put(convertible, selectionBackRef);
  }

  private BackRefResolver getBackRefResolver() {
    if (mValueBackRefs == null && mSelectionBackRefs == null) {
      return DUMMY_BACK_REF_RESOLVER;
    } else {
      return new BackRefResolverImpl(mValueBackRefs, mSelectionBackRefs);
    }
  }

  interface BackRefResolver {
    void resolveBackRefs(ConvertibleToOperation convertible, Builder builder);
  }

  private static class BackRefResolverImpl implements BackRefResolver {
    private final Multimap<ConvertibleToOperation, ValueBackRef> mValueBackRefs;
    private final Multimap<ConvertibleToOperation, SelectionBackRef> mSelectionBackRefs;
    private final Multimap<ConvertibleToOperation, Integer> mParentsPosition;

    public BackRefResolverImpl(Multimap<ConvertibleToOperation, ValueBackRef> valueBackRefs, Multimap<ConvertibleToOperation, SelectionBackRef> selectionBackRefs) {
      mValueBackRefs = valueBackRefs;
      mSelectionBackRefs = selectionBackRefs;
      mParentsPosition = HashMultimap.create();
    }

    @Override
    public void resolveBackRefs(ConvertibleToOperation convertible, Builder builder) {
      if (mValueBackRefs != null && mValueBackRefs.containsKey(convertible)) {
        ContentValues values = new ContentValues();

        for (ValueBackRef valueBackRef : mValueBackRefs.get(convertible)) {
          values.put(valueBackRef.column, getParentPosition(valueBackRef.parent));
        }

        builder.withValueBackReferences(values);
      }

      if (mSelectionBackRefs != null) {
        for (SelectionBackRef selectionBackRef : mSelectionBackRefs.get(convertible)) {
          builder.withSelectionBackReference(
              selectionBackRef.selectionArgumentIndex,
              getParentPosition(selectionBackRef.parent)
          );
        }
      }

      mParentsPosition.put(convertible, mParentsPosition.size());
    }

    private int getParentPosition(ConvertibleToOperation parent) {
      Collection<Integer> positions = mParentsPosition.get(parent);

      if (positions.isEmpty()) {
        throw new IllegalStateException("Could not find operation used in back reference.");
      } else if (positions.size() > 1) {
        throw new IllegalStateException("Ambiguous back reference; referenced operation was added to Batcher more than once.");
      }

      return Iterables.getOnlyElement(positions);
    }
  }

  private static final BackRefResolver DUMMY_BACK_REF_RESOLVER = new BackRefResolver() {
    @Override
    public void resolveBackRefs(ConvertibleToOperation convertible, Builder builder) {
    }
  };
}
