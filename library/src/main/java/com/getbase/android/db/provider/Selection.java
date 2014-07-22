package com.getbase.android.db.provider;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

class Selection {

  private static final Function<String, String> SURROUND_WITH_PARENS = new Function<String, String>() {
    @Override
    public String apply(String input) {
      return "(" + input + ")";
    }
  };

  private final List<String> selection = Lists.newLinkedList();
  private final List<Object> selectionArgs = Lists.newLinkedList();

  void append(String selection, Object... selectionArgs) {
    Preconditions.checkArgument(selection != null || selectionArgs == null || selectionArgs.length == 0,
        "selection cannot be null when arguments are provided");

    if (selection != null) {
      this.selection.add(selection);
      if (selectionArgs != null) {
        Collections.addAll(this.selectionArgs, selectionArgs);
      }
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
    return Collections2.transform(selectionArgs, Functions.toStringFunction()).toArray(new String[selectionArgs.size()]);
  }
}
