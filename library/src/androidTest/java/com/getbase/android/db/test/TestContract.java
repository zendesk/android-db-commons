package com.getbase.android.db.test;

import android.net.Uri;

public class TestContract {
  public static final String AUTHORITY = "com.getbase.android.db.test.TestContentProvider";
  public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
}
