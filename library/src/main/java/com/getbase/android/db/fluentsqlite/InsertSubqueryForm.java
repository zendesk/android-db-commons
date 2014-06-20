package com.getbase.android.db.fluentsqlite;

import com.getbase.android.db.fluentsqlite.Insert.InsertWithSelect;

public interface InsertSubqueryForm {
  InsertSubqueryForm columns(String... columns);
  InsertWithSelect resultOf(Query query);
}
