package com.getbase.android.db.fluentsqlite;

import com.getbase.android.db.fluentsqlite.query.QueryBuilder.Query;
import com.getbase.android.db.fluentsqlite.query.RawQuery;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.Iterator;

public final class Expressions {
  private Expressions() {
  }

  public interface UnaryOperator {
    ExpressionCore not();
  }

  public interface Expression {
    String toRawSql();
  }

  public interface ExpressionCore {
    // basic stuff
    ExpressionCombiner column(String col);
    ExpressionCombiner column(String table, String col);
    ExpressionCombiner arg();
    ExpressionCombiner nul();
    ExpressionCombiner literal(Number number);
    ExpressionCombiner literal(Object object);

    // aggregate functions
    ExpressionCombiner sum(Expression e);
    ExpressionCombiner count(Expression e);
    ExpressionCombiner count();
    ExpressionCombiner max(Expression e);
    ExpressionCombiner min(Expression e);

    // coalescing functions
    ExpressionCombiner ifNull(Expression left, Expression right);
    ExpressionCombiner nullIf(Expression left, Expression right);
    ExpressionCombiner coalesce(Expression... expressions);

    // strings operations
    ExpressionCombiner length(Expression e);
    ExpressionCombiner concat(Expression... e);
    ExpressionCombiner join(String on, Expression... e);
  }

  public interface CaseExpressions {
    CaseCondition cases();
    CaseCondition cases(Expression e);
  }

  public interface CaseCondition {
    CaseValue when(Expression e);
  }

  public interface CaseValue {
    CaseExpressionBuilder then(Expression e);
  }

  public interface CaseExpressionBuilder extends CaseCondition, CaseExpressionEndStep {
    ExpressionCombiner otherwise(Expression e);
  }

  public interface CaseExpressionEndStep {
    ExpressionCombiner end();
  }

  public interface BinaryOperator {
    ExpressionBuilder eq();
    ExpressionCombiner eq(Expression e);
    ExpressionBuilder ne();
    ExpressionCombiner ne(Expression e);
    ExpressionBuilder gt();
    ExpressionCombiner gt(Expression e);
    ExpressionBuilder ge();
    ExpressionCombiner ge(Expression e);
    ExpressionBuilder lt();
    ExpressionCombiner lt(Expression e);
    ExpressionBuilder le();
    ExpressionCombiner le(Expression e);

    ExpressionBuilder is();
    ExpressionCombiner is(Expression e);

    ExpressionCombiner in(Query subquery);
    ExpressionCombiner in(Expression... e);

    ExpressionBuilder or();
    ExpressionCombiner or(Expression e);
    ExpressionBuilder and();
    ExpressionCombiner and(Expression e);
  }

  public interface ExpressionBuilder extends UnaryOperator, ExpressionCore, CaseExpressions {
  }

  public interface ExpressionCombiner extends BinaryOperator, Expression {
  }

  // mirror all method from ExpressionBuilder interface
  public static ExpressionCore not() {
    return new Builder().not();
  }

  public static ExpressionCombiner column(String col) {
    return new Builder().column(col);
  }

  public static ExpressionCombiner column(String table, String col) {
    return new Builder().column(table, col);
  }

  public static ExpressionCombiner arg() {
    return new Builder().arg();
  }

  public static ExpressionCombiner nul() {
    return new Builder().nul();
  }

  public static ExpressionCombiner literal(Number number) {
    return new Builder().literal(number);
  }

  public static ExpressionCombiner literal(Object object) {
    return new Builder().literal(object);
  }

  public static ExpressionCombiner sum(Expression e) {
    return new Builder().sum(e);
  }

  public static ExpressionCombiner count(Expression e) {
    return new Builder().count(e);
  }

  public static ExpressionCombiner count() {
    return new Builder().count();
  }

  public static ExpressionCombiner max(Expression e) {
    return new Builder().max(e);
  }

  public static ExpressionCombiner min(Expression e) {
    return new Builder().min(e);
  }

  public static ExpressionCombiner ifNull(Expression left, Expression right) {
    return new Builder().ifNull(left, right);
  }

  public static ExpressionCombiner nullIf(Expression left, Expression right) {
    return new Builder().nullIf(left, right);
  }

  public static ExpressionCombiner coalesce(Expression... expressions) {
    return new Builder().coalesce(expressions);
  }

  public static ExpressionCombiner length(Expression e) {
    return new Builder().length(e);
  }

  public static ExpressionCombiner concat(Expression... e) {
    return new Builder().concat(e);
  }

  public static ExpressionCombiner join(String on, Expression... e) {
    return new Builder().join(on, e);
  }

  public static CaseCondition cases() {
    return new Builder().cases();
  }

  public static CaseCondition cases(Expression e) {
    return new Builder().cases(e);
  }

  private static class Builder implements ExpressionBuilder, ExpressionCombiner, CaseExpressionBuilder, CaseValue {
    private StringBuilder mBuilder = new StringBuilder();

    private static final Joiner ARGS_JOINER = Joiner.on(", ");
    private static final Joiner CONCAT_JOINER = Joiner.on(" || ");
    private static final Function<Expression, String> GET_EXPR_SQL = new Function<Expression, String>() {
      @Override
      public String apply(Expression e) {
        return e.toRawSql();
      }
    };

    private void expr(Expression... e) {
      mBuilder
          .append("(")
          .append(ARGS_JOINER.join(getSQLs(e)))
          .append(")");
    }

    private ExpressionBuilder binaryOperator(String operator) {
      mBuilder.append(" ");
      mBuilder.append(operator);
      mBuilder.append(" ");
      return this;
    }

    @Override
    public ExpressionBuilder eq() {
      return binaryOperator("==");
    }

    @Override
    public ExpressionCombiner eq(Expression e) {
      eq();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder ne() {
      return binaryOperator("!=");
    }

    @Override
    public ExpressionCombiner ne(Expression e) {
      ne();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder gt() {
      return binaryOperator(">");
    }

    @Override
    public ExpressionCombiner gt(Expression e) {
      gt();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder ge() {
      return binaryOperator(">=");
    }

    @Override
    public ExpressionCombiner ge(Expression e) {
      ge();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder lt() {
      return binaryOperator("<");
    }

    @Override
    public ExpressionCombiner lt(Expression e) {
      lt();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder le() {
      return binaryOperator("<=");
    }

    @Override
    public ExpressionCombiner le(Expression e) {
      le();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder is() {
      return binaryOperator("IS");
    }

    @Override
    public ExpressionCombiner is(Expression e) {
      is();
      expr(e);
      return this;
    }

    @Override
    public ExpressionCombiner in(Query subquery) {
      RawQuery rawQuery = subquery.toRawQuery();
      Preconditions.checkArgument(rawQuery.mRawQueryArgs.isEmpty(), "Queries with bound args in expressions are not supported yet");

      binaryOperator("IN");

      mBuilder
          .append("(")
          .append(rawQuery.mRawQuery)
          .append(")");

      return this;
    }

    @Override
    public ExpressionCombiner in(Expression... e) {
      binaryOperator("IN");
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder or() {
      return binaryOperator("OR");
    }

    @Override
    public ExpressionCombiner or(Expression e) {
      or();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder and() {
      return binaryOperator("AND");
    }

    @Override
    public ExpressionCombiner and(Expression e) {
      and();
      expr(e);
      return this;
    }

    @Override
    public String toRawSql() {
      return mBuilder.toString().trim();
    }

    @Override
    public ExpressionCombiner column(String col) {
      mBuilder.append(col);
      return this;
    }

    @Override
    public ExpressionCombiner column(String table, String col) {
      mBuilder.append(table);
      mBuilder.append(".");
      mBuilder.append(col);
      return this;
    }

    @Override
    public ExpressionCombiner arg() {
      mBuilder.append("?");
      return this;
    }

    @Override
    public ExpressionCombiner nul() {
      mBuilder.append("NULL");
      return this;
    }

    @Override
    public ExpressionCombiner literal(Number number) {
      mBuilder.append(number.toString());
      return this;
    }

    @Override
    public ExpressionCombiner literal(Object object) {
      mBuilder
          .append('\'')
          .append(object.toString().replaceAll("'", "''"))
          .append('\'');
      return this;
    }

    @Override
    public ExpressionCombiner sum(Expression e) {
      return function("SUM", e);
    }

    @Override
    public ExpressionCombiner count(Expression e) {
      return function("COUNT", e);
    }

    @Override
    public ExpressionCombiner count() {
      mBuilder.append("COUNT(*)");
      return this;
    }

    @Override
    public ExpressionCombiner max(Expression e) {
      return function("MAX", e);
    }

    @Override
    public ExpressionCombiner min(Expression e) {
      return function("MIN", e);
    }

    @Override
    public ExpressionCombiner ifNull(Expression left, Expression right) {
      return function("ifnull", left, right);
    }

    @Override
    public ExpressionCombiner nullIf(Expression left, Expression right) {
      return function("nullif", left, right);
    }

    @Override
    public ExpressionCombiner coalesce(Expression... expressions) {
      Preconditions.checkArgument(expressions.length >= 2);
      return function("coalesce", expressions);
    }

    @Override
    public ExpressionCombiner length(Expression e) {
      return function("length", e);
    }

    @Override
    public ExpressionCombiner concat(Expression... e) {
      mBuilder.append(CONCAT_JOINER.join(getSQLs(e)));
      return this;
    }

    private Iterable<String> getSQLs(Expression[] e) {
      return Iterables.transform(Arrays.asList(e), GET_EXPR_SQL);
    }

    private static <T> Iterable<T> intersperse(final T element, final Iterable<T> iterable) {
      return new Iterable<T>() {
        @Override
        public Iterator<T> iterator() {
          final Iterator<T> iterator = iterable.iterator();
          return new AbstractIterator<T>() {
            boolean intersperse = false;

            @Override
            protected T computeNext() {
              if (iterator.hasNext()) {
                final T result;
                if (intersperse) {
                  result = element;
                } else {
                  result = iterator.next();
                }
                intersperse = !intersperse;
                return result;
              }
              return endOfData();
            }
          };
        }
      };
    }

    @Override
    public ExpressionCombiner join(String on, Expression... e) {
      return concat(FluentIterable
          .from(
              intersperse(
                  Expressions.literal(on),
                  Arrays.asList(e)
              )
          )
          .toArray(Expression.class));
    }

    private ExpressionCombiner function(String func, Expression... e) {
      mBuilder.append(func);
      expr(e);
      return this;
    }

    @Override
    public ExpressionCore not() {
      mBuilder.append("NOT ");
      return this;
    }

    @Override
    public ExpressionCombiner otherwise(Expression e) {
      mBuilder.append(" ELSE ");
      expr(e);
      return end();
    }

    @Override
    public CaseValue when(Expression e) {
      mBuilder.append(" WHEN ");
      expr(e);
      return this;
    }

    @Override
    public ExpressionCombiner end() {
      mBuilder.append(" END");
      return this;
    }

    @Override
    public CaseCondition cases() {
      mBuilder.append("CASE");
      return this;
    }

    @Override
    public CaseCondition cases(Expression e) {
      mBuilder.append("CASE ");
      expr(e);
      return this;
    }

    @Override
    public CaseExpressionBuilder then(Expression e) {
      mBuilder.append(" THEN ");
      expr(e);
      return this;
    }
  }
}
