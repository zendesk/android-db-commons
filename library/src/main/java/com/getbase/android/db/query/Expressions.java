package com.getbase.android.db.query;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.Arrays;

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
  }

  public interface BinaryOperator {
    ExpressionBuilder eq();
    ExpressionCombiner eq(Expression e);
    ExpressionBuilder is();
    ExpressionCombiner is(Expression e);
  }

  public interface ExpressionBuilder extends UnaryOperator, ExpressionCore {
  }

  public interface ExpressionCombiner extends BinaryOperator, Expression {
  }

  // mirror all method from UnaryOperator and ExpressionCore interfaces
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

  private static class Builder implements ExpressionBuilder, ExpressionCombiner {
    private StringBuilder mBuilder = new StringBuilder();

    private static final Joiner ARGS_JOINER = Joiner.on(", ");
    private static final Function<Expression, String> GET_EXPR_SQL = new Function<Expression, String>() {
      @Override
      public String apply(Expression e) {
        return e.toRawSql();
      }
    };

    private void expr(Expression... e) {
      mBuilder
          .append("(")
          .append(ARGS_JOINER.join(Iterables.transform(Arrays.asList(e), GET_EXPR_SQL)))
          .append(")");
    }

    @Override
    public ExpressionBuilder eq() {
      mBuilder.append(" == ");
      return this;
    }

    @Override
    public ExpressionCombiner eq(Expression e) {
      eq();
      expr(e);
      return this;
    }

    @Override
    public ExpressionBuilder is() {
      mBuilder.append(" IS ");
      return this;
    }

    @Override
    public ExpressionCombiner is(Expression e) {
      is();
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
  }
}
