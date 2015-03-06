package com.getbase.android.db.provider;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

class Projection {

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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Projection other = (Projection) o;

    return Objects.equal(projection, other.projection);
  }

  @Override
  public int hashCode() {
    return projection.hashCode();
  }
}
