package com.getbase.android.db.query.insert;

import com.getbase.android.db.query.insert.Insert.InsertWithSelect;
import com.getbase.android.db.query.query.Query;

public interface InsertSubqueryForm {
  InsertSubqueryForm columns(String... columns);
  InsertWithSelect resultOf(Query query);
}
