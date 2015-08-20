package com.getbase.android.db.provider;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.sqlite.SQLiteStatement;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UtilsTest {

  @Mock
  private SQLiteStatement mStatement;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldBindStringArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, "test");
    verify(mStatement).bindString(eq(1), eq("test"));
  }

  @Test
  public void shouldBindByteArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, (byte) 42);
    verify(mStatement).bindLong(eq(1), eq(42L));
  }

  @Test
  public void shouldBindShortArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, (short) 42);
    verify(mStatement).bindLong(eq(1), eq(42L));
  }

  @Test
  public void shouldBindIntegerArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, 42);
    verify(mStatement).bindLong(eq(1), eq(42L));
  }

  @Test
  public void shouldBindLongArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, 42L);
    verify(mStatement).bindLong(eq(1), eq(42L));
  }

  @Test
  public void shouldBindFloatArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, 0.5f);
    verify(mStatement).bindDouble(eq(1), eq(0.5d));
  }

  @Test
  public void shouldBindDoubleArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, 0.5d);
    verify(mStatement).bindDouble(eq(1), eq(0.5d));
  }

  @Test
  public void shouldBindTrueArgAsLongEqual1() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, true);
    verify(mStatement).bindLong(eq(1), eq(1L));
  }

  @Test
  public void shouldBindFalseArgAsLongEqual0() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, false);
    verify(mStatement).bindLong(eq(1), eq(0L));
  }

  @Test
  public void shouldBindNullArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, null);
    verify(mStatement).bindNull(eq(1));
  }

  @Test
  public void shouldBindBlobArgs() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, new byte[42]);
    verify(mStatement).bindBlob(eq(1), eq(new byte[42]));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailToBindArgsOfOtherType() throws Exception {
    Utils.bindContentValueArg(mStatement, 1, new Object());
  }

  @Test
  public void shouldReturnNull() throws Exception {
    assertThat(Utils.escapeSqlArg(null)).isNull();
  }

  @Test
  public void shouldReturnSqlBoolean() throws Exception {
    assertThat(Utils.escapeSqlArg(true)).isEqualTo(1);
  }

  @Test
  public void shouldReturnNumber() throws Exception {
    assertThat(Utils.escapeSqlArg(1L)).isEqualTo(1L);
  }

  @Test
  public void shouldReturnEscapedString() throws Exception {
    assertThat(Utils.escapeSqlArg("test")).isEqualTo("'test'");
  }
}
