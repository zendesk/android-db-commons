package com.github.partition.android.database.cursors;

import android.database.MatrixCursor;

class EmptyCursor extends MatrixCursor {

  public EmptyCursor() {
    super(new String[] { });
  }
}
