package com.getbase.android.db.example.ui;

import com.getbase.android.db.example.content.Contract;
import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.loaders.LoaderHelper;
import com.google.common.base.Function;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.List;

import androidx.fragment.app.ListFragment;
import androidx.loader.content.Loader;

public class SimpleTransformedStringsListFragment extends ListFragment implements LoaderHelper.LoaderDataCallbacks<List<String>> {

  private static final int LOADER_ID = 0;
  private ArrayAdapter<String> mAdapter;

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1);
    setListAdapter(mAdapter);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    loaderHelper.initLoader(this, null, this);
  }

  private static final LoaderHelper<List<String>> loaderHelper = new LoaderHelper<List<String>>(LOADER_ID) {
    @Override
    protected Loader<List<String>> onCreateLoader(Context context, Bundle args) {
      return CursorLoaderBuilder.forUri(Contract.People.CONTENT_URI)
          .projection(Contract.People.FIRST_NAME, Contract.People.SECOND_NAME)
          .transformRow(new Function<Cursor, String>() {
            @Override
            public String apply(Cursor cursor) {
              return String.format("%s %s", cursor.getString(0), cursor.getString(1));
            }
          })
          .build(context);
    }
  };

  @Override
  public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
    for (String s : data) {
      mAdapter.add(s);
    }
  }

  @Override
  public void onLoaderReset(Loader<List<String>> loader) {
    mAdapter.clear();
  }
}
