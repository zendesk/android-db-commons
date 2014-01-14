package com.getbase.android.db.query;

import com.getbase.android.db.query.Insert.DefaultValuesInsert;

public interface InsertFormSelector extends InsertValuesBuilder, InsertSubqueryForm {
  DefaultValuesInsert defaultValues(String nullColumnHack);
}
