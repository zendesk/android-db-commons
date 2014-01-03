package com.getbase.android.db.query;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Query {

  public void test() {
    select();
    selectDistinct();
    selectDistinct().column("X");
    selectDistinct().column("X").as("lol");
    select().columns("X", "Y", "Z");
    select().allColumns();
    select().allColumnsOf("T");

    select().allColumns().from("T");
    select().allColumns().from("T").as("asd");

    select().allColumns().from("T").left().join("U");
    select().allColumns().from("T").join("U");
    select().allColumns().from("T").natural().cross().join("U");
    select().allColumns().from("T").natural().cross().join("U").as("wat");

    select().allColumns().from("T").join("U").using("A");
    select().allColumns().from("T").join("U").using("A", "B");

    select().allColumns().from("T").join("U").on("A");
    select().allColumns().from("T").join("U").on("A").on("B");

    select().allColumns().from("T").where("A == B");
    select().allColumns().from("T").as("X").where("A == B");
    select().allColumns().from("T").join("U").on("A").where("A == B");
    select().allColumns().from("T").join("U").on("A").left().join("Z").where("A == B");

    select().allColumns().from("T").groupBy("X");
    select().allColumns().from("T").as("X").groupBy("X");
    select().allColumns().from("T").where("A == B").groupBy("X");
    select().allColumns().from("T").as("X").where("A == B").groupBy("X");
    select().allColumns().from("T").join("U").on("A").where("A == B").groupBy("X");
    select().allColumns().from("T").join("U").on("A").left().join("Z").where("A == B").groupBy("X");

    select().allColumns().from("T").groupBy("X").having("Z");
    select().allColumns().from("T").as("X").groupBy("X").having("Z");
    select().allColumns().from("T").where("A == B").groupBy("X").having("Z");
    select().allColumns().from("T").as("X").where("A == B").groupBy("X").having("Z");
    select().allColumns().from("T").join("U").on("A").where("A == B").groupBy("X").having("Z");
    select().allColumns().from("T").join("U").on("A").left().join("Z").where("A == B").groupBy("X").having("Z");

    select().allColumns().from("T").orderBy("X");
    select().allColumns().from("T").orderBy("X").orderBy("Y");
    select().allColumns().from("T").orderBy("X").asc();
    select().allColumns().from("T").orderBy("X").desc();
    select().allColumns().from("T").orderBy("X").collate("localized");
    select().allColumns().from("T").orderBy("X").collate("localized").desc();
    select().allColumns().from("T").orderBy("X").collate("localized").asc();

    select().allColumns().from("T").orderBy("X");
    select().allColumns().from("T").as("X").orderBy("X");
    select().allColumns().from("T").where("A == B").orderBy("X");
    select().allColumns().from("T").as("X").where("A == B").orderBy("X");
    select().allColumns().from("T").join("U").on("A").where("A == B").orderBy("X");
    select().allColumns().from("T").join("U").on("A").left().join("Z").where("A == B").orderBy("X");
    
    select().allColumns().from("T").groupBy("X").orderBy("Z");
    select().allColumns().from("T").as("X").groupBy("X").orderBy("Z");
    select().allColumns().from("T").where("A == B").groupBy("X").orderBy("Z");
    select().allColumns().from("T").as("X").where("A == B").groupBy("X").orderBy("Z");
    select().allColumns().from("T").join("U").on("A").where("A == B").groupBy("X").orderBy("Z");
    select().allColumns().from("T").join("U").on("A").left().join("Z").where("A == B").groupBy("X").orderBy("Z");

    select().allColumns().from("T").orderBy("X").limit("10");
    select().allColumns().from("T").as("X").orderBy("X").limit("10");
    select().allColumns().from("T").where("A == B").orderBy("X").limit("10");
    select().allColumns().from("T").as("X").where("A == B").orderBy("X").limit("10");
    select().allColumns().from("T").join("U").on("A").where("A == B").orderBy("X").limit("10");
    select().allColumns().from("T").join("U").on("A").left().join("Z").where("A == B").orderBy("X").limit("10");
    
    select().allColumns().from("T").groupBy("X").orderBy("Z").limit("10");
    select().allColumns().from("T").as("X").groupBy("X").orderBy("Z").limit("10");
    select().allColumns().from("T").where("A == B").groupBy("X").orderBy("Z").limit("10");
    select().allColumns().from("T").as("X").where("A == B").groupBy("X").orderBy("Z").limit("10");
    select().allColumns().from("T").join("U").on("A").where("A == B").groupBy("X").orderBy("Z").limit("10");
    select().allColumns().from("T").join("U").on("A").left().join("Z").where("A == B").groupBy("X").orderBy("Z").limit("10");

    select().allColumns().from("T").build();

    select().allColumns().from("T").union().all().select().allColumns().from("X").build();
    select().allColumns().from("T").groupBy("X").having("Z").union().select().allColumns().from("X").build();
  }

  public static QueryBuilder select() {
    return null;
  }

  public static QueryBuilder selectDistinct() {
    return null;
  }

  public Cursor perform(SQLiteDatabase db) {
    return db.rawQuery(null, null);
  }

  public interface QueryBuilder extends TableSelector, ColumnSelector, SelectionBuilder, NaturalJoinTypeBuilder, GroupByBuilder, HavingBuilder, OrderByBuilder, LimitBuilder, CompoundQueryBuilder {
    Query build();
  }

  public interface TableSelector {
    TableAliasBuilder from(String table);
    TableAliasBuilder from(Query subquery);
  }

  public interface TableAliasBuilder extends QueryBuilder {
    QueryBuilder as(String alias);
  }

  public interface ColumnSelector {
    ColumnAliasBuilder column(String column);
    QueryBuilder columns(String... columns);
    QueryBuilder allColumns();
    QueryBuilder allColumnsOf(String table);
  }

  public interface ColumnAliasBuilder extends QueryBuilder {
    QueryBuilder as(String alias);
  }

  public interface SelectionBuilder {
    QueryBuilder where(String selection, Object... selectionArgs);
  }

  public interface JoinTypeBuilder extends JoinBuilder {
    JoinBuilder left();
    JoinBuilder cross();
  }

  public interface NaturalJoinTypeBuilder extends JoinTypeBuilder {
    JoinTypeBuilder natural();
  }

  public interface JoinBuilder {
    JoinAliasBuilder join(String table);
    JoinAliasBuilder join(Query subquery);
  }

  public interface JoinAliasBuilder extends JoinConstraintBuilder {
    JoinConstraintBuilder as(String alias);
  }

  public interface JoinConstraintBuilder extends JoinOnConstraintBuilder {
    QueryBuilder using(String... columns);
  }

  public interface JoinOnConstraintBuilder extends QueryBuilder {
    JoinOnConstraintBuilder on(String constraint, Object... constraintArgs);
  }

  public interface GroupByBuilder {
    QueryBuilder groupBy(String expression);
  }

  public interface HavingBuilder {
    QueryBuilder having(String having, Object... havingArgs);
  }

  public interface OrderByBuilder {
    OrderingTermBuilder orderBy(String expression);
  }

  public interface OrderingTermBuilder extends OrderingDirectionSelector {
    OrderingDirectionSelector collate(String collation);
  }

  public interface OrderingDirectionSelector extends QueryBuilder {
    QueryBuilder asc();
    QueryBuilder desc();
  }

  public interface LimitBuilder {
    LimitOffsetBuilder limit(String expression);
    LimitOffsetBuilder limit(int limit);
  }

  public interface LimitOffsetBuilder extends QueryBuilder {
    QueryBuilder offset(String expression);
    QueryBuilder offset(int limit);
  }

  public interface CompoundQueryBuilder {
    UnionTypeSelector union();
    SelectTypeSelector intersect();
    SelectTypeSelector except();
  }

  public interface UnionTypeSelector extends SelectTypeSelector {
    SelectTypeSelector all();
  }

  public interface SelectTypeSelector {
    QueryBuilder select();
    QueryBuilder selectDistinct();
  }
}
