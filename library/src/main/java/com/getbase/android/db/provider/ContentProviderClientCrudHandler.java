package com.getbase.android.db.provider;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

class ContentProviderClientCrudHandler implements CrudHandler {

  private final ContentProviderClient contentProviderClient;

  ContentProviderClientCrudHandler(ContentProviderClient contentProviderClient) {
    this.contentProviderClient = contentProviderClient;
  }

  @Override
  public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws RemoteException {
    return contentProviderClient.query(url, projection, selection, selectionArgs, sortOrder);
  }

  @Override
  public int delete(Uri url, String selection, String[] selectionArgs) throws RemoteException {
    return contentProviderClient.delete(url, selection, selectionArgs);
  }

  @Override
  public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) throws RemoteException {
    return contentProviderClient.update(url, values, selection, selectionArgs);
  }

  @Override
  public Uri insert(Uri url, ContentValues initialValues) throws RemoteException {
    return contentProviderClient.insert(url, initialValues);
  }

  @Override
  public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
    return contentProviderClient.applyBatch(operations);
  }
}
