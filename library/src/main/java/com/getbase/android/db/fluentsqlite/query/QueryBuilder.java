package com.getbase.android.db.fluentsqlite.query;

import com.getbase.android.db.cursors.FluentCursor;
import com.getbase.android.db.fluentsqlite.Expressions;
import com.getbase.android.db.fluentsqlite.Expressions.Expression;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public final class QueryBuilder {
  private QueryBuilder() {
  }

  private static final Function<String, String> SURROUND_WITH_PARENS = new Function<String, String>() {
    @Override
    public String apply(String input) {
      return "(" + input + ")";
    }
  };

  public static Query select() {
    return new QueryImpl();
  }

  private static class QueryImpl implements Query, ColumnAliasBuilder, LimitOffsetBuilder, OrderingTermBuilder, ColumnListTableSelector, ColumnsListAliasBuilder {
    private List<String> mProjection = Lists.newArrayList();
    private String mColumnWithPotentialAlias;
    private List<String> mColumnsWithPotentialTable = Lists.newArrayList();
    private String mColumnsListsTableWithPotentialAlias;

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

    private boolean mIsDistinct = false;

    private String mPendingJoinType = "";
    private JoinSpec mPendingJoin;
    private List<JoinSpec> mJoins = Lists.newArrayList();

    private LinkedHashMap<RawQuery, String> mCompoundQueryParts = Maps.newLinkedHashMap();

    private QueryImpl() {
    }

    private QueryImpl(QueryImpl other) {
      mIsDistinct = other.mIsDistinct;
      mProjection = Lists.newCopyOnWriteArrayList(other.mProjection);
      mColumnWithPotentialAlias = other.mColumnWithPotentialAlias;
      mColumnsWithPotentialTable = Lists.newCopyOnWriteArrayList(other.mColumnsWithPotentialTable);
      mColumnsListsTableWithPotentialAlias = other.mColumnsListsTableWithPotentialAlias;

      mGroupByExpressions = Lists.newCopyOnWriteArrayList(other.mGroupByExpressions);
      mHaving = Lists.newCopyOnWriteArrayList(other.mHaving);
      mHavingArgs = Lists.newCopyOnWriteArrayList(other.mHavingArgs);

      mLimit = other.mLimit;
      mOffset = other.mOffset;

      mOrderByExpression = other.mOrderByExpression;
      mOrderByCollation = other.mOrderByCollation;
      mOrderByOrder = other.mOrderByOrder;
      mOrderClauses = Lists.newCopyOnWriteArrayList(other.mOrderClauses);

      mSelection = Lists.newCopyOnWriteArrayList(other.mSelection);
      mSelectionArgs = Lists.newCopyOnWriteArrayList(other.mSelectionArgs);

      mPendingTable = other.mPendingTable;

      mTables = Maps.newLinkedHashMap(other.mTables);

      mPendingJoinType = other.mPendingJoinType;
      mPendingJoin = other.mPendingJoin != null ? new JoinSpec(other.mPendingJoin) : null;

      mJoins = Lists.newArrayListWithCapacity(other.mJoins.size());
      for (JoinSpec join : other.mJoins) {
        mJoins.add(new JoinSpec(join));
      }

      mCompoundQueryParts = Maps.newLinkedHashMap(other.mCompoundQueryParts);
    }

    @Override
    public Query copy() {
      return new QueryImpl(this);
    }

    private void resetCoreSelectParts() {
      mIsDistinct = false;

      mProjection = Lists.newArrayList();
      mColumnWithPotentialAlias = null;

      mGroupByExpressions = Lists.newArrayList();
      mHaving = Lists.newArrayList();
      mHavingArgs = Lists.newArrayList();

      mSelection = Lists.newArrayList();
      mSelectionArgs = Lists.newArrayList();

      mPendingTable = null;
      mTables = Maps.newLinkedHashMap();

      mPendingJoinType = "";
      mPendingJoin = null;
      mJoins = Lists.newArrayList();
    }

    @Override
    public RawQuery toRawQuery() {
      processPendingParts();

      List<String> args = Lists.newArrayList();
      StringBuilder builder = new StringBuilder();

      for (Entry<RawQuery, String> entry : mCompoundQueryParts.entrySet()) {
        builder.append(entry.getKey().mRawQuery);
        builder.append(" ");
        builder.append(entry.getValue());
        builder.append(" ");
        args.addAll(entry.getKey().mRawQueryArgs);
      }

      RawQuery lastQueryPart = buildCompoundQueryPart();
      args.addAll(lastQueryPart.mRawQueryArgs);
      builder.append(lastQueryPart.mRawQuery);

      if (!mOrderClauses.isEmpty()) {
        builder.append(" ORDER BY ");
        builder.append(Joiner.on(", ").join(mOrderClauses));
      }

      if (mLimit != null) {
        builder.append(" LIMIT ");
        builder.append(mLimit);
        if (mOffset != null) {
          builder.append(" OFFSET ");
          builder.append(mOffset);
        }
      }

      return new RawQuery(builder.toString(), args);
    }

    @Override
    public FluentCursor perform(SQLiteDatabase db) {
      RawQuery rawQuery = toRawQuery();
      return new FluentCursor(db.rawQuery(rawQuery.mRawQuery, rawQuery.mRawQueryArgs.toArray(new String[rawQuery.mRawQueryArgs.size()])));
    }

    private RawQuery buildCompoundQueryPart() {
      processPendingParts();

      Preconditions.checkState(!(!mHaving.isEmpty() && mGroupByExpressions.isEmpty()), "a GROUP BY clause is required when using HAVING clause");

      List<String> args = Lists.newArrayList();
      StringBuilder builder = new StringBuilder();

      builder.append("SELECT ");
      if (mIsDistinct) {
        builder.append("DISTINCT ");
      }
      if (!mProjection.isEmpty()) {
        builder.append(Joiner.on(", ").join(mProjection));
      } else {
        builder.append("*");
      }

      if (!mTables.isEmpty()) {
        builder.append(" FROM ");

        List<String> tables = Lists.newArrayList();
        for (Entry<TableOrSubquery, String> tableEntry : mTables.entrySet()) {
          TableOrSubquery tableOrSubquery = tableEntry.getKey();
          String alias = tableEntry.getValue();

          String tableString;
          if (tableOrSubquery.mTable != null) {
            tableString = tableOrSubquery.mTable;
          } else {
            RawQuery rawSubquery = tableOrSubquery.mSubquery.toRawQuery();

            tableString = SURROUND_WITH_PARENS.apply(rawSubquery.mRawQuery);
            args.addAll(rawSubquery.mRawQueryArgs);
          }

          if (alias != null) {
            tableString += " AS " + alias;
          }

          tables.add(tableString);
        }

        builder.append(Joiner.on(", ").join(tables));
      }

      for (JoinSpec join : mJoins) {
        builder.append(" ");
        builder.append(join.mJoinType);
        builder.append("JOIN ");

        builder.append(join.mJoinSource.mTable != null
            ? join.mJoinSource.mTable
            : SURROUND_WITH_PARENS.apply(join.mJoinSource.mSubquery.toRawQuery().mRawQuery)
        );

        if (join.mAlias != null) {
          builder.append(" AS ");
          builder.append(join.mAlias);
        }

        if (join.mUsingColumns != null) {
          builder.append(" USING ");
          builder.append("(");
          builder.append(Joiner.on(", ").join(join.mUsingColumns));
          builder.append(")");
        } else if (!join.mConstraints.isEmpty()) {
          builder.append(" ON ");
          builder.append(Joiner.on(" AND ").join(Collections2.transform(join.mConstraints, SURROUND_WITH_PARENS)));
          args.addAll(Collections2.transform(join.mConstraintsArgs, Functions.toStringFunction()));
        }
      }

      if (!mSelection.isEmpty()) {
        builder.append(" WHERE ");
        builder.append(Joiner.on(" AND ").join(Collections2.transform(mSelection, SURROUND_WITH_PARENS)));
        args.addAll(Collections2.transform(mSelectionArgs, Functions.toStringFunction()));
      }

      if (!mGroupByExpressions.isEmpty()) {
        builder.append(" GROUP BY ");
        builder.append(Joiner.on(", ").join(mGroupByExpressions));

        if (!mHaving.isEmpty()) {
          builder.append(" HAVING ");
          builder.append(Joiner.on(" AND ").join(Collections2.transform(mHaving, SURROUND_WITH_PARENS)));
          args.addAll(Collections2.transform(mHavingArgs, Functions.toStringFunction()));
        }
      }

      return new RawQuery(builder.toString(), args);
    }

    private void processPendingParts() {
      addPendingColumn();
      addPendingColumns();
      buildPendingOrderByClause();
      addPendingTable(null);
      addPendingJoin();
    }

    @Override
    public ColumnAliasBuilder column(String column) {
      return expr(Expressions.column(column));
    }

    @Override
    public ColumnAliasBuilder column(String table, String column) {
      return expr(Expressions.column(table, column));
    }

    @Override
    public ColumnAliasBuilder literal(Number number) {
      return expr(Expressions.literal(number));
    }

    @Override
    public ColumnAliasBuilder literal(Object object) {
      return expr(Expressions.literal(object));
    }

    @Override
    public ColumnAliasBuilder nul() {
      return expr(Expressions.nul());
    }

    @Override
    public ColumnAliasBuilder expr(Expression expression) {
      addPendingColumn();
      mColumnWithPotentialAlias = expression.toRawSql();
      return this;
    }

    @Override
    public Query as(String alias) {
      Preconditions.checkState(mColumnWithPotentialAlias != null);
      mProjection.add(mColumnWithPotentialAlias + " AS " + alias);
      mColumnWithPotentialAlias = null;
      return this;
    }

    private void addPendingColumn() {
      if (mColumnWithPotentialAlias != null) {
        mProjection.add(mColumnWithPotentialAlias);
        mColumnWithPotentialAlias = null;
      }
    }

    @Override
    public ColumnListTableSelector columns(String... columns) {
      addPendingColumns();
      if (columns != null) {
        Collections.addAll(mColumnsWithPotentialTable, columns);
      }
      return this;
    }

    @Override
    public ColumnsTableSelector allColumns() {
      addPendingColumns();
      mColumnsWithPotentialTable.add("*");
      return mColumnsTableSelectorHelper;
    }

    private void addPendingColumns() {
      if (mColumnsListsTableWithPotentialAlias != null) {
        for (String column : mColumnsWithPotentialTable) {
          mProjection.add(mColumnsListsTableWithPotentialAlias + "." + column);
        }
      } else {
        mProjection.addAll(mColumnsWithPotentialTable);
      }

      mColumnsListsTableWithPotentialAlias = null;
      mColumnsWithPotentialTable.clear();
    }

    @Override
    public ColumnsListAliasBuilder of(String table) {
      mColumnsListsTableWithPotentialAlias = table;
      return this;
    }

    @Override
    public Query asColumnNames() {
      for (String column : mColumnsWithPotentialTable) {
        mProjection.add(mColumnsListsTableWithPotentialAlias + "." + column + " AS " + column);
      }

      mColumnsListsTableWithPotentialAlias = null;
      mColumnsWithPotentialTable.clear();

      return this;
    }

    @Override
    public Query distinct() {
      mIsDistinct = true;
      return this;
    }

    @Override
    public Query all() {
      mIsDistinct = false;
      return this;
    }

    private static abstract class ColumnsTableSelectorHelper extends QueryBuilderProxy implements ColumnsTableSelector {
      private ColumnsTableSelectorHelper(Query delegate) {
        super(delegate);
      }
    }

    private ColumnsTableSelectorHelper mColumnsTableSelectorHelper = new ColumnsTableSelectorHelper(this) {
      @Override
      public Query of(String table) {
        for (String column : mColumnsWithPotentialTable) {
          mProjection.add(table + "." + column);
        }
        mColumnsWithPotentialTable.clear();

        return QueryImpl.this;
      }
    };

    @Override
    public UnionTypeSelector union() {
      return mCompoundQueryHelper.withOperation("UNION");
    }

    @Override
    public NextQueryPartStart intersect() {
      return mCompoundQueryHelper.withOperation("INTERSECT");
    }

    @Override
    public NextQueryPartStart except() {
      return mCompoundQueryHelper.withOperation("EXCEPT");
    }

    private abstract static class CompoundQueryHelper implements UnionTypeSelector {
      protected String mOperation;

      public CompoundQueryHelper withOperation(String operation) {
        mOperation = operation;
        return this;
      }

      @Override
      public NextQueryPartStart all() {
        return withOperation("UNION ALL");
      }
    }

    private CompoundQueryHelper mCompoundQueryHelper = new CompoundQueryHelper() {
      @Override
      public Query select() {
        mCompoundQueryParts.put(buildCompoundQueryPart(), mOperation);

        resetCoreSelectParts();

        return QueryImpl.this;
      }
    };

    @Override
    public Query groupBy(String expression) {
      mGroupByExpressions.add(expression);
      return this;
    }

    @Override
    public Query groupBy(Expression expression) {
      return groupBy(expression.toRawSql());
    }

    @Override
    public Query having(String having, Object... havingArgs) {
      mHaving.add(having);
      Collections.addAll(mHavingArgs, havingArgs);

      return this;
    }

    @Override
    public Query having(Expression having, Object... havingArgs) {
      return having(having.toRawSql(), havingArgs);
    }

    @Override
    public JoinTypeBuilder natural() {
      mPendingJoinType = "NATURAL ";
      return this;
    }

    @Override
    public JoinBuilder left() {
      mPendingJoinType += "LEFT ";
      return this;
    }

    @Override
    public JoinBuilder cross() {
      mPendingJoinType += "CROSS ";
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
      mPendingJoinType = "";

      return mJoinHelper;
    }

    @Override
    public JoinAliasBuilder join(Query subquery) {
      addPendingJoin();

      mPendingJoin = new JoinSpec(mPendingJoinType, new TableOrSubquery(subquery));
      mPendingJoinType = "";

      return mJoinHelper;
    }

    private static abstract class JoinHelper extends QueryBuilderProxy implements JoinAliasBuilder {
      private JoinHelper(Query delegate) {
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
      public Query using(String... columns) {
        mPendingJoin.mUsingColumns = columns;
        addPendingJoin();
        return QueryImpl.this;
      }

      @Override
      public JoinOnConstraintBuilder on(String constraint, Object... constraintArgs) {
        mPendingJoin.mConstraints.add(constraint);
        Collections.addAll(mPendingJoin.mConstraintsArgs, constraintArgs);
        return this;
      }

      @Override
      public JoinOnConstraintBuilder on(Expression constraint, Object... constraintArgs) {
        return on(constraint.toRawSql(), constraintArgs);
      }
    };

    private static class JoinSpec {
      final String mJoinType;
      final TableOrSubquery mJoinSource;

      String mAlias;

      String[] mUsingColumns;

      List<String> mConstraints = Lists.newArrayList();
      List<Object> mConstraintsArgs = Lists.newArrayList();

      private JoinSpec(String joinType, TableOrSubquery joinSource) {
        mJoinType = joinType;
        mJoinSource = joinSource;
      }

      private JoinSpec(JoinSpec other) {
        mJoinType = other.mJoinType;
        mJoinSource = other.mJoinSource;
        mAlias = other.mAlias;
        mUsingColumns = mUsingColumns != null ? Arrays.copyOf(other.mUsingColumns, other.mUsingColumns.length) : null;
        mConstraints = Lists.newCopyOnWriteArrayList(other.mConstraints);
        mConstraintsArgs = Lists.newCopyOnWriteArrayList(other.mConstraintsArgs);
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
    public Query offset(String expression) {
      Preconditions.checkState(mLimit != null);
      Preconditions.checkState(mOffset == null);
      mOffset = expression;
      return this;
    }

    @Override
    public Query offset(int limit) {
      return offset(String.valueOf(limit));
    }

    @Override
    public OrderingTermBuilder orderBy(String expression) {
      buildPendingOrderByClause();
      mOrderByExpression = expression;
      return this;
    }

    @Override
    public OrderingTermBuilder orderBy(Expression expression) {
      return orderBy(expression.toRawSql());
    }

    @Override
    public OrderingDirectionSelector collate(String collation) {
      mOrderByCollation = collation;
      return this;
    }

    @Override
    public Query asc() {
      mOrderByOrder = " ASC";
      return this;
    }

    @Override
    public Query desc() {
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
    public Query where(String selection, Object... selectionArgs) {
      if (!Strings.isNullOrEmpty(selection)) {
        mSelection.add(selection);
        Collections.addAll(mSelectionArgs, selectionArgs);
      }

      return this;
    }

    @Override
    public Query where(Expression selection, Object... selectionArgs) {
      if (selection != null) {
        where(selection.toRawSql(), selectionArgs);
      }
      return this;
    }

    private static abstract class TableAliasBuilderImpl extends QueryBuilderProxy implements TableAliasBuilder {
      private TableAliasBuilderImpl(Query delegate) {
        super(delegate);
      }
    }

    private TableAliasBuilderImpl mTableAliasBuilder = new TableAliasBuilderImpl(this) {
      @Override
      public Query as(String alias) {
        addPendingTable(alias);
        return QueryImpl.this;
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

  public interface Query extends DistinctSelector, TableSelector, ColumnSelector, SelectionBuilder, NaturalJoinTypeBuilder, GroupByBuilder, HavingBuilder, OrderByBuilder, LimitBuilder, CompoundQueryBuilder {
    FluentCursor perform(SQLiteDatabase db);
    RawQuery toRawQuery();
    Query copy();
  }

  private static class QueryBuilderProxy implements Query {

    private final Query mDelegate;

    private QueryBuilderProxy(Query delegate) {
      mDelegate = delegate;
    }

    @Override
    public ColumnAliasBuilder column(String column) {
      return mDelegate.column(column);
    }

    @Override
    public ColumnAliasBuilder column(String table, String column) {
      return mDelegate.column(table, column);
    }

    @Override
    public ColumnAliasBuilder literal(Number number) {
      return mDelegate.literal(number);
    }

    @Override
    public ColumnAliasBuilder literal(Object object) {
      return mDelegate.literal(object);
    }

    @Override
    public ColumnAliasBuilder nul() {
      return mDelegate.nul();
    }

    @Override
    public ColumnListTableSelector columns(String... columns) {
      return mDelegate.columns(columns);
    }

    @Override
    public ColumnsTableSelector allColumns() {
      return mDelegate.allColumns();
    }

    @Override
    public ColumnAliasBuilder expr(Expression expression) {
      return mDelegate.expr(expression);
    }

    @Override
    public UnionTypeSelector union() {
      return mDelegate.union();
    }

    @Override
    public NextQueryPartStart intersect() {
      return mDelegate.intersect();
    }

    @Override
    public NextQueryPartStart except() {
      return mDelegate.except();
    }

    @Override
    public Query groupBy(String expression) {
      return mDelegate.groupBy(expression);
    }

    @Override
    public Query groupBy(Expression expression) {
      return mDelegate.groupBy(expression);
    }

    @Override
    public Query having(String having, Object... havingArgs) {
      return mDelegate.having(having, havingArgs);
    }

    @Override
    public Query having(Expression having, Object... havingArgs) {
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
    public OrderingTermBuilder orderBy(Expression expression) {
      return mDelegate.orderBy(expression);
    }

    @Override
    public Query where(String selection, Object... selectionArgs) {
      return mDelegate.where(selection, selectionArgs);
    }

    @Override
    public Query where(Expression selection, Object... selectionArgs) {
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

    @Override
    public FluentCursor perform(SQLiteDatabase db) {
      return mDelegate.perform(db);
    }

    @Override
    public RawQuery toRawQuery() {
      return mDelegate.toRawQuery();
    }

    @Override
    public Query copy() {
      return mDelegate.copy();
    }

    @Override
    public Query distinct() {
      return mDelegate.distinct();
    }

    @Override
    public Query all() {
      return mDelegate.all();
    }
  }

  public interface TableSelector {
    TableAliasBuilder from(String table);
    TableAliasBuilder from(Query subquery);
  }

  public interface DistinctSelector {
    Query distinct();
    Query all();
  }

  public interface TableAliasBuilder extends Query {
    Query as(String alias);
  }

  public interface ColumnSelector {
    ColumnAliasBuilder column(String column);
    ColumnAliasBuilder column(String table, String column);
    ColumnAliasBuilder literal(Number number);
    ColumnAliasBuilder literal(Object object);
    ColumnAliasBuilder nul();
    ColumnListTableSelector columns(String... columns);
    ColumnsTableSelector allColumns();
    ColumnAliasBuilder expr(Expression expression);
  }

  public interface ColumnsTableSelector extends Query {
    Query of(String table);
  }

  public interface ColumnListTableSelector extends Query {
    ColumnsListAliasBuilder of(String table);
  }

  public interface ColumnsListAliasBuilder extends Query {
    Query asColumnNames();
  }

  public interface ColumnAliasBuilder extends Query {
    Query as(String alias);
  }

  public interface SelectionBuilder {
    Query where(String selection, Object... selectionArgs);
    Query where(Expression selection, Object... selectionArgs);
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
    Query using(String... columns);
  }

  public interface JoinOnConstraintBuilder extends Query {
    JoinOnConstraintBuilder on(String constraint, Object... constraintArgs);
    JoinOnConstraintBuilder on(Expression constraint, Object... constraintArgs);
  }

  public interface GroupByBuilder {
    Query groupBy(String expression);
    Query groupBy(Expression expression);
  }

  public interface HavingBuilder {
    Query having(String having, Object... havingArgs);
    Query having(Expression having, Object... havingArgs);
  }

  public interface OrderByBuilder {
    OrderingTermBuilder orderBy(String expression);
    OrderingTermBuilder orderBy(Expression expression);
  }

  public interface OrderingTermBuilder extends OrderingDirectionSelector {
    OrderingDirectionSelector collate(String collation);
  }

  public interface OrderingDirectionSelector extends Query {
    Query asc();
    Query desc();
  }

  public interface LimitBuilder {
    LimitOffsetBuilder limit(String expression);
    LimitOffsetBuilder limit(int limit);
  }

  public interface LimitOffsetBuilder extends Query {
    Query offset(String expression);
    Query offset(int limit);
  }

  public interface CompoundQueryBuilder {
    UnionTypeSelector union();
    NextQueryPartStart intersect();
    NextQueryPartStart except();
  }

  public interface UnionTypeSelector extends NextQueryPartStart {
    NextQueryPartStart all();
  }

  public interface NextQueryPartStart {
    Query select();
  }
}
