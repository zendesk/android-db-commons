package com.getbase.android.db.fluentsqlite;

public interface InsertTableWithAlgorithmSelector extends InsertTableSelector {
  InsertTableSelector orIgnore();
  InsertTableSelector orReplace();
}
