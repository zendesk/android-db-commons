package com.getbase.android.db.query;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

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
    return new QueryBuilderImpl(false);
  }

  public static QueryBuilder selectDistinct() {
    return new QueryBuilderImpl(true);
  }

  final String mRawQuery;
  final String[] mRawQueryArgs;

  private Query(String rawQuery, String[] rawQueryArgs) {
    mRawQuery = rawQuery;
    mRawQueryArgs = rawQueryArgs;
  }

  public Cursor perform(SQLiteDatabase db) {
    return db.rawQuery(mRawQuery, mRawQueryArgs);
  }

  private static class QueryBuilderImpl implements QueryBuilder, ColumnAliasBuilder, LimitOffsetBuilder, OrderingTermBuilder {
    private List<String> mProjection = Lists.newArrayList();
    private String mColumnWithPotentialAlias;

    private List<String> mGroupByExpressions = Lists.newArrayList();
    private List<String> mHaving = Lists.newArrayList();
    private List<Object> mHavingArgs = Lists.newArrayList();

    private String mLimit;
    private String mOffset;

    private String mOrderByExpression;
    private String mOrderByCollation;
    private String mOrderByOrder;
    private List<String> mOrderClauses = Lists.newArrayList();

    private List<String> mSelection = Lists.newArrayList();
    private List<Object> mSelectionArgs = Lists.newArrayList();

    private TableOrSubquery mPendingTable;
    private LinkedHashMap<TableOrSubquery, String> mTables = Maps.newLinkedHashMap();

    private final boolean mIsDistinct;
    private final QueryBuilderImpl mPreviousCompoundQueryPart;
    private final String mCompoundQueryOperation;

    private String mPendingJoinType = "";
    private JoinSpec mPendingJoin;
    private List<JoinSpec> mJoins = Lists.newArrayList();

    private QueryBuilderImpl(boolean isDistinct) {
      mIsDistinct = isDistinct;
      mPreviousCompoundQueryPart = null;
      mCompoundQueryOperation = null;
    }

    private QueryBuilderImpl(boolean isDistinct, QueryBuilderImpl previousCompoundQueryPart, String compoundQueryOperation) {
      mIsDistinct = isDistinct;
      mPreviousCompoundQueryPart = previousCompoundQueryPart;
      mCompoundQueryOperation = compoundQueryOperation;
    }

    @Override
    public Query build() {
      addPendingColumn();
      buildPendingOrderByClause();
      addPendingTable(null);
      addPendingJoin();

      Preconditions.checkState(!(!mHaving.isEmpty() && mGroupByExpressions.isEmpty()), "a GROUP BY clause is required when using HAVING clause");

      return null; // TODO
    }

    @Override
    public ColumnAliasBuilder column(String column) {
      addPendingColumn();
      mColumnWithPotentialAlias = column;
      return this;
    }

    @Override
    public QueryBuilder as(String alias) {
      Preconditions.checkState(mColumnWithPotentialAlias != null);
      mProjection.add(mColumnWithPotentialAlias + " AS " + alias);
      mColumnWithPotentialAlias = null;
      return this;
    }

    private void addPendingColumn() {
      if (mColumnWithPotentialAlias != null) {
        mProjection.add(mColumnWithPotentialAlias);
      }
    }

    @Override
    public QueryBuilder columns(String... columns) {
      Collections.addAll(mProjection, columns);
      return this;
    }

    @Override
    public QueryBuilder allColumns() {
      mProjection.add("*");
      return this;
    }

    @Override
    public QueryBuilder allColumnsOf(String table) {
      mProjection.add(table + ".*");
      return this;
    }

    @Override
    public UnionTypeSelector union() {
      return mCompoundQueryHelper.withOperation("UNION");
    }

    @Override
    public SelectTypeSelector intersect() {
      return mCompoundQueryHelper.withOperation("INTERSECT");
    }

    @Override
    public SelectTypeSelector except() {
      return mCompoundQueryHelper.withOperation("EXCEPT");
    }

    private abstract static class CompoundQueryHelper implements UnionTypeSelector {
      protected String mOperation;

      public CompoundQueryHelper withOperation(String operation) {
        mOperation = operation;
        return this;
      }

      @Override
      public SelectTypeSelector all() {
        return withOperation("UNION ALL");
      }
    }

    private CompoundQueryHelper mCompoundQueryHelper = new CompoundQueryHelper() {
      @Override
      public QueryBuilder select() {
        return select(false);
      }

      @Override
      public QueryBuilder selectDistinct() {
        return select(true);
      }

      private QueryBuilder select(boolean distinct) {
        return new QueryBuilderImpl(distinct, QueryBuilderImpl.this, mOperation);
      }
    };

    @Override
    public QueryBuilder groupBy(String expression) {
      mGroupByExpressions.add(expression);
      return this;
    }

    @Override
    public QueryBuilder having(String having, Object... havingArgs) {
      mHaving.add(having);
      Collections.addAll(mHavingArgs, havingArgs);

      return this;
    }

    @Override
    public JoinTypeBuilder natural() {
      mPendingJoinType = "NATURAL ";
      return this;
    }

    @Override
    public JoinBuilder left() {
      mPendingJoinType += "LEFT";
      return this;
    }

    @Override
    public JoinBuilder cross() {
      mPendingJoinType += "CROSS";
      return this;
    }

    private void addPendingJoin() {
      if (mPendingJoin != null) {
        mJoins.add(mPendingJoin);
        mPendingJoin = null;
      }
    }

    @Override
    public JoinAliasBuilder join(String table) {
      addPendingJoin();

      mPendingJoin = new JoinSpec(mPendingJoinType, new TableOrSubquery(table));
      mPendingJoinType = null;

      return mJoinHelper;
    }

    @Override
    public JoinAliasBuilder join(Query subquery) {
      addPendingJoin();

      mPendingJoin = new JoinSpec(mPendingJoinType, new TableOrSubquery(subquery));
      mPendingJoinType = null;

      return mJoinHelper;
    }

    private static abstract class JoinHelper extends QueryBuilderProxy implements JoinAliasBuilder {
      private JoinHelper(QueryBuilder delegate) {
        super(delegate);
      }
    }

    private JoinHelper mJoinHelper = new JoinHelper(this) {
      @Override
      public JoinConstraintBuilder as(String alias) {
        mPendingJoin.mAlias = alias;
        return this;
      }

      @Override
      public QueryBuilder using(String... columns) {
        mPendingJoin.mUsingColumns = columns;
        addPendingJoin();
        return QueryBuilderImpl.this;
      }

      @Override
      public JoinOnConstraintBuilder on(String constraint, Object... constraintArgs) {
        mPendingJoin.mConstraints.add(constraint);
        mPendingJoin.mConstraintsArgs.add(constraintArgs);
        return this;
      }
    };

    private static class JoinSpec {
      final String mJoinType;
      final TableOrSubquery mTable;

      String mAlias;

      String[] mUsingColumns;

      List<String> mConstraints = Lists.newArrayList();
      List<Object> mConstraintsArgs = Lists.newArrayList();

      private JoinSpec(String joinType, TableOrSubquery table) {
        mJoinType = joinType;
        mTable = table;
      }
    }

    @Override
    public LimitOffsetBuilder limit(String expression) {
      Preconditions.checkState(mLimit == null, "LIMIT can be set only once");
      mLimit = expression;
      return this;
    }

    @Override
    public LimitOffsetBuilder limit(int limit) {
      return limit(String.valueOf(limit));
    }

    @Override
    public QueryBuilder offset(String expression) {
      Preconditions.checkState(mOffset == null);
      mOffset = expression;
      return this;
    }

    @Override
    public QueryBuilder offset(int limit) {
      return offset(String.valueOf(limit));
    }

    @Override
    public OrderingTermBuilder orderBy(String expression) {
      buildPendingOrderByClause();
      mOrderByExpression = expression;
      return this;
    }

    @Override
    public OrderingDirectionSelector collate(String collation) {
      mOrderByCollation = collation;
      return this;
    }

    @Override
    public QueryBuilder asc() {
      mOrderByOrder = " ASC";
      return this;
    }

    @Override
    public QueryBuilder desc() {
      mOrderByOrder = " DESC";
      return this;
    }

    private void buildPendingOrderByClause() {
      if (mOrderByExpression != null) {
        String orderByClause = mOrderByExpression;
        if (mOrderByCollation != null) {
          orderByClause += " COLLATE " + mOrderByCollation;
        }
        if (mOrderByOrder != null) {
          orderByClause += mOrderByOrder;
        }

        mOrderByExpression = null;
        mOrderByCollation = null;
        mOrderByOrder = null;

        mOrderClauses.add(orderByClause);
      }
    }

    @Override
    public QueryBuilder where(String selection, Object... selectionArgs) {
      mSelection.add(selection);
      Collections.addAll(mSelectionArgs, selectionArgs);

      return this;
    }

    private static abstract class TableAliasBuilderImpl extends QueryBuilderProxy implements TableAliasBuilder {
      private TableAliasBuilderImpl(QueryBuilder delegate) {
        super(delegate);
      }
    }

    private TableAliasBuilderImpl mTableAliasBuilder = new TableAliasBuilderImpl(this) {
      @Override
      public QueryBuilder as(String alias) {
        addPendingTable(alias);
        return QueryBuilderImpl.this;
      }
    };

    @Override
    public TableAliasBuilder from(String table) {
      addPendingTable(null);
      mPendingTable = new TableOrSubquery(table);
      return mTableAliasBuilder;
    }

    @Override
    public TableAliasBuilder from(Query subquery) {
      addPendingTable(null);
      mPendingTable = new TableOrSubquery(subquery);
      return mTableAliasBuilder;
    }

    private void addPendingTable(String alias) {
      if (mPendingTable != null) {
        mTables.put(mPendingTable, alias);
        mPendingTable = null;
      }
    }

    private static class TableOrSubquery {
      final String mTable;
      final Query mSubquery;

      private TableOrSubquery(String table) {
        mTable = table;
        mSubquery = null;
      }

      private TableOrSubquery(Query subquery) {
        mTable = null;
        mSubquery = subquery;
      }
    }
  }

  public interface QueryBuilder extends TableSelector, ColumnSelector, SelectionBuilder, NaturalJoinTypeBuilder, GroupByBuilder, HavingBuilder, OrderByBuilder, LimitBuilder, CompoundQueryBuilder {
    Query build();
  }

  private static class QueryBuilderProxy implements QueryBuilder {

    private final QueryBuilder mDelegate;

    private QueryBuilderProxy(QueryBuilder delegate) {
      mDelegate = delegate;
    }

    @Override
    public Query build() {
      return mDelegate.build();
    }

    @Override
    public ColumnAliasBuilder column(String column) {
      return mDelegate.column(column);
    }

    @Override
    public QueryBuilder columns(String... columns) {
      return mDelegate.columns(columns);
    }

    @Override
    public QueryBuilder allColumns() {
      return mDelegate.allColumns();
    }

    @Override
    public QueryBuilder allColumnsOf(String table) {
      return mDelegate.allColumnsOf(table);
    }

    @Override
    public UnionTypeSelector union() {
      return mDelegate.union();
    }

    @Override
    public SelectTypeSelector intersect() {
      return mDelegate.intersect();
    }

    @Override
    public SelectTypeSelector except() {
      return mDelegate.except();
    }

    @Override
    public QueryBuilder groupBy(String expression) {
      return mDelegate.groupBy(expression);
    }

    @Override
    public QueryBuilder having(String having, Object... havingArgs) {
      return mDelegate.having(having, havingArgs);
    }

    @Override
    public JoinBuilder left() {
      return mDelegate.left();
    }

    @Override
    public JoinBuilder cross() {
      return mDelegate.cross();
    }

    @Override
    public JoinAliasBuilder join(String table) {
      return mDelegate.join(table);
    }

    @Override
    public JoinAliasBuilder join(Query subquery) {
      return mDelegate.join(subquery);
    }

    @Override
    public LimitOffsetBuilder limit(String expression) {
      return mDelegate.limit(expression);
    }

    @Override
    public LimitOffsetBuilder limit(int limit) {
      return mDelegate.limit(limit);
    }

    @Override
    public JoinTypeBuilder natural() {
      return mDelegate.natural();
    }

    @Override
    public OrderingTermBuilder orderBy(String expression) {
      return mDelegate.orderBy(expression);
    }

    @Override
    public QueryBuilder where(String selection, Object... selectionArgs) {
      return mDelegate.where(selection, selectionArgs);
    }

    @Override
    public TableAliasBuilder from(String table) {
      return mDelegate.from(table);
    }

    @Override
    public TableAliasBuilder from(Query subquery) {
      return mDelegate.from(subquery);
    }
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
