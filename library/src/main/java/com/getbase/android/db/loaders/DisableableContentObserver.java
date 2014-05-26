package com.getbase.android.db.loaders;

import android.database.ContentObserver;
import android.os.Handler;

public class DisableableContentObserver extends ContentObserver {
  private final ContentObserver mWrappedObserver;
  private boolean mIsEnabled = true;

  public DisableableContentObserver(ContentObserver wrappedObserver) {
    super(new Handler());
    mWrappedObserver = wrappedObserver;
  }

  @Override
  public void onChange(boolean selfChange) {
    if (mIsEnabled) {
      mWrappedObserver.onChange(selfChange);
    }
  }

  public void setEnabled(boolean isEnabled) {
    mIsEnabled = isEnabled;
  }
}
