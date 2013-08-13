package com.github.partition.android.database.provider;

import android.content.ContentResolver;
import android.net.Uri;

public class Delete extends ProviderAction<Integer> {

  private final Selection selection = new Selection();

  Delete(Uri uri) {
    super(uri);
  }

  public Delete where(String selection, Object... selectionArgs) {
    this.selection.append(selection, selectionArgs);
    return this;
  }

  @Override
  public Integer perform(ContentResolver contentResolver) {
    return contentResolver.delete(getUri(), selection.getSelection(), selection.getSelectionArgs());
  }
}
