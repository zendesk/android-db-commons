package com.getbase.android.db.cursors;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.MatrixCursor;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorsTest {

  @Test
  public void shouldSurviveNullPassedToFluentIterable() throws Exception {
    final Cursor cursor = null;
    final FluentIterable<Object> objects = Cursors.toFluentIterable(cursor, new Function<Cursor, Object>() {
      @Override
      public Object apply(Cursor cursor) {
        return null;
      }
    });
    assertThat(objects).isEmpty();
  }

  @Test
  public void shouldCloseCursorProperly() throws Exception {
    final MatrixCursor cursor = new MatrixCursor(new String[] { "column1" });
    Cursors.closeQuietly(cursor);
    assertThat(cursor.isClosed()).isTrue();
  }

  @Test
  public void shouldSurviveNullPassedToCloseQuietly() throws Exception {
    Cursors.closeQuietly(null);
  }

  @Test
  public void shouldNotTryToCloseAlreadyClosedCursor() throws Exception {
    Cursor cursor = mock(Cursor.class);
    when(cursor.isClosed()).thenReturn(true);
    Cursors.closeQuietly(cursor);
    verify(cursor, never()).close();
  }
}
