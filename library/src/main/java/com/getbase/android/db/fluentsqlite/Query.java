package com.getbase.android.db.fluentsqlite;

import com.getbase.android.db.cursors.FluentCursor;
import com.getbase.android.db.fluentsqlite.Expressions.Expression;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public final class Query {
  private final QueryBuilderImpl mQueryBuilder;

  private Query(QueryBuilderImpl queryBuilder) {
    mQueryBuilder = queryBuilder;
  }

  public FluentCursor perform(SQLiteDatabase db) {
    return mQueryBuilder.perform(db);
  }

  public RawQuery toRawQuery() {
    return mQueryBuilder.toRawQuery();
  }

  public QueryBuilder buildUpon() {
    return mQueryBuilder.copy();
  }

  public Set<String> getTables() {
    return mQueryBuilder.getTables();
  }

  private static final Function<String, String> SURROUND_WITH_PARENS = new Function<String, String>() {
    @Override
    public String apply(String input) {
      return "(" + input + ")";
    }
  };

  public static QueryBuilder select() {
    return new QueryBuilderImpl();
  }

  public static CompoundQueryBuilder select(Query query) {
    return new CompoundQueryBuilderImpl(query);
  }

  public static CompoundQueryBuilder select(QueryBuilder queryBuilder) {
    return select(queryBuilder.build());
  }

  public static Query union(Query... queries) {
    Preconditions.checkNotNull(queries);
    Preconditions.checkArgument(queries.length > 0);

    CompoundQueryBuilder builder = select(queries[0]);
    for (int i = 1; i < queries.length; i++) {
      builder = builder.union(queries[i]);
    }

    return builder.build();
  }

  public static Query unionAll(Query... queries) {
    Preconditions.checkNotNull(queries);
    Preconditions.checkArgument(queries.length > 0);

    CompoundQueryBuilder builder = select(queries[0]);
    for (int i = 1; i < queries.length; i++) {
      builder = builder.unionAll(queries[i]);
    }

    return builder.build();
  }

  public static Query intersect(Query... queries) {
    Preconditions.checkNotNull(queries);
    Preconditions.checkArgument(queries.length > 0);

    CompoundQueryBuilder builder = select(queries[0]);
    for (int i = 1; i < queries.length; i++) {
      builder = builder.intersect(queries[i]);
    }

    return builder.build();
  }

  public interface CompoundQueryBuilder extends CompoundOrderByBuilder, CompoundLimitBuilder {
    CompoundQueryBuilder union(Query query);
    CompoundQueryBuilder union(QueryBuilder queryBuilder);

    CompoundQueryBuilder unionAll(Query query);
    CompoundQueryBuilder unionAll(QueryBuilder queryBuilder);

    CompoundQueryBuilder intersect(Query query);
    CompoundQueryBuilder intersect(QueryBuilder queryBuilder);

    CompoundQueryBuilder except(Query query);
    CompoundQueryBuilder except(QueryBuilder queryBuilder);

    Query build();
  }

  public interface CompoundOrderByBuilder {
    CompoundOrderingTermBuilder orderBy(String expression);
    CompoundOrderingTermBuilder orderBy(Expression expression);
  }

  public interface CompoundOrderingTermBuilder extends CompoundOrderingDirectionSelector {
    CompoundOrderingDirectionSelector collate(String collation);
  }

  public interface CompoundOrderingDirectionSelector extends CompoundQueryBuilder {
    CompoundQueryBuilder asc();
    CompoundQueryBuilder desc();
  }

  public interface CompoundLimitBuilder {
    CompoundLimitOffsetBuilder limit(String expression);
    CompoundLimitOffsetBuilder limit(int limit);
  }

  public interface CompoundLimitOffsetBuilder extends CompoundQueryBuilder {
    CompoundQueryBuilder offset(String expression);
    CompoundQueryBuilder offset(int limit);
  }

  private static class CompoundQueryBuilderImpl implements CompoundQueryBuilder, CompoundOrderingTermBuilder, CompoundLimitOffsetBuilder {
    private QueryBuilderImpl mQueryBuilder;

    private CompoundQueryBuilderImpl(Query query) {
      mQueryBuilder = query.mQueryBuilder.copy();
    }

    @Override
    public CompoundQueryBuilder offset(String expression) {
      mQueryBuilder.offset(expression);
      return this;
    }

    @Override
    public CompoundQueryBuilder offset(int limit) {
      mQueryBuilder.offset(limit);
      return this;
    }

    @Override
    public CompoundOrderingDirectionSelector collate(String collation) {
      mQueryBuilder.collate(collation);
      return this;
    }

    @Override
    public CompoundQueryBuilder asc() {
      mQueryBuilder.asc();
      return this;
    }

    @Override
    public CompoundQueryBuilder desc() {
      mQueryBuilder.desc();
      return this;
    }

    private CompoundQueryBuilder withCompoundQueryPart(Query queryPart, String operation) {
      mQueryBuilder.mCompoundQueryParts.put(mQueryBuilder.mCurrentQueryPart, operation);
      mQueryBuilder.mCurrentQueryPart = queryPart.mQueryBuilder.copy().mCurrentQueryPart;

      return this;
    }

    @Override
    public CompoundQueryBuilder union(Query query) {
      return withCompoundQueryPart(query, "UNION");
    }

    @Override
    public CompoundQueryBuilder union(QueryBuilder queryBuilder) {
      return union(queryBuilder.build());
    }

    @Override
    public CompoundQueryBuilder unionAll(Query query) {
      return withCompoundQueryPart(query, "UNION ALL");
    }

    @Override
    public CompoundQueryBuilder unionAll(QueryBuilder queryBuilder) {
      return unionAll(queryBuilder.build());
    }

    @Override
    public CompoundQueryBuilder intersect(Query query) {
      return withCompoundQueryPart(query, "INTERSECT");
    }

    @Override
    public CompoundQueryBuilder intersect(QueryBuilder queryBuilder) {
      return intersect(queryBuilder.build());
    }

    @Override
    public CompoundQueryBuilder except(Query query) {
      return withCompoundQueryPart(query, "EXCEPT");
    }

    @Override
    public CompoundQueryBuilder except(QueryBuilder queryBuilder) {
      return except(queryBuilder.build());
    }

    @Override
    public Query build() {
      return new Query(mQueryBuilder.copy());
    }

    @Override
    public CompoundLimitOffsetBuilder limit(String expression) {
      mQueryBuilder.limit(expression);
      return this;
    }

    @Override
    public CompoundLimitOffsetBuilder limit(int limit) {
      mQueryBuilder.limit(limit);
      return this;
    }

    @Override
    public CompoundOrderingTermBuilder orderBy(String expression) {
      mQueryBuilder.orderBy(expression);
      return this;
    }

    @Override
    public CompoundOrderingTermBuilder orderBy(Expression expression) {
      mQueryBuilder.orderBy(expression);
      return this;
    }
  }

  private static class QueryBuilderImpl implements QueryBuilder, ColumnAliasBuilder, LimitOffsetBuilder, OrderingTermBuilder, ColumnListTableSelector, ColumnsListAliasBuilder {

    @Override
    public Query build() {
      buildPendingOrderByClause();

      return new Query(copy());
    }

    private static class CompoundQueryPart {

      private List<String> mProjection = Lists.newArrayList();
      private String mColumnWithPotentialAlias;
      private List<String> mColumnsWithPotentialTable = Lists.newArrayList();
      private String mColumnsListsTableWithPotentialAlias;

      private List<String> mGroupByExpressions = Lists.newArrayList();
      private List<String> mHaving = Lists.newArrayList();
      private List<String> mSelection = Lists.newArrayList();

      private LinkedListMultimap<QueryPart, Object> mArgs = LinkedListMultimap.create();

      private TableOrSubquery mPendingTable;
      private LinkedHashMap<TableOrSubquery, String> mTables = Maps.newLinkedHashMap();

      private boolean mIsDistinct = false;

      private String mPendingJoinType = "";
      private JoinSpec mPendingJoin;
      private List<JoinSpec> mJoins = Lists.newArrayList();

      private Set<String> mTablesUsedInExpressions = Sets.newHashSet();

      CompoundQueryPart() {
      }

      CompoundQueryPart(CompoundQueryPart other) {
        mIsDistinct = other.mIsDistinct;
        mProjection.addAll(other.mProjection);
        mColumnWithPotentialAlias = other.mColumnWithPotentialAlias;
        mColumnsWithPotentialTable.addAll(other.mColumnsWithPotentialTable);
        mColumnsListsTableWithPotentialAlias = other.mColumnsListsTableWithPotentialAlias;

        mGroupByExpressions.addAll(other.mGroupByExpressions);
        mHaving.addAll(other.mHaving);

        mSelection.addAll(other.mSelection);

        mArgs.putAll(other.mArgs);

        mPendingTable = other.mPendingTable;

        mTables.putAll(other.mTables);

        mPendingJoinType = other.mPendingJoinType;
        mPendingJoin = other.mPendingJoin != null ? new JoinSpec(other.mPendingJoin) : null;

        mJoins = Lists.newArrayListWithCapacity(other.mJoins.size());
        for (JoinSpec join : other.mJoins) {
          mJoins.add(new JoinSpec(join));
        }

        mTablesUsedInExpressions.addAll(other.mTablesUsedInExpressions);
      }

      private void addPendingColumn() {
        if (mColumnWithPotentialAlias != null) {
          mProjection.add(mColumnWithPotentialAlias);
          mColumnWithPotentialAlias = null;
        }
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

      private void addPendingTable(String alias) {
        if (mPendingTable != null) {
          mTables.put(mPendingTable, alias);
          mPendingTable = null;
        }
      }

      private void addPendingJoin() {
        if (mPendingJoin != null) {
          mJoins.add(mPendingJoin);
          mPendingJoin = null;
        }
      }

      private void processPendingParts() {
        addPendingColumn();
        addPendingColumns();
        addPendingTable(null);
        addPendingJoin();
      }

      RawQuery toRawQuery() {
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

        args.addAll(Collections2.transform(mArgs.get(QueryPart.PROJECTION), Functions.toStringFunction()));

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
          args.addAll(Collections2.transform(mArgs.get(QueryPart.SELECTION), Functions.toStringFunction()));
        }

        if (!mGroupByExpressions.isEmpty()) {
          builder.append(" GROUP BY ");
          builder.append(Joiner.on(", ").join(mGroupByExpressions));

          args.addAll(Collections2.transform(mArgs.get(QueryPart.GROUP_BY), Functions.toStringFunction()));

          if (!mHaving.isEmpty()) {
            builder.append(" HAVING ");
            builder.append(Joiner.on(" AND ").join(Collections2.transform(mHaving, SURROUND_WITH_PARENS)));
            args.addAll(Collections2.transform(mArgs.get(QueryPart.HAVING), Functions.toStringFunction()));
          }
        }

        return new RawQuery(builder.toString(), args);
      }

      public void getTables(ImmutableSet.Builder<String> builder) {
        addTableOrSubquery(builder, mPendingTable);
        for (TableOrSubquery tableOrSubquery : mTables.keySet()) {
          addTableOrSubquery(builder, tableOrSubquery);
        }

        if (mPendingJoin != null) {
          addTableOrSubquery(builder, mPendingJoin.mJoinSource);
        }

        for (JoinSpec join : mJoins) {
          addTableOrSubquery(builder, join.mJoinSource);
        }

        builder.addAll(mTablesUsedInExpressions);
      }
    }

    private CompoundQueryPart mCurrentQueryPart = new CompoundQueryPart();

    private String mLimit;
    private String mOffset;

    private String mOrderByExpression;
    private String mOrderByCollation;
    private String mOrderByOrder;
    private List<String> mOrderClauses = Lists.newArrayList();
    private List<Object> mOrderByArgs = Lists.newArrayList();
    private Set<String> mTablesUsedInExpressions = Sets.newHashSet();

    private LinkedHashMap<CompoundQueryPart, String> mCompoundQueryParts = Maps.newLinkedHashMap();

    private QueryBuilderImpl() {
    }

    private QueryBuilderImpl(QueryBuilderImpl other) {
      mLimit = other.mLimit;
      mOffset = other.mOffset;

      mOrderByExpression = other.mOrderByExpression;
      mOrderByCollation = other.mOrderByCollation;
      mOrderByOrder = other.mOrderByOrder;
      mOrderClauses = Lists.newCopyOnWriteArrayList(other.mOrderClauses);
      mOrderByArgs = Lists.newCopyOnWriteArrayList(other.mOrderByArgs);
      mTablesUsedInExpressions = Sets.newHashSet(other.mTablesUsedInExpressions);

      mCurrentQueryPart = new CompoundQueryPart(other.mCurrentQueryPart);

      mCompoundQueryParts = Maps.newLinkedHashMap(other.mCompoundQueryParts);
    }

    QueryBuilderImpl copy() {
      return new QueryBuilderImpl(this);
    }

    @Override
    public Set<String> getTables() {
      Builder<String> builder = ImmutableSet.builder();

      mCurrentQueryPart.getTables(builder);
      for (CompoundQueryPart part : mCompoundQueryParts.keySet()) {
        part.getTables(builder);
      }
      builder.addAll(mTablesUsedInExpressions);

      return builder.build();
    }

    private static void addTableOrSubquery(ImmutableSet.Builder<String> builder, TableOrSubquery tableOrSubquery) {
      if (tableOrSubquery != null) {
        if (tableOrSubquery.mSubquery != null) {
          builder.addAll(tableOrSubquery.mSubquery.getTables());
        } else {
          builder.add(tableOrSubquery.mTable);
        }
      }
    }

    @Override
    public RawQuery toRawQuery() {
      buildPendingOrderByClause();

      List<String> args = Lists.newArrayList();
      StringBuilder builder = new StringBuilder();

      for (Entry<CompoundQueryPart, String> entry : mCompoundQueryParts.entrySet()) {
        RawQuery partRawQuery = entry.getKey().toRawQuery();
        builder.append(partRawQuery.mRawQuery);
        builder.append(" ");
        builder.append(entry.getValue());
        builder.append(" ");
        args.addAll(partRawQuery.mRawQueryArgs);
      }

      RawQuery lastQueryPart = mCurrentQueryPart.toRawQuery();
      args.addAll(lastQueryPart.mRawQueryArgs);
      builder.append(lastQueryPart.mRawQuery);

      if (!mOrderClauses.isEmpty()) {
        builder.append(" ORDER BY ");
        builder.append(Joiner.on(", ").join(mOrderClauses));
      }

      args.addAll(Collections2.transform(mOrderByArgs, Functions.toStringFunction()));

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
      mCurrentQueryPart.addPendingColumns();
      mCurrentQueryPart.addPendingColumn();
      mCurrentQueryPart.mColumnWithPotentialAlias = expression.getSql();
      mCurrentQueryPart.mTablesUsedInExpressions.addAll(expression.getTables());

      if (expression.getArgsCount() > 0) {
        mCurrentQueryPart.mArgs.putAll(QueryPart.PROJECTION, Arrays.asList(expression.getMergedArgs()));
      }

      return this;
    }

    @Override
    public QueryBuilder as(String alias) {
      Preconditions.checkState(mCurrentQueryPart.mColumnWithPotentialAlias != null);
      mCurrentQueryPart.mProjection.add(mCurrentQueryPart.mColumnWithPotentialAlias + " AS " + alias);
      mCurrentQueryPart.mColumnWithPotentialAlias = null;
      return this;
    }

    @Override
    public ColumnListTableSelector columns(String... columns) {
      mCurrentQueryPart.addPendingColumn();
      mCurrentQueryPart.addPendingColumns();
      if (columns != null) {
        Collections.addAll(mCurrentQueryPart.mColumnsWithPotentialTable, columns);
      }
      return this;
    }

    @Override
    public ColumnsTableSelector allColumns() {
      mCurrentQueryPart.addPendingColumn();
      mCurrentQueryPart.addPendingColumns();
      mCurrentQueryPart.mColumnsWithPotentialTable.add("*");
      return mColumnsTableSelectorHelper;
    }

    @Override
    public ColumnsListAliasBuilder of(String table) {
      mCurrentQueryPart.mColumnsListsTableWithPotentialAlias = table;
      return this;
    }

    @Override
    public QueryBuilder asColumnNames() {
      for (String column : mCurrentQueryPart.mColumnsWithPotentialTable) {
        mCurrentQueryPart.mProjection.add(mCurrentQueryPart.mColumnsListsTableWithPotentialAlias + "." + column + " AS " + column);
      }

      mCurrentQueryPart.mColumnsListsTableWithPotentialAlias = null;
      mCurrentQueryPart.mColumnsWithPotentialTable.clear();

      return this;
    }

    @Override
    public QueryBuilder distinct() {
      mCurrentQueryPart.mIsDistinct = true;
      return this;
    }

    @Override
    public QueryBuilder all() {
      mCurrentQueryPart.mIsDistinct = false;
      return this;
    }

    private static abstract class ColumnsTableSelectorHelper extends QueryBuilderProxy implements ColumnsTableSelector {
      private ColumnsTableSelectorHelper(QueryBuilder delegate) {
        super(delegate);
      }
    }

    private ColumnsTableSelectorHelper mColumnsTableSelectorHelper = new ColumnsTableSelectorHelper(this) {
      @Override
      public QueryBuilder of(String table) {
        for (String column : mCurrentQueryPart.mColumnsWithPotentialTable) {
          mCurrentQueryPart.mProjection.add(table + "." + column);
        }
        mCurrentQueryPart.mColumnsWithPotentialTable.clear();

        return QueryBuilderImpl.this;
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
      public QueryBuilder select() {
        mCompoundQueryParts.put(mCurrentQueryPart, mOperation);

        mCurrentQueryPart = new CompoundQueryPart();

        return QueryBuilderImpl.this;
      }
    };

    @Override
    public QueryBuilder groupBy(String expression) {
      mCurrentQueryPart.mGroupByExpressions.add(expression);
      return this;
    }

    @Override
    public QueryBuilder groupBy(Expression expression) {
      mCurrentQueryPart.mTablesUsedInExpressions.addAll(expression.getTables());
      if (expression.getArgsCount() > 0) {
        mCurrentQueryPart.mArgs.putAll(QueryPart.GROUP_BY, Arrays.asList(expression.getMergedArgs()));
      }

      return groupBy(expression.getSql());
    }

    @Override
    public QueryBuilder having(String having, Object... havingArgs) {
      mCurrentQueryPart.mHaving.add(having);
      if (havingArgs != null) {
        mCurrentQueryPart.mArgs.putAll(QueryPart.HAVING, Arrays.asList(havingArgs));
      }

      return this;
    }

    @Override
    public QueryBuilder having(Expression having, Object... havingArgs) {
      mCurrentQueryPart.mTablesUsedInExpressions.addAll(having.getTables());
      return having(having.getSql(), having.getMergedArgs(havingArgs));
    }

    @Override
    public JoinTypeBuilder natural() {
      mCurrentQueryPart.mPendingJoinType = "NATURAL ";
      return this;
    }

    @Override
    public JoinBuilder left() {
      mCurrentQueryPart.mPendingJoinType += "LEFT ";
      return this;
    }

    @Override
    public JoinBuilder cross() {
      mCurrentQueryPart.mPendingJoinType += "CROSS ";
      return this;
    }

    @Override
    public JoinAliasBuilder join(String table) {
      mCurrentQueryPart.addPendingJoin();

      mCurrentQueryPart.mPendingJoin = new JoinSpec(mCurrentQueryPart.mPendingJoinType, new TableOrSubquery(table));
      mCurrentQueryPart.mPendingJoinType = "";

      return mJoinHelper;
    }

    @Override
    public JoinAliasBuilder join(Query subquery) {
      mCurrentQueryPart.addPendingJoin();

      mCurrentQueryPart.mPendingJoin = new JoinSpec(mCurrentQueryPart.mPendingJoinType, new TableOrSubquery(subquery));
      mCurrentQueryPart.mPendingJoinType = "";

      return mJoinHelper;
    }

    @Override
    public JoinAliasBuilder join(QueryBuilder subqueryBuilder) {
      return join(subqueryBuilder.build());
    }

    private static abstract class JoinHelper extends QueryBuilderProxy implements JoinAliasBuilder {
      private JoinHelper(QueryBuilder delegate) {
        super(delegate);
      }
    }

    private JoinHelper mJoinHelper = new JoinHelper(this) {
      @Override
      public JoinConstraintBuilder as(String alias) {
        mCurrentQueryPart.mPendingJoin.mAlias = alias;
        return this;
      }

      @Override
      public QueryBuilder using(String... columns) {
        Preconditions.checkArgument(columns != null, "Column list in USING clause cannot be null");
        Preconditions.checkArgument(columns.length > 0, "Column list in USING clause cannot be empty");
        mCurrentQueryPart.mPendingJoin.mUsingColumns = columns;
        mCurrentQueryPart.addPendingJoin();
        return QueryBuilderImpl.this;
      }

      @Override
      public JoinOnConstraintBuilder on(String constraint, Object... constraintArgs) {
        mCurrentQueryPart.mPendingJoin.mConstraints.add(constraint);
        if (constraintArgs != null) {
          Collections.addAll(mCurrentQueryPart.mPendingJoin.mConstraintsArgs, constraintArgs);
        }
        return this;
      }

      @Override
      public JoinOnConstraintBuilder on(Expression constraint, Object... constraintArgs) {
        mCurrentQueryPart.mTablesUsedInExpressions.addAll(constraint.getTables());
        mCurrentQueryPart.mPendingJoin.mConstraints.add(constraint.getSql());
        Collections.addAll(mCurrentQueryPart.mPendingJoin.mConstraintsArgs, constraint.getMergedArgs(constraintArgs));

        return this;
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
        mUsingColumns = other.mUsingColumns != null ? Arrays.copyOf(other.mUsingColumns, other.mUsingColumns.length) : null;
        mConstraints.addAll(other.mConstraints);
        mConstraintsArgs.addAll(other.mConstraintsArgs);
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
      mTablesUsedInExpressions.addAll(expression.getTables());
      Collections.addAll(mOrderByArgs, expression.getMergedArgs());
      return orderBy(expression.getSql());
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
        mCurrentQueryPart.mSelection.add(selection);
        if (selectionArgs != null) {
          mCurrentQueryPart.mArgs.putAll(QueryPart.SELECTION, Arrays.asList(selectionArgs));
        }
      }

      return this;
    }

    @Override
    public QueryBuilder where(Expression selection, Object... selectionArgs) {
      if (selection != null) {
        mCurrentQueryPart.mTablesUsedInExpressions.addAll(selection.getTables());
        where(selection.getSql(), selection.getMergedArgs(selectionArgs));
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
        mCurrentQueryPart.addPendingTable(alias);
        return QueryBuilderImpl.this;
      }
    };

    @Override
    public TableAliasBuilder from(String table) {
      mCurrentQueryPart.addPendingTable(null);
      mCurrentQueryPart.mPendingTable = new TableOrSubquery(table);
      return mTableAliasBuilder;
    }

    @Override
    public TableAliasBuilder from(Query subquery) {
      mCurrentQueryPart.addPendingTable(null);
      mCurrentQueryPart.mPendingTable = new TableOrSubquery(subquery);
      return mTableAliasBuilder;
    }

    @Override
    public TableAliasBuilder from(QueryBuilder subqueryBuilder) {
      return from(subqueryBuilder.build());
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

  public interface QueryBuilder extends DistinctSelector, TableSelector, ColumnSelector, SelectionBuilder, NaturalJoinTypeBuilder, GroupByBuilder, HavingBuilder, OrderByBuilder, LimitBuilder, CompoundOperator {
    Query build();
    RawQuery toRawQuery();
    Set<String> getTables();
    FluentCursor perform(SQLiteDatabase db);
  }

  private static class QueryBuilderProxy implements QueryBuilder {

    private final QueryBuilder mDelegate;

    private QueryBuilderProxy(QueryBuilder delegate) {
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
    public JoinAliasBuilder join(QueryBuilder subqueryBuilder) {
      return mDelegate.join(subqueryBuilder);
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

    @Override
    public TableAliasBuilder from(QueryBuilder subqueryBuilder) {
      return mDelegate.from(subqueryBuilder);
    }

    @Override
    public Query build() {
      return mDelegate.build();
    }

    @Override
    public RawQuery toRawQuery() {
      return mDelegate.toRawQuery();
    }

    @Override
    public Set<String> getTables() {
      return mDelegate.getTables();
    }

    @Override
    public FluentCursor perform(SQLiteDatabase db) {
      return mDelegate.perform(db);
    }

    @Override
    public QueryBuilder distinct() {
      return mDelegate.distinct();
    }

    @Override
    public QueryBuilder all() {
      return mDelegate.all();
    }
  }

  public interface TableSelector {
    TableAliasBuilder from(String table);
    TableAliasBuilder from(Query subquery);
    TableAliasBuilder from(QueryBuilder subqueryBuilder);
  }

  public interface DistinctSelector {
    QueryBuilder distinct();
    QueryBuilder all();
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
    ColumnListTableSelector columns(String... columns);
    ColumnsTableSelector allColumns();
    ColumnAliasBuilder expr(Expression expression);
  }

  public interface ColumnsTableSelector extends QueryBuilder {
    QueryBuilder of(String table);
  }

  public interface ColumnListTableSelector extends QueryBuilder {
    ColumnsListAliasBuilder of(String table);
  }

  public interface ColumnsListAliasBuilder extends QueryBuilder {
    QueryBuilder asColumnNames();
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
    JoinAliasBuilder join(QueryBuilder subqueryBuilder);
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

  public interface CompoundOperator {
    UnionTypeSelector union();
    NextQueryPartStart intersect();
    NextQueryPartStart except();
  }

  public interface UnionTypeSelector extends NextQueryPartStart {
    NextQueryPartStart all();
  }

  public interface NextQueryPartStart {
    QueryBuilder select();
  }
}
