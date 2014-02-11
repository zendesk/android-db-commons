package com.getbase.android.db.provider;

import com.google.common.base.Function;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteStatement;

import javax.annotation.Nullable;

public final class Utils {
  private Utils() {
  }

  public static void addToContentValues(String key, Object value, ContentValues contentValues) {
    if (value == null) {
      contentValues.putNull(key);
    } else if (value instanceof String) {
      contentValues.put(key, (String) value);
    } else if (value instanceof Byte) {
      contentValues.put(key, (Byte) value);
    } else if (value instanceof Short) {
      contentValues.put(key, (Short) value);
    } else if (value instanceof Integer) {
      contentValues.put(key, (Integer) value);
    } else if (value instanceof Long) {
      contentValues.put(key, (Long) value);
    } else if (value instanceof Float) {
      contentValues.put(key, (Float) value);
    } else if (value instanceof Double) {
      contentValues.put(key, (Double) value);
    } else if (value instanceof Boolean) {
      contentValues.put(key, (Boolean) value);
    } else if (value instanceof byte[]) {
      contentValues.put(key, (byte[]) value);
    } else {
      throw new IllegalArgumentException("bad value type: " + value.getClass().getName());
    }
  }

  public static void bindContentValueArg(SQLiteStatement statement, int index, Object value) {
    if (value == null) {
      statement.bindNull(index);
    } else if (value instanceof String) {
      statement.bindString(index, (String) value);
    } else if (value instanceof Byte) {
      statement.bindLong(index, Long.valueOf((Byte) value));
    } else if (value instanceof Short) {
      statement.bindLong(index, Long.valueOf((Short) value));
    } else if (value instanceof Integer) {
      statement.bindLong(index, Long.valueOf((Integer) value));
    } else if (value instanceof Long) {
      statement.bindLong(index, (Long) value);
    } else if (value instanceof Float) {
      statement.bindDouble(index, (Float) value);
    } else if (value instanceof Double) {
      statement.bindDouble(index, (Double) value);
    } else if (value instanceof Boolean) {
      statement.bindLong(index, (Boolean) value ? 1 : 0);
    } else if (value instanceof byte[]) {
      statement.bindBlob(index, (byte[]) value);
    } else {
      throw new IllegalArgumentException("bad value type: " + value.getClass().getName());
    }
  }

  public static Object escapeSqlArg(Object arg) {
    if (arg == null) {
      return null;
    }
    if (arg instanceof Boolean) {
      return (Boolean) arg ? 1 : 0;
    }
    if(arg instanceof Number) {
      return arg;
    }
    return DatabaseUtils.sqlEscapeString(arg.toString());
  }

  public static <T> Function<T, Object> toEscapedSqlFunction() {
    return new Function<T, Object>() {
      @Nullable
      @Override
      public Object apply(@Nullable T arg) {
        return escapeSqlArg(arg);
      }
    };
  }
}
