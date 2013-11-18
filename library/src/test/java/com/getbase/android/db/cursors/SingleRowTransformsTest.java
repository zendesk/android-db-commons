package com.getbase.android.db.cursors;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.common.base.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.MatrixCursor;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SingleRowTransformsTest {

  @Mock
  private Cursor cursorMock;

  private static String COLUMN = "column";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(cursorMock.getColumnIndexOrThrow(COLUMN)).thenReturn(1);
  }

  @Test
  public void shouldCacheGetColumnIndexResult() throws Exception {
    Cursor c = mock(Cursor.class);

    Function<Cursor, Integer> transform = SingleRowTransforms.getColumn(COLUMN).asInteger();
    transform.apply(c);
    transform.apply(c);

    verify(c, times(1)).getColumnIndexOrThrow(anyString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectCursorWithoutSpecifiedColumn() throws Exception {
    MatrixCursor cursor = new MatrixCursor(new String[] { COLUMN });
    cursor.addRow(new Object[] { 42 });
    cursor.moveToFirst();

    SingleRowTransforms.getColumn("lol").asInteger().apply(cursor);
  }

  @Test
  public void shouldGetShortForExistingColumn() throws Exception {
    short expected = 42;

    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getShort(1)).thenReturn(expected);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asShort().apply(cursorMock)).isEqualTo(expected);
  }

  @Test
  public void shouldGetNullForExistingShortColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(true);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asShort().apply(cursorMock)).isNull();
  }

  @Test
  public void shouldGetIntForExistingColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getInt(1)).thenReturn(42);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asInteger().apply(cursorMock)).isEqualTo(42);
  }

  @Test
  public void shouldGetNullForExistingIntColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(true);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asInteger().apply(cursorMock)).isNull();
  }

  @Test
  public void shouldGetLongForExistingColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getLong(1)).thenReturn(42L);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asLong().apply(cursorMock)).isEqualTo(42L);
  }

  @Test
  public void shouldGetNullForExistingLongColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(true);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asLong().apply(cursorMock)).isNull();
  }

  @Test
  public void shouldGetFloatForExistingColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getFloat(1)).thenReturn(42f);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asFloat().apply(cursorMock)).isEqualTo(42f);
  }

  @Test
  public void shouldGetNullForExistingFloatColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(true);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asFloat().apply(cursorMock)).isNull();
  }

  @Test
  public void shouldGetDoubleForExistingColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getDouble(1)).thenReturn(42d);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asDouble().apply(cursorMock)).isEqualTo(42d);
  }

  @Test
  public void shouldGetNullForExistingDoubleColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(true);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asDouble().apply(cursorMock)).isNull();
  }

  @Test
  public void shouldGetTrueBooleanForExistingColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getInt(1)).thenReturn(1);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asBoolean().apply(cursorMock)).isTrue();
  }

  @Test
  public void shouldGetFalseBooleanForExistingColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getInt(1)).thenReturn(0);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asBoolean().apply(cursorMock)).isFalse();
  }

  @Test
  public void shouldGetNullForExistingBooleanColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(true);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asBoolean().apply(cursorMock)).isNull();
  }

  @Test
  public void shouldGetStringForExistingColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(false);
    when(cursorMock.getString(1)).thenReturn("wat?");

    assertThat(SingleRowTransforms.getColumn(COLUMN).asString().apply(cursorMock)).isEqualTo("wat?");
  }

  @Test
  public void shouldGetNullForExistingStringColumn() throws Exception {
    when(cursorMock.isNull(1)).thenReturn(true);

    assertThat(SingleRowTransforms.getColumn(COLUMN).asString().apply(cursorMock)).isNull();
  }
}
