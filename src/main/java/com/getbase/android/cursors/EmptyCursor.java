package com.getbase.android.cursors;

import android.database.MatrixCursor;

class EmptyCursor extends MatrixCursor {

  public EmptyCursor() {
    super(new String[] { });
  }
}
