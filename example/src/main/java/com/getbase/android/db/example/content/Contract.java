package com.getbase.android.db.example.content;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {

  public static final String AUTHORITY = "com.getbase.android.db.example";
  public static final Uri BASE_URI = Uri.parse(String.format("content://%s", AUTHORITY));

  public interface Paths {
    String PEOPLE = "people";
  }

  public interface PeopleColumns extends BaseColumns {

    String FIRST_NAME = "first_name";
    String SECOND_NAME = "second_name";
  }

  public static class People implements PeopleColumns {
    public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(Paths.PEOPLE).build();

    public static Uri buildUriForItem(long itemId) {
      return ContentUris.withAppendedId(CONTENT_URI, itemId);
    }
  }
}
