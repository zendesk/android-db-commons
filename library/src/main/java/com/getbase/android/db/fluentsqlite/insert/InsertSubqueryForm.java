package com.getbase.android.db.fluentsqlite.insert;

import com.getbase.android.db.fluentsqlite.insert.Insert.InsertWithSelect;
import com.getbase.android.db.fluentsqlite.query.QueryBuilder.Query;

public interface InsertSubqueryForm {
  InsertSubqueryForm columns(String... columns);
  InsertWithSelect resultOf(Query query);
}
