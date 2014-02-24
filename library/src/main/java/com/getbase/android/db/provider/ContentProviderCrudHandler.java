package com.getbase.android.db.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

public class ContentProviderCrudHandler implements CrudHandler {

  private final ContentProvider contentProvider;

  public ContentProviderCrudHandler(ContentProvider contentProvider) {
    this.contentProvider = contentProvider;
  }

  @Override
  public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws RemoteException {
    return contentProvider.query(url, projection, selection, selectionArgs, sortOrder);
  }

  @Override
  public int delete(Uri url, String selection, String[] selectionArgs) throws RemoteException {
    return contentProvider.delete(url, selection, selectionArgs);
  }

  @Override
  public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) throws RemoteException {
    return contentProvider.update(url, values, selection, selectionArgs);
  }

  @Override
  public Uri insert(Uri url, ContentValues initialValues) throws RemoteException {
    return contentProvider.insert(url, initialValues);
  }

  @Override
  public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
    return contentProvider.applyBatch(operations);
  }
}
