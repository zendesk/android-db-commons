package com.getbase.android.db.fluentsqlite.delete;

public interface DeleteTableSelector {
  Delete from(String table);
}
