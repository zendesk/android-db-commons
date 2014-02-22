package com.getbase.android.db.provider;

import com.getbase.android.db.provider.BatcherImpl.BackRefBuilder;
import com.google.common.collect.Multimap;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

public abstract class Batcher {

  public static Batcher begin() {
    return new BatcherImpl();
  }

  public abstract Batcher append(Batcher Batcher);

  public abstract Batcher append(ConvertibleToOperation... convertibles);

  public abstract Batcher append(Iterable<ConvertibleToOperation> convertibles);

  public abstract Batcher append(ConvertibleToOperation convertible);

  public abstract BackRefBuilder appendWithBackRef(ConvertibleToOperation convertibleToOperation);

  public abstract ArrayList<ContentProviderOperation> operations();

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

  static class BackRef {

    final Insert parent;
    final String column;

    BackRef(Insert parent, String column) {
      this.parent = parent;
      this.column = column;
    }
  }

  protected abstract Multimap<ConvertibleToOperation, BackRef> getBackRefsMultimap();
  protected abstract List<ConvertibleToOperation> getConvertibles();
}
