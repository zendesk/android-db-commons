package com.github.partition.android.database.provider;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

class Selection {

  private final List<String> selection = Lists.newLinkedList();
  private final List<String> selectionArgs = Lists.newLinkedList();

  void append(String selection, String... selectionArgs) {
    this.selection.add(selection);
    Collections.addAll(this.selectionArgs, selectionArgs);
  }

  String getSelection() {
    if (selection.isEmpty()) {
      return null;
    }
    return Joiner.on(" AND ").join(selection);
  }

  String[] getSelectionArgs() {
    if (selectionArgs.isEmpty()) {
      return null;
    }
    return selectionArgs.toArray(new String[selectionArgs.size()]);
  }
}
