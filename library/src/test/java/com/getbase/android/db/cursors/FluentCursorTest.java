package com.getbase.android.db.cursors;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.MatrixCursor;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class FluentCursorTest {

  private static final String TEST_COLUMN = "test_column";

  @Test
  public void shouldCloseCursorAfterItIsTransformed() throws Exception {
    final MatrixCursor cursor = new MatrixCursor(new String[] { TEST_COLUMN });
    final FluentCursor fluentCursor = new FluentCursor(cursor);
    fluentCursor.toFluentIterable(new Function<Cursor, Object>() {
      @Override
      public Object apply(Cursor cursor) {
        return null;
      }
    });
    assertThat(fluentCursor.isClosed()).isTrue();
  }

  @Test
  public void shouldApplyGivenFunctionOnEverySingleRow() throws Exception {
    final MatrixCursor cursor = buildMatrixCursor();
    final FluentCursor fluentCursor = new FluentCursor(cursor);
    final FluentIterable<Long> transformed = fluentCursor.toFluentIterable(new Function<Cursor, Long>() {
      @Override
      public Long apply(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(TEST_COLUMN));
      }
    });
    assertThat(transformed).hasSize(cursor.getCount());
    assertThat(transformed.allMatch(new Predicate<Long>() {
      @Override
      public boolean apply(Long aLong) {
        return aLong.equals(18L);
      }
    })).isTrue();
  }

  @Test
  public void shouldRecognizeNullAsAnEmptyCursor() throws Exception {
    final FluentCursor cursor = new FluentCursor(null);
    assertThat(cursor.getCount()).isZero();
  }

  @Test
  public void shouldBeAbleToCloseQuietlyFluentCursorWrappingNull() throws Exception {
    final FluentCursor cursor = new FluentCursor(null);
    cursor.close();
  }

  @Test
  public void shouldBeAbleToGetColumnIndexFromFluentCursorWrappingNull() throws Exception {
    final FluentCursor cursor = new FluentCursor(null);
    cursor.getColumnIndexOrThrow(TEST_COLUMN);
    cursor.getColumnIndex(TEST_COLUMN);
  }

  private MatrixCursor buildMatrixCursor() {
    final MatrixCursor cursor = new MatrixCursor(new String[] { TEST_COLUMN });
    for (int i = 0; i < 10; i++) {
      cursor.addRow(new Object[] { 18L });
    }
    return cursor;
  }
}
