package com.getbase.android.db.cursors;

import android.database.MatrixCursor;

class EmptyCursor extends MatrixCursor {

  public EmptyCursor() {
    super(new String[] { });
  }

  @Override
  public int getColumnIndexOrThrow(String columnName) {
    return -1;
  }

  @Override
  public int getColumnIndex(String columnName) {
    return -1;
  }
}
