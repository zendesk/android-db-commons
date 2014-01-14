package com.getbase.android.db.query.insert;

import com.getbase.android.db.query.insert.Insert.DefaultValuesInsert;

public interface InsertFormSelector extends InsertValuesBuilder, InsertSubqueryForm {
  DefaultValuesInsert defaultValues(String nullColumnHack);
}
