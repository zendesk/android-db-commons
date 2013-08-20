package com.getbase.android.db.provider;

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
}
