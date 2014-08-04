package com.getbase.android.db.provider;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentProviderOperation;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
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


  private static Uri createFakeUri(String suffix) {
    return Uri.parse("content://com.fakedomain.base")
        .buildUpon()
        .appendPath(suffix)
        .build();
  }
}
