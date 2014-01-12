package com.getbase.android.db.query;

import com.getbase.android.db.cursors.FluentCursor;
import com.getbase.android.db.query.Expressions.Expression;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import android.database.sqlite.SQLiteDatabase;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class Query {

  private static final Function<String, String> SURROUND_WITH_PARENS = new Function<String, String>() {
    @Override
    public String apply(String input) {
      return "(" + input + ")";
    }
  };

  public static QueryBuilder select() {
    return new QueryBuilderImpl(false);
  }

  public static QueryBuilder selectDistinct() {
    return new QueryBuilderImpl(true);
  }

  final String mRawQuery;
  final List<String> mRawQueryArgs;

  private Query(String rawQuery, List<String> rawQueryArgs) {
    mRawQuery = rawQuery;
    mRawQueryArgs = rawQueryArgs;
  }

  public FluentCursor perform(SQLiteDatabase db) {
    return new FluentCursor(db.rawQuery(mRawQuery, mRawQueryArgs.toArray(new String[mRawQueryArgs.size()])));
  }

  private static class QueryBuilderImpl implements QueryBuilder, ColumnAliasBuilder, LimitOffsetBuilder, OrderingTermBuilder, ColumnsTableSelector {
    private List<String> mProjection = Lists.newArrayList();
    private String mColumnWithPotentialAlias;
    private List<String> mColumnsWithPotentialTable = Lists.newArrayList();

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

    private boolean mIsDistinct;

    private String mPendingJoinType = "";
    private JoinSpec mPendingJoin;
    private List<JoinSpec> mJoins = Lists.newArrayList();

    private LinkedHashMap<Query, String> mCompoundQueryParts = Maps.newLinkedHashMap();

    private QueryBuilderImpl(boolean isDistinct) {
      mIsDistinct = isDistinct;
    }

    private void resetCoreSelectParts(boolean distinct) {
      mIsDistinct = distinct;

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
    public Query build() {
      processPendingParts();

      List<String> args = Lists.newArrayList();
      StringBuilder builder = new StringBuilder();

      for (Entry<Query, String> entry : mCompoundQueryParts.entrySet()) {
        builder.append(entry.getKey().mRawQuery);
        builder.append(" ");
        builder.append(entry.getValue());
        builder.append(" ");
        args.addAll(entry.getKey().mRawQueryArgs);
      }

      Query lastQueryPart = buildCompoundQueryPart();
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

      return new Query(builder.toString(), args);
    }

    private Query buildCompoundQueryPart() {
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

        builder.append(Joiner.on(", ").join(Collections2.transform(mTables.entrySet(), new Function<Entry<TableOrSubquery, String>, String>() {
          @Override
          public String apply(Entry<TableOrSubquery, String> entry) {
            TableOrSubquery tableOrSubquery = entry.getKey();
            String alias = entry.getValue();

            String result = tableOrSubquery.mTable != null
                ? tableOrSubquery.mTable
                : SURROUND_WITH_PARENS.apply(tableOrSubquery.mSubquery.mRawQuery);

            if (alias != null) {
              result += " AS " + alias;
            }

            return result;
          }
        })));

        args.addAll(FluentIterable.from(mTables.keySet())
            .filter(new Predicate<TableOrSubquery>() {
              @Override
              public boolean apply(TableOrSubquery tableOrSubquery) {
                return tableOrSubquery.mSubquery != null;
              }
            })
            .transformAndConcat(new Function<TableOrSubquery, Iterable<String>>() {
              @Override
              public Iterable<String> apply(TableOrSubquery input) {
                return input.mSubquery.mRawQueryArgs;
              }
            })
            .toImmutableList()
        );
      }

      for (JoinSpec join : mJoins) {
        builder.append(" ");
        builder.append(join.mJoinType);
        builder.append("JOIN ");

        builder.append(join.mJoinSource.mTable != null
            ? join.mJoinSource.mTable
            : SURROUND_WITH_PARENS.apply(join.mJoinSource.mSubquery.mRawQuery)
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

      return new Query(builder.toString(), args);
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
    public QueryBuilder as(String alias) {
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
    public ColumnsTableSelector columns(String... columns) {
      addPendingColumns();
      if (columns != null) {
        Collections.addAll(mColumnsWithPotentialTable, columns);
        return this;
      } else {
        return allColumns();
      }
    }

    @Override
    public ColumnsTableSelector allColumns() {
      addPendingColumns();
      mColumnsWithPotentialTable.add("*");
      return this;
    }

    private void addPendingColumns() {
      mProjection.addAll(mColumnsWithPotentialTable);
      mColumnsWithPotentialTable.clear();
    }

    @Override
    public QueryBuilder of(String table) {
      for (String column : mColumnsWithPotentialTable) {
        mProjection.add(table + "." + column);
      }
      mColumnsWithPotentialTable.clear();

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
        mCompoundQueryParts.put(buildCompoundQueryPart(), mOperation);

        resetCoreSelectParts(distinct);

        return QueryBuilderImpl.this;
      }
    };

    @Override
    public QueryBuilder groupBy(String expression) {
      mGroupByExpressions.add(expression);
      return this;
    }

    @Override
    public QueryBuilder groupBy(Expression expression) {
      return groupBy(expression.toRawSql());
    }

    @Override
    public QueryBuilder having(String having, Object... havingArgs) {
      mHaving.add(having);
      Collections.addAll(mHavingArgs, havingArgs);

      return this;
    }

    @Override
    public QueryBuilder having(Expression having, Object... havingArgs) {
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
      Preconditions.checkState(mLimit != null);
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
    public OrderingTermBuilder orderBy(Expression expression) {
      return orderBy(expression.toRawSql());
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
      if (!Strings.isNullOrEmpty(selection)) {
        mSelection.add(selection);
        Collections.addAll(mSelectionArgs, selectionArgs);
      }

      return this;
    }

    @Override
    public QueryBuilder where(Expression selection, Object... selectionArgs) {
      if (selection != null) {
        where(selection.toRawSql(), selectionArgs);
      }
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
    public ColumnsTableSelector columns(String... columns) {
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
    public QueryBuilder groupBy(Expression expression) {
      return mDelegate.groupBy(expression);
    }

    @Override
    public QueryBuilder having(String having, Object... havingArgs) {
      return mDelegate.having(having, havingArgs);
    }

    @Override
    public QueryBuilder having(Expression having, Object... havingArgs) {
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
    public QueryBuilder where(String selection, Object... selectionArgs) {
      return mDelegate.where(selection, selectionArgs);
    }

    @Override
    public QueryBuilder where(Expression selection, Object... selectionArgs) {
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
    ColumnAliasBuilder column(String table, String column);
    ColumnAliasBuilder literal(Number number);
    ColumnAliasBuilder literal(Object object);
    ColumnAliasBuilder nul();
    ColumnsTableSelector columns(String... columns);
    ColumnsTableSelector allColumns();
    ColumnAliasBuilder expr(Expression expression);
  }

  public interface ColumnsTableSelector extends QueryBuilder {
    QueryBuilder of(String table);
  }

  public interface ColumnAliasBuilder extends QueryBuilder {
    QueryBuilder as(String alias);
  }

  public interface SelectionBuilder {
    QueryBuilder where(String selection, Object... selectionArgs);
    QueryBuilder where(Expression selection, Object... selectionArgs);
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
    JoinOnConstraintBuilder on(Expression constraint, Object... constraintArgs);
  }

  public interface GroupByBuilder {
    QueryBuilder groupBy(String expression);
    QueryBuilder groupBy(Expression expression);
  }

  public interface HavingBuilder {
    QueryBuilder having(String having, Object... havingArgs);
    QueryBuilder having(Expression having, Object... havingArgs);
  }

  public interface OrderByBuilder {
    OrderingTermBuilder orderBy(String expression);
    OrderingTermBuilder orderBy(Expression expression);
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
