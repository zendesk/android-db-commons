package com.getbase.android.db.fluentsqlite;

import android.database.sqlite.SQLiteDatabase;

public final class SchemaActions {
  private SchemaActions() {
  }

  public static void dropTriggerIfExists(String trigger, SQLiteDatabase db) {
    db.execSQL("DROP TRIGGER IF EXISTS " + trigger);
  }

  public static void dropIndexIfExists(String index, SQLiteDatabase db) {
    db.execSQL("DROP TRIGGER IF EXISTS " + index);
  }
}
