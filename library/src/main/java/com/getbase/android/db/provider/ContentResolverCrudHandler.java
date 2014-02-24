package com.getbase.android.db.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

class ContentResolverCrudHandler implements CrudHandler {

  private final ContentResolver contentResolver;

  ContentResolverCrudHandler(ContentResolver contentResolver) {
    this.contentResolver = contentResolver;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
    return contentResolver.query(uri, projection, selection, selectionArgs, orderBy);
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return contentResolver.insert(uri, values);
  }

  @Override
  public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
    return contentResolver.applyBatch(authority, operations);
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return contentResolver.update(uri, values, selection, selectionArgs);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return contentResolver.delete(uri, selection, selectionArgs);
  }
}
