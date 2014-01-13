package com.getbase.android.db.loaders;

import com.getbase.android.db.common.QueryData;
import com.getbase.android.db.cursors.Cursors;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;

import java.util.List;

public class TransformedLoaderBuilder<T> {

  private final QueryData queryData;
  private final Function<Cursor, T> cursorTransformation;

  public TransformedLoaderBuilder(QueryData queryData, Function<Cursor, T> transformation) {
    this.queryData = queryData;
    this.cursorTransformation = transformation;
  }

  public <Out> TransformedLoaderBuilder<Out> transform(final Function<T, Out> function) {
    return new TransformedLoaderBuilder<Out>(queryData, Functions.compose(function, cursorTransformation));
  }

  public WrappedLoaderBuilder<List<T>> lazy() {
    return new WrappedLoaderBuilder<List<T>>(queryData, getLazyTransformationFunction());
  }

  public <Out> WrappedLoaderBuilder<Out> wrap(final Function<List<T>, Out> wrapper) {
    return new WrappedLoaderBuilder<Out>(queryData, Functions.compose(wrapper, getEagerTransformationFunction())
    );
  }

  public Loader<List<T>> build(Context context) {
    return new ComposedCursorLoader<List<T>>(context, queryData, getEagerTransformationFunction());
  }

  private Function<Cursor, List<T>> getEagerTransformationFunction() {
    return new Function<Cursor, List<T>>() {
      @Override
      public List<T> apply(Cursor input) {
        return Lists.newArrayList(Cursors.toFluentIterable(input, cursorTransformation));
      }
    };
  }

  private Function<Cursor, List<T>> getLazyTransformationFunction() {
    return new Function<Cursor, List<T>>() {
      @Override
      public List<T> apply(Cursor cursor) {
        return new LazyCursorList<T>(cursor, cursorTransformation);
      }
    };
  }
}
