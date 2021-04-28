package com.getbase.android.db.cursors;

import android.annotation.SuppressLint;
import android.database.MatrixCursor;

class EmptyCursor extends MatrixCursor {

  public EmptyCursor() {
    super(new String[] { });
  }

  @Override
  @SuppressLint("Range")
  public int getColumnIndexOrThrow(String columnName) {
    return -1;
  }

  @Override
  public int getColumnIndex(String columnName) {
    return -1;
  }
}
