package com.getbase.android.db.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;

@Implements(AsyncTaskLoader.class)
public class ShadowAsyncTaskLoader<T> {

  @RealObject
  private AsyncTaskLoader<T> realLoader;

  @Implementation
  public void executePendingTask() {
    new AsyncTask<Void, Void, T>() {

      @Override
      protected T doInBackground(Void... voids) {
        return realLoader.loadInBackground();
      }

      @Override
      protected void onPostExecute(T result) {
        realLoader.deliverResult(result);
      }
    }.execute();
  }
}
