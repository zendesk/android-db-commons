package com.getbase.android.db;

import com.getbase.android.db.shadows.CustomShadowMatrixCursor;
import com.getbase.android.db.shadows.ShadowAsyncTaskLoader;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.bytecode.ShadowMap;

public class CustomRobolectricTestRunner extends RobolectricTestRunner {

  public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected ShadowMap createShadowMap() {
    return super.createShadowMap()
        .newBuilder()
        .addShadowClass(ShadowAsyncTaskLoader.class)
        .addShadowClass(CustomShadowMatrixCursor.class)
        .build();
  }
}
