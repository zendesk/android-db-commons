package com.getbase.android.provider;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

class Selection {

  private final List<String> selection = Lists.newLinkedList();
  private final List<Object> selectionArgs = Lists.newLinkedList();

  void append(String selection, Object... selectionArgs) {
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
    return Collections2.transform(selectionArgs, new Function<Object, String>() {
      @Override
      public String apply(Object object) {
        return object.toString();
      }
    }).toArray(new String[selectionArgs.size()]);
  }
}
