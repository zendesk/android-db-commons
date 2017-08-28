package android.support.v4.content;

import android.os.AsyncTask;
import android.support.v4.os.OperationCanceledException;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(AsyncTaskLoader.class)
public class ShadowAsyncTaskLoader<T> {

  @RealObject
  private AsyncTaskLoader<T> realLoader;

  @Implementation
  public void executePendingTask() {
    new AsyncTask<Void, Void, T>() {

      @Override
      protected T doInBackground(Void... voids) {
        try {
          return realLoader.loadInBackground();
        } catch (OperationCanceledException ex) {
          return null;
        }
      }

      @Override
      protected void onPostExecute(T result) {
        //Deliver result only if doInBackground was not cancelled
        if (result != null) {
          realLoader.dispatchOnLoadComplete(realLoader.mTask, result);
        } else {
          realLoader.dispatchOnCancelled(realLoader.mCancellingTask, null);
        }
      }
    }.execute();
  }
}
