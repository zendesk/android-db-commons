package com.getbase.android.db.provider;

import android.content.ContentValues;

public final class Utils {
  private Utils() {
  }

  static void addToContentValues(String key, Object value, ContentValues contentValues) {
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
}
