package com.getbase.android.db.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

interface CrudHandler {

  public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws RemoteException;

  public int delete(Uri url, String selection, String[] selectionArgs) throws RemoteException;

  public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) throws RemoteException;

  public Uri insert(Uri url, ContentValues initialValues) throws RemoteException;

  public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException;
}
