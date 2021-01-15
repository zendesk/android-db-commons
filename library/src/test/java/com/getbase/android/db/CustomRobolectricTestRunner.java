package com.getbase.android.db;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.bytecode.ShadowMap;

import androidx.loader.content.ShadowAsyncTaskLoader;

public class CustomRobolectricTestRunner extends RobolectricTestRunner {

  public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected ShadowMap createShadowMap() {
    return super.createShadowMap()
        .newBuilder()
        .addShadowClass(ShadowAsyncTaskLoader.class)
        .build();
  }
}
