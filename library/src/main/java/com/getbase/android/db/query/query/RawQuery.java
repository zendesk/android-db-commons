package com.getbase.android.db.query.query;

public class RawQuery {
  public final String mRawQuery;
  public final String[] mRawQueryArgs;

  public RawQuery(String rawQuery, String... rawQueryArgs) {
    mRawQuery = rawQuery;
    mRawQueryArgs = rawQueryArgs;
  }
}
