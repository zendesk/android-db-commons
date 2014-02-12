package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.base.Objects;

import android.database.Cursor;

/**
 * Builders for {@link com.google.common.base.Function} transforming a single
 * {@link android.database.Cursor} row to {@link java.lang.String} or boxed
 * primitive type.
 *
 * The constructed Functions are not thread-safe, but can be reused with
 * multiple Cursors.
 *
 * All constructed Functions return null when the {@link android.database.Cursor#isNull(int)}
 * for the specified column returns true.
 */

public final class SingleRowTransforms {
  private SingleRowTransforms() {
  }

  /**
   * Constructs new {@link Builder} for specified {@code columnName}.
   */
  public static Builder getColumn(String columnName) {
    return new Builder(columnName);
  }

  public static class Builder {
    private final String mColumnName;

    Builder(String columnName) {
      mColumnName = columnName;
    }

    /**
     * Constructs {@link com.google.common.base.Function} converting the
     * specified column in {@link android.database.Cursor} row to
     * {@link java.lang.String}
     */
    public Function<Cursor, String> asString() {
      return new CursorRowFunction<String>(mColumnName) {
        @Override
        protected String getValue(Cursor c, int columnIndex) {
          return c.getString(columnIndex);
        }
      };
    }

    /**
     * Constructs {@link com.google.common.base.Function} converting the
     * specified column in {@link android.database.Cursor} row to
     * {@link java.lang.Short}
     */
    public Function<Cursor, Short> asShort() {
      return new CursorRowFunction<Short>(mColumnName) {
        @Override
        protected Short getValue(Cursor c, int columnIndex) {
          return c.getShort(columnIndex);
        }
      };
    }

    /**
     * Constructs {@link com.google.common.base.Function} converting the
     * specified column in {@link android.database.Cursor} row to
     * {@link java.lang.Integer}
     */
    public Function<Cursor, Integer> asInteger() {
      return new CursorRowFunction<Integer>(mColumnName) {
        @Override
        protected Integer getValue(Cursor c, int columnIndex) {
          return c.getInt(columnIndex);
        }
      };
    }

    /**
     * Constructs {@link com.google.common.base.Function} converting the
     * specified column in {@link android.database.Cursor} row to
     * {@link java.lang.Long}
     */
    public Function<Cursor, Long> asLong() {
      return new CursorRowFunction<Long>(mColumnName) {
        @Override
        protected Long getValue(Cursor c, int columnIndex) {
          return c.getLong(columnIndex);
        }
      };
    }

    /**
     * Constructs {@link com.google.common.base.Function} converting the
     * specified column in {@link android.database.Cursor} row to
     * {@link java.lang.Float}
     */
    public Function<Cursor, Float> asFloat() {
      return new CursorRowFunction<Float>(mColumnName) {
        @Override
        protected Float getValue(Cursor c, int columnIndex) {
          return c.getFloat(columnIndex);
        }
      };
    }

    /**
     * Constructs {@link com.google.common.base.Function} converting the
     * specified column in {@link android.database.Cursor} row to
     * {@link java.lang.Double}
     */
    public Function<Cursor, Double> asDouble() {
      return new CursorRowFunction<Double>(mColumnName) {
        @Override
        protected Double getValue(Cursor c, int columnIndex) {
          return c.getDouble(columnIndex);
        }
      };
    }

    /**
     * Constructs {@link com.google.common.base.Function} converting the
     * specified column in {@link android.database.Cursor} row to
     * {@link java.lang.Boolean}, by fetching column as integer. The Function
     * returns true when the integer value of the column is 1, otherwise it
     * returns false.
     */
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
