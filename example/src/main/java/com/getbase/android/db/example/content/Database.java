package com.getbase.android.db.example.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

  public interface Tables {
    String PEOPLE = "people";
  }

  private static final int DB_VERSION = 1;
  public static final String DB_NAME = "sample_db";

  public Database(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table " + Tables.PEOPLE + " (" +
        Contract.PeopleColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        Contract.PeopleColumns.FIRST_NAME + " TEXT NOT NULL, " +
        Contract.PeopleColumns.SECOND_NAME + " TEXT NOT NULL)"
    );
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }
}
