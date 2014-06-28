package com.getbase.android.db.provider;

import com.google.common.collect.Lists;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.List;

class Projection implements Parcelable {

  public static final Creator<Projection> CREATOR = new Creator<Projection>() {
    @Override
    public Projection createFromParcel(Parcel parcel) {
      final List<String> projectionStrings = Lists.newArrayList();
      parcel.readStringList(projectionStrings);
      final Projection projection = new Projection();
      projection.projection.addAll(projectionStrings);
      return projection;
    }

    @Override
    public Projection[] newArray(int size) {
      return new Projection[size];
    }
  };

  private List<String> projection = Lists.newLinkedList();

  void append(String... projection) {
    Collections.addAll(this.projection, projection);
  }

  String[] getProjection() {
    if (!projection.isEmpty()) {
      return projection.toArray(new String[projection.size()]);
    }
    return null;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeStringList(projection);
  }
}
