package com.getbase.android.db.fluentsqlite;

import com.getbase.android.db.fluentsqlite.Insert.DefaultValuesInsert;

public interface InsertFormSelector extends InsertValuesBuilder, InsertSubqueryForm {
  DefaultValuesInsert defaultValues(String nullColumnHack);
}
