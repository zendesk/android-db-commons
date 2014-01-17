package com.getbase.android.db.fluentsqlite.insert;

import com.getbase.android.db.fluentsqlite.insert.Insert.DefaultValuesInsert;

public interface InsertFormSelector extends InsertValuesBuilder, InsertSubqueryForm {
  DefaultValuesInsert defaultValues(String nullColumnHack);
}
