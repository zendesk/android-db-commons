package com.getbase.android.db;

import static org.fest.assertions.Assertions.assertThat;

import com.getbase.android.db.cursors.Cursors;
import com.getbase.android.db.cursors.SingleRowTransforms;
import com.getbase.android.db.provider.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UtilityClassesTest {

  private Class<?> mKlass;

  public UtilityClassesTest(Class<?> klass) {
    mKlass = klass;
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { Cursors.class },
        { SingleRowTransforms.class },
        { Utils.class },
    });
  }

  @Test
  public void shouldBeWellDefined() throws Exception {
    assertThat(mKlass.getSuperclass()).isEqualTo(Object.class);
    assertThat(Modifier.isFinal(mKlass.getModifiers())).isTrue();
    assertThat(mKlass.getDeclaredConstructors()).hasSize(1);

    final Constructor<?> constructor = mKlass.getDeclaredConstructor();
    assertThat(constructor.isAccessible()).isFalse();
    assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();

    for (final Method method : mKlass.getDeclaredMethods()) {
      assertThat(Modifier.isStatic(method.getModifiers())).describedAs(method.getName()).isTrue();
    }
  }
}
