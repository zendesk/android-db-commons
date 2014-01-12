package com.getbase.android.db.query;

import static com.getbase.android.db.query.Expressions.column;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DeleteTest {

  @Test
  public void shouldBuildTheDeleteWithoutSelection() throws Exception {
    Delete delete = Delete.delete().from("A").build();

    assertThat(delete.mTable).isEqualTo("A");
  }

  @Test
  public void shouldBuildTheDeleteWithSingleSelection() throws Exception {
    Delete delete = Delete.delete().from("A").where("a IS NULL").build();

    assertThat(delete.mSelection).isEqualTo("(a IS NULL)");
  }

  @Test
  public void shouldBuildTheDeleteWithSingleSelectionBuiltFromExpressions() throws Exception {
    Delete delete = Delete.delete().from("A").where(column("a").is().nul()).build();

    assertThat(delete.mSelection).isEqualTo("(a IS NULL)");
  }

  @Test
  public void shouldBuildTheDeleteWithMultipleSelections() throws Exception {
    Delete delete = Delete.delete().from("A").where("a IS NULL").where("b IS NULL").build();

    assertThat(delete.mSelection).isEqualTo("(a IS NULL) AND (b IS NULL)");
  }

  @Test
  public void shouldBuildTheDeleteWithBoundParams() throws Exception {
    Delete delete = Delete.delete().from("A").where("a=?", 0).build();

    assertThat(delete.mSelection).isEqualTo("(a=?)");
    assertThat(delete.mSelectionArgs).containsOnly("0");
  }
}
