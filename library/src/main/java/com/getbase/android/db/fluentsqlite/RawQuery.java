package com.getbase.android.db.fluentsqlite;

import java.util.List;

public class RawQuery {
  public final String mRawQuery;
  public final List<String> mRawQueryArgs;

  public RawQuery(String rawQuery, List<String> rawQueryArgs) {
    mRawQuery = rawQuery;
    mRawQueryArgs = rawQueryArgs;
  }
}
