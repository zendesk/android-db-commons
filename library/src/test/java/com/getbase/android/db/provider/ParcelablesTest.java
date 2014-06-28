package com.getbase.android.db.provider;

import static org.fest.assertions.Assertions.assertThat;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

@RunWith(RobolectricTestRunner.class)
public class ParcelablesTest {

  private static final Uri FAKE_URI = Uri.parse("content://com.domain.app/endpoint");

  @Test
  public void shouldPackInsertionInParcel() throws Exception {
    final ContentValues values = new ContentValues();
    values.put("key1", "something");
    values.put("key2", 5L);
    final Insert initialInsert = ProviderAction.insert(FAKE_URI).values(values);
    assertThatWorksAfterDeparcelization(initialInsert, Insert.CREATOR);
  }

  @Test
  public void shouldPackUpdateInParcel() throws Exception {
    final ContentValues values = new ContentValues();
    values.put("key1", "something");
    values.put("key2", 5L);
    final Update initialUpdate = ProviderAction.update(FAKE_URI).values(values).where("sel = ?", 15L);
    assertThatWorksAfterDeparcelization(initialUpdate, Update.CREATOR);
  }

  @Test
  public void shouldPackDeleteInParcel() throws Exception {
    final Delete delete = ProviderAction.delete(FAKE_URI).where("aaa = ?", 15);
    assertThatWorksAfterDeparcelization(delete, Delete.CREATOR);
  }

  @Test
  public void shouldPackQueryInParcel() throws Exception {
    final Query query = ProviderAction.query(FAKE_URI)
        .projection("one", "second")
        .where("aaa = ?", 15)
        .whereIn("bbb", Lists.newArrayList(1, 2, 3, 4, 5))
        .orderBy("something");
    assertThatWorksAfterDeparcelization(query, Query.CREATOR);
  }

  private static <T extends Parcelable> void assertThatWorksAfterDeparcelization(T parcelable, Creator<T> creator) {
    Parcel parcel = Parcel.obtain();
    try {
      parcelable.writeToParcel(parcel, 0);
      parcel.setDataPosition(0);
      assertThat(creator.createFromParcel(parcel)).isEqualTo(parcelable);
    } finally {
      parcel.recycle();
    }
  }
}
