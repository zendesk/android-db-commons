package com.getbase.android.db.fluentsqlite;

public final class SQLiteMasterContract {
  private SQLiteMasterContract() {
  }

  public static final String TABLE = "sqlite_master";

  public interface SQLiteMasterColumns {
    public static final String NAME = "name";
    public static final String SQL = "sql";
    public static final String TYPE = "type";
  }

  public interface SQLiteSchemaPartType {
    String TABLE = "table";
    String VIEW = "view";
    String INDEX = "index";
    String TRIGGER = "trigger";
  }
}
