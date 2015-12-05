package com.getbase.android.db.provider;

import static com.google.common.truth.Truth.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.android.content.ContentValuesEntry.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ProviderActionsTest {

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
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq("(COL1 = ?) AND (COL2 = ?)"), eq(new String[] { "arg", "arg2" }), eq((String) null));
  }

  @Test
  public void shouldAddParenthesesForEachWhereWhenQuerying() throws Exception {
    ProviderAction.query(TEST_URI)
        .where("COL1 = ? OR COL1 = ?", "arg", "argh")
        .where("COL2 = ?", "arg2")
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq("(COL1 = ? OR COL1 = ?) AND (COL2 = ?)"), eq(new String[] { "arg", "argh", "arg2" }), eq((String) null));
  }

  @Test
  public void shouldAddParenthesesForEachWhereWhenDeleting() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where("COL1 = ? OR COL1 = ?", "arg", "argh")
        .where("COL2 = ?", "arg2")
        .perform(contentResolverMock);
    verify(contentResolverMock).delete(eq(TEST_URI), eq("(COL1 = ? OR COL1 = ?) AND (COL2 = ?)"), eq(new String[] { "arg", "argh", "arg2" }));
  }

  @Test
  public void shouldAddParenthesesForEachWhereWhenUpdating() throws Exception {
    ProviderAction.update(TEST_URI)
        .where("COL1 = ? OR COL1 = ?", "arg", "argh")
        .where("COL2 = ?", "arg2")
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), any(ContentValues.class), eq("(COL1 = ? OR COL1 = ?) AND (COL2 = ?)"), eq(new String[] { "arg", "argh", "arg2" }));
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
  public void shouldPerformInsertWithSingleValue() throws Exception {
    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .value("col1", "val1")
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());
    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"));
  }

  @Test
  public void insertShouldNotModifyPassedContentValues() throws Exception {
    ContentValues values = new ContentValues();

    ProviderAction.insert(TEST_URI)
        .values(values)
        .value("key", "value")
        .perform(contentResolverMock);

    assertThat(values.containsKey("key")).isFalse();

    ContentValues valuesToConcatenate = new ContentValues();
    valuesToConcatenate.put("another_key", "another_value");

    ProviderAction.insert(TEST_URI)
        .values(values)
        .values(valuesToConcatenate)
        .perform(contentResolverMock);

    assertThat(values.containsKey("another_key")).isFalse();
  }

  @Test
  public void shouldPerformInsertWithConcatenatedContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");

    ContentValues secondValues = new ContentValues();
    secondValues.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", "val2"));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenBySingleValue() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    values.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .values(values)
        .value("col2", null)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", null));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenByOtherContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");
    firstValues.put("col2", "val2");

    ContentValues secondValues = new ContentValues();
    secondValues.putNull("col2");
    secondValues.put("col3", "val3");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.insert(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).insert(eq(TEST_URI), contentValuesArgument.capture());

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col3", "val3"), entry("col2", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectInsertWithSingleValueOfUnsupportedType() throws Exception {
    ProviderAction.insert(TEST_URI).value("col1", new Object());
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
  public void shouldPerformUpdateWithSingleValue() throws Exception {
    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .value("col1", "val1")
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));
    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"));
  }

  @Test
  public void shouldPerformUpdateWithConcatenatedContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");

    ContentValues secondValues = new ContentValues();
    secondValues.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", "val2"));
  }

  @Test
  public void shouldPerformUpdateWithContentValuesOverriddenBySingleValue() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    values.put("col2", "val2");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .values(values)
        .value("col2", null)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", null));
  }

  @Test
  public void shouldPerformUpdateWithContentValuesOverriddenByOtherContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");
    firstValues.put("col2", "val2");

    ContentValues secondValues = new ContentValues();
    secondValues.putNull("col2");
    secondValues.put("col3", "val3");

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    ProviderAction.update(TEST_URI)
        .values(firstValues)
        .values(secondValues)
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), contentValuesArgument.capture(), eq((String) null), eq((String[]) null));

    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col3", "val3"), entry("col2", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectUpdateWithSingleValueOfUnsupportedType() throws Exception {
    ProviderAction.update(TEST_URI).value("col1", new Object());
  }

  @Test
  public void shouldPerformUpdateWithSelectionAndSelectionArgs() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    ProviderAction.update(TEST_URI)
        .values(values)
        .where("col2 = ?", "blah")
        .perform(contentResolverMock);
    verify(contentResolverMock).update(eq(TEST_URI), eq(values), eq("(col2 = ?)"), eq(new String[] { "blah" }));
  }

  @Test
  public void updateShouldNotModifyPassedContentValues() throws Exception {
    ContentValues values = new ContentValues();

    ProviderAction.update(TEST_URI)
        .values(values)
        .value("key", "value")
        .perform(contentResolverMock);

    assertThat(values.containsKey("key")).isFalse();

    ContentValues valuesToConcatenate = new ContentValues();
    valuesToConcatenate.put("another_key", "another_value");

    ProviderAction.update(TEST_URI)
        .values(values)
        .values(valuesToConcatenate)
        .perform(contentResolverMock);

    assertThat(values.containsKey("another_key")).isFalse();
  }

  @Test
  public void shouldPerformDeleteOnUri() throws Exception {
    ProviderAction.delete(TEST_URI).perform(contentResolverMock);
    verify(contentResolverMock).delete(eq(TEST_URI), eq((String) null), eq((String[]) null));
  }

  @Test
  public void shouldCareAboutSelectionAndSelectionArgsWhenDeleting() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where("col1 = ?", "val1")
        .perform(contentResolverMock);
    verify(contentResolverMock).delete(eq(TEST_URI), eq("(col1 = ?)"), eq(new String[] { "val1" }));
  }

  @Test
  public void shouldBeAbleToUseNonStringObjectsInSelectionArgs() throws Exception {
    ProviderAction.query(TEST_URI)
        .where("col1 > ?", 18)
        .perform(contentResolverMock);
    verify(contentResolverMock).query(eq(TEST_URI), eq((String[]) null), eq("(col1 > ?)"), eq(new String[] { "18" }), eq((String) null));
  }

  @Test
  public void shouldBeAbleToCreateASelectionWithWhereIn() throws Exception {
    final List<?> inSet = Lists.newArrayList(1L, "two", 3L);
    ProviderAction.query(TEST_URI)
        .whereIn("col1", inSet)
        .perform(contentResolverMock);
    final String expectedSelection = "(" + "col1 IN (" + Joiner.on(",").join(Collections2.transform(inSet, Utils.toEscapedSqlFunction())) + ")" + ")";
    verify(contentResolverMock).query(eq(TEST_URI),
        eq((String[]) null),
        eq(expectedSelection),
        eq((String[]) null),
        eq((String) null));
  }

  @Test
  public void shouldBeAbleToCreateAnUpdateWithWhereIn() throws Exception {
    final List<Object> inSet = Lists.<Object>newArrayList(1L, "two", 3L);
    ProviderAction.update(TEST_URI)
        .whereIn("col1", inSet)
        .perform(contentResolverMock);
    final String expectedSelection = "(" + "col1 IN (" + Joiner.on(",").join(Collections2.transform(inSet, Utils.toEscapedSqlFunction())) + ")" + ")";
    verify(contentResolverMock).update(eq(TEST_URI),
        any(ContentValues.class),
        eq(expectedSelection),
        eq((String[]) null));
  }

  @Test
  public void shouldBeAbleToCreateADeleteWithWhereIn() throws Exception {
    final List<Object> inSet = Lists.<Object>newArrayList(1L, "two", 3L);
    ProviderAction.delete(TEST_URI)
        .whereIn("col1", inSet)
        .perform(contentResolverMock);
    final String expectedSelection = "(" + "col1 IN (" + Joiner.on(",").join(Collections2.transform(inSet, Utils.toEscapedSqlFunction())) + ")" + ")";
    verify(contentResolverMock).delete(eq(TEST_URI),
        eq(expectedSelection),
        eq((String[]) null));
  }

  @Test
  public void shouldAlwaysPassNonNullContentValuesOnInsert() throws Exception {
    ProviderAction.insert(TEST_URI)
        .perform(contentResolverMock);

    verify(contentResolverMock).insert(eq(TEST_URI), isNotNull(ContentValues.class));
  }

  @Test
  public void shouldAlwaysPassNonNullContentValuesOnUpdate() throws Exception {
    ProviderAction.update(TEST_URI)
        .perform(contentResolverMock);

    verify(contentResolverMock).update(eq(TEST_URI), isNotNull(ContentValues.class), isNull(String.class), isNull(String[].class));
  }

  @Test
  public void shouldAllowUsingNullSelectionOnQuery() throws Exception {
    ProviderAction.query(TEST_URI)
        .where(null)
        .perform(contentResolverMock);

    verify(contentResolverMock).query(eq(TEST_URI), isNull(String[].class), isNull(String.class), isNull(String[].class), isNull(String.class));
  }

  @Test
  public void shouldAllowUsingNullSelectionOnUpdate() throws Exception {
    ProviderAction.update(TEST_URI)
        .where(null)
        .perform(contentResolverMock);

    verify(contentResolverMock).update(eq(TEST_URI), any(ContentValues.class), isNull(String.class), isNull(String[].class));
  }

  @Test
  public void shouldAllowUsingNullSelectionOnDelete() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where(null)
        .perform(contentResolverMock);

    verify(contentResolverMock).delete(eq(TEST_URI), isNull(String.class), isNull(String[].class));
  }

  private static final Object[] NULL_ARGS = null;

  @Test
  public void shouldAllowUsingNullArgumentsForSelectionOnQuery() throws Exception {
    ProviderAction.query(TEST_URI)
        .where("col1 IS NULL", NULL_ARGS)
        .perform(contentResolverMock);

    verify(contentResolverMock).query(eq(TEST_URI), isNull(String[].class), eq("(col1 IS NULL)"), isNull(String[].class), isNull(String.class));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForSelectionOnUpdate() throws Exception {
    ProviderAction.update(TEST_URI)
        .where("col1 IS NULL", NULL_ARGS)
        .perform(contentResolverMock);

    verify(contentResolverMock).update(eq(TEST_URI), any(ContentValues.class), eq("(col1 IS NULL)"), isNull(String[].class));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForSelectionOnDelete() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where("col1 IS NULL", NULL_ARGS)
        .perform(contentResolverMock);

    verify(contentResolverMock).delete(eq(TEST_URI), eq("(col1 IS NULL)"), isNull(String[].class));
  }

  @Test
  public void shouldAllowUsingNullSelectionAndArgumentsOnQuery() throws Exception {
    ProviderAction.query(TEST_URI)
        .where(null, NULL_ARGS)
        .perform(contentResolverMock);

    verify(contentResolverMock).query(eq(TEST_URI), isNull(String[].class), isNull(String.class), isNull(String[].class), isNull(String.class));
  }

  @Test
  public void shouldAllowUsingNullSelectionAndArgumentsOnUpdate() throws Exception {
    ProviderAction.update(TEST_URI)
        .where(null, NULL_ARGS)
        .perform(contentResolverMock);

    verify(contentResolverMock).update(eq(TEST_URI), any(ContentValues.class), isNull(String.class), isNull(String[].class));
  }

  @Test
  public void shouldAllowUsingNullSelectionAndArgumentsOnDelete() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where(null, NULL_ARGS)
        .perform(contentResolverMock);

    verify(contentResolverMock).delete(eq(TEST_URI), isNull(String.class), isNull(String[].class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowUsingNonNullArgumentsWithNullSelectionOnQuery() throws Exception {
    ProviderAction.query(TEST_URI)
        .where(null, "arg1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowUsingNonNullArgumentsWithNullSelectionOnUpdate() throws Exception {
    ProviderAction.update(TEST_URI)
        .where(null, "arg1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowUsingNonNullArgumentsWithNullSelectionOnDelete() throws Exception {
    ProviderAction.delete(TEST_URI)
        .where(null, "arg1");
  }
}
