package com.getbase.android.db.provider;

import android.content.ContentProviderOperation;

public interface ConvertibleToOperation {

  ContentProviderOperation toContentProviderOperation();
}
