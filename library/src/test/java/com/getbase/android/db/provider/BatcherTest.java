package com.getbase.android.db.provider;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentProviderOperation;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BatcherTest {

  @Test
  public void shouldWorkFineWithValueBackReferences() throws Exception {
    final Insert firstInsert = ProviderAction.insert(Uri.EMPTY);
    final Insert secondInsert = ProviderAction.insert(Uri.EMPTY);
    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(firstInsert)
        .append(secondInsert)
        .append(ProviderAction.insert(Uri.EMPTY))
        .withValueBackReference(firstInsert, BaseColumns._ID)
        .withValueBackReference(secondInsert, "contact_id")
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
        .append(ProviderAction.insert(createFakeUri("only"))).withValueBackReference(first, "column")
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
        .append(ProviderAction.insert(createFakeUri("only"))).withValueBackReference(first, "column")
        .operations();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowAnExceptionInCaseReferencedInsertDoesNotExistInBatcher() throws Exception {
    final Insert first = ProviderAction.insert(createFakeUri("only"));
    Batcher.begin()
        .append(ProviderAction.insert(createFakeUri("only"))).withValueBackReference(first, "column")
        .operations();
  }

  @Test
  public void shouldResolveValueBackReferencesForAllConvertiblesWithinIterable() throws Exception {
    final Insert first = ProviderAction.insert(createFakeUri("fake"));
    final Insert second = ProviderAction.insert(createFakeUri("second"));

    final int copies = 5;
    final List<ConvertibleToOperation> firstDependants =
        Collections.<ConvertibleToOperation>nCopies(copies, ProviderAction.insert(createFakeUri("another")));
    final List<ConvertibleToOperation> secondDependants =
        Collections.<ConvertibleToOperation>nCopies(copies, ProviderAction.insert(createFakeUri("yetAnother")));

    final ArrayList<ContentProviderOperation> operations = Batcher.begin()
        .append(first)
        .append(second)
        .append(firstDependants)
        .withValueBackReference(first, "parent_id")
        .withValueBackReference(second, "another_parent_id")
        .append(secondDependants)
        .withValueBackReference(second, "parent_id")
        .withValueBackReference(first, "another_parent_id")
        .operations();

    assertThat(operations).hasSize(copies * 2 + 2);

    for (ContentProviderOperation contentProviderOperation : operations.subList(2, 2 + copies)) {
      final ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(contentProviderOperation);
      final ContentValues refs = shadowOperation.getValuesBackReferences();
      assertThat(refs.get("parent_id")).isEqualTo(0);
      assertThat(refs.get("another_parent_id")).isEqualTo(1);
    }

    for (ContentProviderOperation contentProviderOperation : operations.subList(2 + copies, operations.size())) {
      final ShadowContentProviderOperation shadowOperation = Robolectric.shadowOf(contentProviderOperation);
      final ContentValues refs = shadowOperation.getValuesBackReferences();
      assertThat(refs.get("parent_id")).isEqualTo(1);
      assertThat(refs.get("another_parent_id")).isEqualTo(0);
    }
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfRemoteExceptionOccurInResolver() throws Exception {
    throwAnExceptionInsideResolversApplyBatch(OperationApplicationException.class);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfOperationApplicationExceptionOccurInResolver() throws Exception {
    throwAnExceptionInsideResolversApplyBatch(OperationApplicationException.class);
  }

  @Test(expected = SecurityException.class)
  public void ifExceptionThrownFromApplyBatchIsNotCheckedThenJustThrowItinResolver() throws Exception {
    throwAnExceptionInsideResolversApplyBatch(SecurityException.class);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfRemoteExceptionOccurInProviderClient() throws Exception {
    throwAnExceptionInsideClientsApplyBatch(OperationApplicationException.class);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfOperationApplicationExceptionOccurInProviderClient() throws Exception {
    throwAnExceptionInsideClientsApplyBatch(OperationApplicationException.class);
  }

  @Test(expected = SecurityException.class)
  public void ifExceptionThrownFromApplyBatchIsNotCheckedThenJustThrowItInProviderClient() throws Exception {
    throwAnExceptionInsideClientsApplyBatch(SecurityException.class);
  }

  @SuppressWarnings("unchecked")
  private void throwAnExceptionInsideResolversApplyBatch(Class<? extends Exception> applyBatchException) throws RemoteException, OperationApplicationException {
    final ContentResolver resolver = mock(ContentResolver.class);
    when(resolver.applyBatch(anyString(), any(ArrayList.class))).thenThrow(applyBatchException);
    Batcher.begin()
        .append(ProviderAction.insert(createFakeUri("fake")))
        .applyBatchOrThrow("com.fakedomain.base", resolver);
  }

  @SuppressWarnings("unchecked")
  private void throwAnExceptionInsideClientsApplyBatch(Class<? extends Exception> applyBatchException) throws RemoteException, OperationApplicationException {
    final ContentProviderClient client = mock(ContentProviderClient.class);
    when(client.applyBatch(any(ArrayList.class))).thenThrow(applyBatchException);
    Batcher.begin()
        .append(ProviderAction.insert(createFakeUri("fake")))
        .applyBatchOrThrow(client);
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
