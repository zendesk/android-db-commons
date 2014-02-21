package com.getbase.android.db.provider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Batcher {

  public static Batcher begin() {
    return new Batcher();
  }

  private static class BackRef {

    private final ConvertibleToOperation parent;
    private final String column;

    private BackRef(ConvertibleToOperation parent, String column) {
      this.parent = parent;
      this.column = column;
    }
  }

  private final List<ConvertibleToOperation> operations = Lists.newArrayList();

  private final Multimap<ConvertibleToOperation, BackRef> backRefs = HashMultimap.create();
  private final Set<ConvertibleToOperation> parents = Sets.newHashSet();

  private Batcher() {
  }

  public Batcher append(Batcher batcher) {
    backRefs.putAll(batcher.backRefs);
    parents.addAll(batcher.parents);
    return append(batcher.operations);
  }

  public Batcher append(ConvertibleToOperation... convertibles) {
    operations.addAll(Lists.newArrayList(convertibles));
    return this;
  }

  public Batcher append(Iterable<ConvertibleToOperation> convertibles) {
    operations.addAll(Lists.newArrayList(convertibles));
    return this;
  }

  public Batcher append(ConvertibleToOperation convertible) {
    operations.add(convertible);
    return this;
  }

  public BackRefBuilder appendWithBackRef(ConvertibleToOperation convertibleToOperation) {
    return new BackRefBuilder(convertibleToOperation);
  }

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
      if (parents.contains(convertible)) {
        parentsPositions.put(convertible, providerOperations.size());
      }
      providerOperations.add(builder.build());
    }
    return providerOperations;
  }

  public ContentProviderResult[] applyBatch(ContentProvider provider) {
    try {
      return applyBatch(null, new ContentProviderCrudHandler(provider));
    } catch (RemoteException neverThrown) {
      throw new RuntimeException(neverThrown);
    } catch (OperationApplicationException neverThrown) {
      throw new RuntimeException(neverThrown);
    }
  }

  public ContentProviderResult[] applyBatch(ContentProviderClient providerClient) throws RemoteException, OperationApplicationException {
    return applyBatch(null, new ContentProviderClientCrudHandler(providerClient));
  }

  public ContentProviderResult[] applyBatch(String authority, ContentResolver resolver) throws RemoteException, OperationApplicationException {
    return applyBatch(authority, new ContentResolverCrudHandler(resolver));
  }

  private ContentProviderResult[] applyBatch(String authority, CrudHandler crudHandler) throws RemoteException, OperationApplicationException {
    return crudHandler.applyBatch(authority, operations());
  }

  public class BackRefBuilder {

    private final ConvertibleToOperation convertible;

    public BackRefBuilder(ConvertibleToOperation convertible) {
      this.convertible = convertible;
    }

    public BackRefBuilder forPrevious(ConvertibleToOperation operation, String columnName) {
      parents.add(operation);
      backRefs.put(convertible, new BackRef(operation, columnName));
      return this;
    }

    public Batcher batch() {
      operations.add(convertible);
      return Batcher.this;
    }
  }
}
