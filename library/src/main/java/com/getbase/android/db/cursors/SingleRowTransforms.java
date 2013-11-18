package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.base.Objects;

import android.database.Cursor;

public abstract class SingleRowTransforms {
  private SingleRowTransforms() {
  }

  public static Builder getColumn(String columnName) {
    return new Builder(columnName);
  }

  public static class Builder {
    private final String mColumnName;

    Builder(String columnName) {
      mColumnName = columnName;
    }

    public Function<Cursor, String> asString() {
      return new CursorRowFunction<String>(mColumnName) {
        @Override
        protected String getValue(Cursor c, int columnIndex) {
          return c.getString(columnIndex);
        }
      };
    }

    public Function<Cursor, Short> asShort() {
      return new CursorRowFunction<Short>(mColumnName) {
        @Override
        protected Short getValue(Cursor c, int columnIndex) {
          return c.getShort(columnIndex);
        }
      };
    }

    public Function<Cursor, Integer> asInteger() {
      return new CursorRowFunction<Integer>(mColumnName) {
        @Override
        protected Integer getValue(Cursor c, int columnIndex) {
          return c.getInt(columnIndex);
        }
      };
    }

    public Function<Cursor, Long> asLong() {
      return new CursorRowFunction<Long>(mColumnName) {
        @Override
        protected Long getValue(Cursor c, int columnIndex) {
          return c.getLong(columnIndex);
        }
      };
    }

    public Function<Cursor, Float> asFloat() {
      return new CursorRowFunction<Float>(mColumnName) {
        @Override
        protected Float getValue(Cursor c, int columnIndex) {
          return c.getFloat(columnIndex);
        }
      };
    }

    public Function<Cursor, Double> asDouble() {
      return new CursorRowFunction<Double>(mColumnName) {
        @Override
        protected Double getValue(Cursor c, int columnIndex) {
          return c.getDouble(columnIndex);
        }
      };
    }

    public Function<Cursor, Boolean> asBoolean() {
      return new CursorRowFunction<Boolean>(mColumnName) {
        @Override
        protected Boolean getValue(Cursor c, int columnIndex) {
          return c.getInt(columnIndex) == 1;
        }
      };
    }
  }

  private abstract static class CursorRowFunction<T> implements Function<Cursor, T> {
    protected final String mColumnName;
    private int mColumnIndex;
    private Cursor mInitializedForCursor;

    protected CursorRowFunction(String columnName) {
      mColumnName = columnName;
    }

    private int getColumnIndex(Cursor c) {
      if (!Objects.equal(mInitializedForCursor, c)) {
        mColumnIndex = c.getColumnIndexOrThrow(mColumnName);
        mInitializedForCursor = c;
      }

      return mColumnIndex;
    }

    @Override
    public final T apply(Cursor c) {
      int index = getColumnIndex(c);
      return c.isNull(index)
          ? null
          : getValue(c, index);
    }

    protected abstract T getValue(Cursor c, int columnIndex);
  }
}
