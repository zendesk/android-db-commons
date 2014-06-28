package com.getbase.android.db.provider;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Selection implements Parcelable {

  public static final Creator<Selection> CREATOR = new Creator<Selection>() {
    @Override
    public Selection createFromParcel(Parcel parcel) {
      List<String> selection = Lists.newArrayList();
      List<String> selectionArgs = Lists.newArrayList();
      parcel.readStringList(selection);
      parcel.readStringList(selectionArgs);
      final Selection result = new Selection();
      result.selection.addAll(selection);
      result.selectionArgs.addAll(selectionArgs);
      return result;
    }

    @Override
    public Selection[] newArray(int size) {
      return new Selection[size];
    }
  };

  private static final Function<String, String> SURROUND_WITH_PARENS = new Function<String, String>() {
    @Override
    public String apply(String input) {
      return "(" + input + ")";
    }
  };

  private final List<String> selection = Lists.newLinkedList();
  private final List<String> selectionArgs = Lists.newLinkedList();

  void append(String selection, Object... selectionArgs) {
    this.selection.add(selection);
    for (Object selectionArg : selectionArgs) {
      this.selectionArgs.add(selectionArg.toString());
    }
  }

  String getSelection() {
    if (selection.isEmpty()) {
      return null;
    }
    return Joiner.on(" AND ").join(Collections2.transform(selection, SURROUND_WITH_PARENS));
  }

  String[] getSelectionArgs() {
    if (selectionArgs.isEmpty()) {
      return null;
    }
    return selectionArgs.toArray(new String[selectionArgs.size()]);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeStringList(selection);
    parcel.writeStringList(selectionArgs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Selection selection1 = (Selection) o;

    return Objects.equal(selection1.selection, selection) && Objects.equal(selection1.selectionArgs, selectionArgs);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(selection, selectionArgs);
  }
}
