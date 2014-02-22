package com.getbase.android.db.provider;

import static org.fest.assertions.Assertions.assertThat;

import com.google.common.collect.Iterables;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentProviderOperation;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class ConvertToContentProviderOperationTest {

  @Test
  public void shouldConstructInsertOperation() throws Exception {
    ContentValues values = new ContentValues();
    values.put("key", "value");
    values.put("second", 2L);

    final ContentProviderOperation operation = ProviderAction.insert(createFakeUri("endpoint"))
        .values(values)
        .toContentProviderOperation();

    final ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
    assertThat(operation.getUri()).isEqualTo(createFakeUri("endpoint"));
    assertThat(shadowOperation.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_INSERT);
    assertThat(shadowOperation.getContentValues()).isEqualTo(values);
  }

  @Test
  public void shouldConstructUpdateOperation() throws Exception {
    final ContentProviderOperation operation = ProviderAction.update(createFakeUri("endpoint"))
        .value("key", "value")
        .where("key=?", "hello")
        .toContentProviderOperation();

    final ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
    assertThat(operation.getUri()).isEqualTo(createFakeUri("endpoint"));
    assertThat(shadowOperation.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_UPDATE);
    assertThat(shadowOperation.getSelection()).isEqualTo("(key=?)");
    assertThat(shadowOperation.getSelectionArgs()).isEqualTo(new String[] { "hello" });
  }

  @Test
  public void shouldConstructDeleteOperation() throws Exception {
    final ContentProviderOperation operation = ProviderAction.delete(createFakeUri("endpoint"))
        .where("key=?", "hello")
        .toContentProviderOperation();

    final ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
    assertThat(operation.getUri()).isEqualTo(createFakeUri("endpoint"));
    assertThat(shadowOperation.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_DELETE);
    assertThat(shadowOperation.getSelection()).isEqualTo("(key=?)");
    assertThat(shadowOperation.getSelectionArgs()).isEqualTo(new String[] { "hello" });
  }

  @Test
  public void shouldWorkFineWithValueBackReferences() throws Exception {
    final Insert firstInsert = ProviderAction.insert(Uri.EMPTY);
    final Insert secondInsert = ProviderAction.insert(Uri.EMPTY);
    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(firstInsert)
        .append(secondInsert)
        .appendWithBackRef(ProviderAction.insert(Uri.EMPTY))
        .forPrevious(firstInsert, BaseColumns._ID)
        .forPrevious(secondInsert, "contact_id")
        .operations();

    assertThat(operations).hasSize(3);

    final ContentProviderOperation lastOperation = operations.get(2);
    ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(lastOperation);
    final ContentValues backRefs = shadowOperation.getValuesBackReferences();
    assertThat(backRefs.get("_id")).isEqualTo(0);
    assertThat(backRefs.get("contact_id")).isEqualTo(1);
  }

  @Test
  public void shouldGenerateProperListOfContentProviderOperations() throws Exception {
    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(ProviderAction.insert(createFakeUri("first")))
        .append(ProviderAction.insert(createFakeUri("second")))
        .append(ProviderAction.update(createFakeUri("third")).value("test", 1L))
        .operations();
    assertThat(operations).hasSize(3);

    operationAssert(operations.get(0), createFakeUri("first"), ShadowContentProviderOperation.TYPE_INSERT);
    operationAssert(operations.get(1), createFakeUri("second"), ShadowContentProviderOperation.TYPE_INSERT);
    operationAssert(operations.get(2), createFakeUri("third"), ShadowContentProviderOperation.TYPE_UPDATE);
  }

  @Test
  public void shouldTakeCareAboutContentValuesInBatch() throws Exception {
    ContentValues values = new ContentValues();
    values.put("test1", 1L);
    values.put("test2", "blah");

    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(ProviderAction.insert(createFakeUri("first")).values(values))
        .operations();

    final ShadowContentProviderOperation contentProviderOperation = Robolectric.shadowOf(operations.get(0));
    assertThat(contentProviderOperation.getContentValues()).isEqualTo(values);
  }

  @Test
  public void shouldMergeBatches() throws Exception {
    final Batcher firstPart = Batcher.begin()
        .append(ProviderAction.insert(createFakeUri("first")))
        .append(ProviderAction.insert(createFakeUri("second")));

    final Batcher secondPart = Batcher.begin()
        .append(ProviderAction.update(createFakeUri("third")).value("test", 1L));

    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(firstPart)
        .append(secondPart)
        .operations();

    assertThat(operations).hasSize(3);

    operationAssert(operations.get(0), createFakeUri("first"), ShadowContentProviderOperation.TYPE_INSERT);
    operationAssert(operations.get(1), createFakeUri("second"), ShadowContentProviderOperation.TYPE_INSERT);
    operationAssert(operations.get(2), createFakeUri("third"), ShadowContentProviderOperation.TYPE_UPDATE);
  }

  @Test
  public void shouldResolveBackReferencesFromPreviousBatch() throws Exception {
    final Insert firstInsert = ProviderAction.insert(createFakeUri("first"));

    final Batcher firstPart = Batcher.begin()
        .append(firstInsert)
        .append(ProviderAction.insert(createFakeUri("second")));

    final Batcher secondPart = Batcher.begin()
        .appendWithBackRef(ProviderAction
            .update(createFakeUri("third"))
            .value("test", 1L)
        ).forPrevious(firstInsert, "column");

    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(firstPart)
        .append(secondPart)
        .operations();

    final ContentProviderOperation lastOperation = Iterables.getLast(operations);
    ShadowContentProviderOperation shadowLastOperation = Robolectric.shadowOf(lastOperation);
    final ContentValues backRefs = shadowLastOperation.getValuesBackReferences();
    assertThat(backRefs.get("column")).isEqualTo(0);
  }

  @Test
  public void shouldGenerateEmptyOperations() throws Exception {
    assertThat(Batcher.begin().operations()).isEmpty();
  }

  @Test
  public void shouldMapToProperInsertEvenIfTheyHaveIdenticalState() throws Exception {
    final Insert first = ProviderAction.insert(createFakeUri("only"));
    final Insert second = ProviderAction.insert(createFakeUri("only"));

    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(first)
        .append(second)
        .appendWithBackRef(ProviderAction.insert(createFakeUri("only"))).forPrevious(first, "column")
        .operations();

    assertThat(operations).hasSize(3);
    final ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(Iterables.getLast(operations));
    final ContentValues backRefs = shadowOperation.getValuesBackReferences();
    assertThat(backRefs.get("column")).isEqualTo(0);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowAnExceptionIfRequestingForPreviousWhenItsDuplicated() throws Exception {
    final Insert first = ProviderAction.insert(createFakeUri("only"));
    Batcher.begin()
        .append(first)
        .append(first)
        .appendWithBackRef(ProviderAction.insert(createFakeUri("only"))).forPrevious(first, "column")
        .operations();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowAnExceptionInCaseReferencedInsertDoesNotExistInBatcher() throws Exception {
    final Insert first = ProviderAction.insert(createFakeUri("only"));
    Batcher.begin()
        .appendWithBackRef(ProviderAction.insert(createFakeUri("only"))).forPrevious(first, "column")
        .operations();
  }

  private static Uri createFakeUri(String suffix) {
    return Uri.parse("content://com.fakedomain.base")
        .buildUpon()
        .appendPath(suffix)
        .build();
  }

  private static void operationAssert(ContentProviderOperation operation, Uri uri, int type) {
    final ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(operation);
    assertThat(operation.getUri()).isEqualTo(uri);
    assertThat(shadowOperation.getType()).isEqualTo(type);
  }
}
