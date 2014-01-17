package com.getbase.android.db.fluentsqlite.insert;

import android.content.ContentValues;

public interface InsertValuesBuilder {
  Insert values(ContentValues values);
  Insert value(String column, Object value);
}
