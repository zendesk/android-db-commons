package com.github.partition.android.database.tests.provider;

import com.github.partition.android.database.provider.ProviderAction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ResolvatorTests {

  private static final Uri TEST_URI = Uri.parse("content://authority/people");

  @Mock
  private ContentResolver contentResolverMock;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldPassNullsEverywhere() throws Exception {
    ProviderAction.query(TEST_URI)
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq((String) null), eq((String[]) null), eq((String) null));
  }

  @Test
  public void shouldUseProjectionWhenQuery() throws Exception {
    ProviderAction.query(TEST_URI)
        .projection("COL1")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq(new String[] { "COL1" }), eq((String) null), eq((String[]) null), eq((String) null));
  }

  @Test
  public void shouldAppendProjection() throws Exception {
    ProviderAction.query(TEST_URI)
        .projection("COL1")
        .projection("COL2")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq(new String[] { "COL1", "COL2" }), eq((String) null), eq((String[]) null), eq((String) null));
  }

  @Test
  public void shouldConcatenateSelectionProperlyWhenQuerying() throws Exception {
    ProviderAction.query(TEST_URI)
        .where("COL1 = ?", "arg")
        .where("COL2 = ?", "arg2")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq("COL1 = ? AND COL2 = ?"), eq(new String[] { "arg", "arg2" }), eq((String) null));
  }

  @Test
  public void shouldUseOrderBy() throws Exception {
    ProviderAction.query(TEST_URI)
        .orderBy("COL1 DESC")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq((String) null), eq((String[]) null), eq("COL1 DESC"));
  }

  @Test
  public void shouldPerformProperInsert() throws Exception {
    ContentValues values = new ContentValues();
    values.put("asdf", "value");
    ProviderAction.insert(TEST_URI)
        .values(values)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), eq(values));
  }

  @Test
  public void shouldPerformUpdateWithValues() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    ProviderAction.update(TEST_URI)
        .values(values)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), eq(values), eq((String) null), eq((String[]) null));
  }

  @Test
  public void shouldPerformUpdateWithSeletionAndSelectionArgs() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    ProviderAction.update(TEST_URI)
        .values(values)
        .where("col2 = ?", "blah")
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), eq(values), eq("col2 = ?"), eq(new String[] { "blah" }));
  }

  @Test
  public void shouldPerformDeleteOnUri() throws Exception {
    ProviderAction.delete(TEST_URI).perform(contentResolverMock);
    verify(contentResolverMock).delete(eq(TEST_URI), eq((String) null), eq((String[]) null));
  }

  @Test
  public void shouldCareAboutSelectionAndSeletionArgsWhenDeleting() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where("col1 = ?", "val1")
        .perform(contentResolverMock);
    verify(contentResolverMock).delete(eq(TEST_URI), eq("col1 = ?"), eq(new String[] { "val1" }));
  }

  @Test
  public void shouldBeAbleToUseNonStringObjectsInSelectionArgs() throws Exception {
    ProviderAction.query(TEST_URI)
        .where("col1 > ?", 18)
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq("col1 > ?"), eq(new String[] { "18" }), eq((String) null));
  }
}
