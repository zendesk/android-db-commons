package com.getbase.android.db.query;

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
    ExpressionCombiner column(String col);
  }

  public interface BinaryOperator {
    ExpressionBuilder eq();
    ExpressionCombiner eq(Expression e);
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

  private static class Builder implements ExpressionBuilder, ExpressionCombiner {
    private StringBuilder mBuilder = new StringBuilder();

    private void expr(Expression e) {
      mBuilder
          .append("(")
          .append(e.toRawSql())
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
    public String toRawSql() {
      return mBuilder.toString().trim();
    }

    @Override
    public ExpressionCombiner column(String col) {
      mBuilder.append(col);
      return this;
    }

    @Override
    public ExpressionCore not() {
      mBuilder.append("NOT ");
      return this;
    }
  }
}
