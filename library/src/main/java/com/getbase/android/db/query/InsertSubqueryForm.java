package com.getbase.android.db.query;

import com.getbase.android.db.query.Insert.InsertWithSelect;

public interface InsertSubqueryForm {
  InsertSubqueryForm columns(String... columns);
  InsertWithSelect resultOf(Query query);
}
