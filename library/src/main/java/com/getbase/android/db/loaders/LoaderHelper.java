package com.getbase.android.db.loaders;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public abstract class LoaderHelper<T> {
  final int mId;

  public LoaderHelper(int id) {
    mId = id;
  }

  public final void destroyLoader(Fragment fragment) {
    fragment.getLoaderManager().destroyLoader(mId);
  }

  public final void destroyLoader(FragmentActivity activity) {
    activity.getSupportLoaderManager().destroyLoader(mId);
  }

  public final void restartLoader(Fragment fragment, Bundle args, LoaderDataCallbacks<T> callbacks) {
    restartLoader(fragment.getLoaderManager(), fragment.getActivity(), args, callbacks);
  }

  public final void restartLoader(FragmentActivity activity, Bundle args, LoaderDataCallbacks<T> callbacks) {
    restartLoader(activity.getSupportLoaderManager(), activity, args, callbacks);
  }

  private void restartLoader(LoaderManager loaderManager, Context context, Bundle args, LoaderDataCallbacks<T> callbacks) {
    loaderManager.restartLoader(mId, args, wrapCallbacks(context.getApplicationContext(), callbacks));
  }

  public final void initLoader(Fragment fragment, Bundle args, LoaderDataCallbacks<T> callbacks) {
    initLoader(fragment.getLoaderManager(), fragment.getActivity(), args, callbacks);
  }

  public final void initLoader(FragmentActivity activity, Bundle args, LoaderDataCallbacks<T> callbacks) {
    initLoader(activity.getSupportLoaderManager(), activity, args, callbacks);
  }

  private void initLoader(LoaderManager loaderManager, Context context, Bundle args, LoaderDataCallbacks<T> callbacks) {
    loaderManager.initLoader(mId, args, wrapCallbacks(context.getApplicationContext(), callbacks));
  }

  private LoaderCallbacks<T> wrapCallbacks(final Context applicationContext, final LoaderDataCallbacks<T> callbacks) {
    return new LoaderCallbacks<T>() {
      @Override
      public Loader<T> onCreateLoader(int id, Bundle args) {
        Preconditions.checkArgument(id == mId);
        return LoaderHelper.this.onCreateLoader(applicationContext, args);
      }

      @Override
      public void onLoadFinished(Loader<T> loader, T data) {
        callbacks.onLoadFinished(data);
      }

      @Override
      public void onLoaderReset(Loader<T> loader) {
        callbacks.onLoaderReset();
      }
    };
  }

  protected abstract Loader<T> onCreateLoader(Context context, Bundle args);

  public interface LoaderDataCallbacks<T> {
    public void onLoadFinished(T data);
    public void onLoaderReset();
  }
}
