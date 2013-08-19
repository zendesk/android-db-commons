package com.getbase.android.db.shadows;

import com.google.common.base.Preconditions;

import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowMatrixCursor;

import android.database.MatrixCursor;

@Implements(MatrixCursor.class)
public class CustomShadowMatrixCursor extends ShadowMatrixCursor {

  @Override
  protected void setPosition(int pos) {
    Preconditions.checkState(!isClosed(), "Cursor is already closed");
    super.setPosition(pos);
  }

}
