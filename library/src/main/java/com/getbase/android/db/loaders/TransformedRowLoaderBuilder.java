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

public class TransformedRowLoaderBuilder<T> {

  private final QueryData queryData;
  private final Function<Cursor, T> cursorTransformation;

  public TransformedRowLoaderBuilder(QueryData queryData, Function<Cursor, T> transformation) {
    this.queryData = queryData;
    this.cursorTransformation = transformation;
  }

  public <Out> TransformedRowLoaderBuilder<Out> transformRow(final Function<T, Out> rowTransformer) {
    return new TransformedRowLoaderBuilder<Out>(queryData, Functions.compose(rowTransformer, cursorTransformation));
  }

  public TransformedLoaderBuilder<List<T>> lazy() {
    return new TransformedLoaderBuilder<List<T>>(queryData, getLazyTransformationFunction());
  }

  public <Out> TransformedLoaderBuilder<Out> transform(final Function<List<T>, Out> transformer) {
    return new TransformedLoaderBuilder<Out>(queryData, Functions.compose(transformer, getEagerTransformationFunction())
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
