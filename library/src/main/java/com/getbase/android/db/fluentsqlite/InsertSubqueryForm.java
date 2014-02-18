package com.getbase.android.db.fluentsqlite;

import com.getbase.android.db.fluentsqlite.Insert.InsertWithSelect;
import com.getbase.android.db.fluentsqlite.QueryBuilder.Query;

public interface InsertSubqueryForm {
  InsertSubqueryForm columns(String... columns);
  InsertWithSelect resultOf(Query query);
}
