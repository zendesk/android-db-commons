package com.getbase.android.db.query;

public class Query {

  public void test() {
    select();
    select().distinct();
    select().distinct().column("X");
    select().distinct().column("X").as("lol");
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

  public static SelectDistinctCore select() {
    return null;
  }

  public interface SelectCore {
    SelectDistinctCore select();
  }

  public interface SelectDistinctCore extends ColumnBuilder {
    ColumnBuilder distinct();
  }

  public interface ColumnBuilder extends TableSelector {
    ColumnAliasBuilder column(String column);
    ColumnBuilder columns(String... columns);
    ColumnBuilder allColumns();
    ColumnBuilder allColumnsOf(String table);
  }

  public interface ColumnAliasBuilder extends ColumnBuilder {
    ColumnBuilder as(String alias);
  }

  public interface TableSelector {
    TableAliasBuilder from(String table);
    TableAliasBuilder from(Query subquery);
  }

  public interface TableAliasBuilder extends NaturalJoinTypeBuilder {
    NaturalJoinTypeBuilder as(String alias);
  }

  public interface JoinTypeBuilder extends JoinBuilder {
    JoinBuilder left();
    JoinBuilder cross();
  }

  public interface NaturalJoinTypeBuilder extends JoinTypeBuilder, SelectionBuilder {
    JoinTypeBuilder natural();
  }

  public interface JoinBuilder {
    JoinAliasBuilder join(String table);
    JoinAliasBuilder join(Query subquery);
  }

  public interface JoinAliasBuilder extends JoinConstraintBuilder, SelectionBuilder {
    JoinConstraintBuilder as(String alias);
  }

  public interface JoinConstraintBuilder {
    NaturalJoinTypeBuilder using(String... columns);
    NaturalJoinTypeBuilder on(String expression);
  }

  public interface SelectionBuilder extends GroupByBuilder {
    GroupByBuilder where(String expression);
  }

  public interface GroupByBuilder extends OrderByBuilder {
    HavingBuilder groupBy(String... expressions);
  }

  public interface HavingBuilder extends OrderByBuilder {
    OrderByBuilder having(String expression);
  }

  public interface OrderByBuilder extends LimitBuilder, CompoundQueryBuilder {
    OrderingTermBuilder orderBy(String expression);
  }

  public interface OrderingTermBuilder extends OrderingDirectionSelector {
    OrderingDirectionSelector collate(String collation);
  }

  public interface OrderingDirectionSelector extends OrderByBuilder {
    OrderByBuilder asc();
    OrderByBuilder desc();
  }

  public interface LimitBuilder extends QueryBuilder {
    LimitOffsetBuilder limit(String expression);
    LimitOffsetBuilder limit(int limit);
  }

  public interface LimitOffsetBuilder extends QueryBuilder {
    QueryBuilder offset(String expression);
    QueryBuilder offset(int limit);
  }

  public interface QueryBuilder {
    Query build();
  }

  public interface CompoundQueryBuilder {
    UnionTypeSelector union();
    SelectCore intersect();
    SelectCore except();
  }

  public interface UnionTypeSelector extends SelectCore {
    SelectCore all();
  }
}
