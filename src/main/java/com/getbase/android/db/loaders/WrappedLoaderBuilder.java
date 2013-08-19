package com.getbase.android.db.loaders;

import com.getbase.android.db.common.QueryData;
import com.google.common.base.Function;
import com.google.common.base.Functions;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;

public class WrappedLoaderBuilder<To> {

  private final QueryData queryData;
  private final Function<Cursor, To> wrapperFunction;

  public WrappedLoaderBuilder(QueryData queryData, Function<Cursor, To> wrapperFunction) {
    this.queryData = queryData;
    this.wrapperFunction = wrapperFunction;
  }

  public <NewTo> WrappedLoaderBuilder<NewTo> wrap(Function<To, NewTo> wrapper) {
    return new WrappedLoaderBuilder<NewTo>(queryData, Functions.compose(wrapper, wrapperFunction));
  }

  public Loader<To> build(Context context) {
    return new ComposedCursorLoader<To>(context, queryData, wrapperFunction);
  }
}
