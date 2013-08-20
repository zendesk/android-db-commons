
package com.getbase.android.db.example.ui;

import com.getbase.android.db.example.R;
import com.getbase.android.db.example.content.PeopleInsertingService;
import com.google.common.collect.Lists;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import java.util.Queue;

public class MainActivity extends FragmentActivity {

  private static Queue<Fragment> fragmentsQueue = Lists.newLinkedList();

  static {
    Fragment firstFragment = new SimpleTransformedStringsListFragment();
    Fragment secondFragment = new WrapperTransformationListFragment();
    fragmentsQueue.add(firstFragment);
    fragmentsQueue.add(secondFragment);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PeopleInsertingService.schedule(this);
    setContentView(R.layout.activity_main);
    Button button = (Button) findViewById(R.id.toggle);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        toggleFragment();
      }
    });
    if (savedInstanceState == null) {
      toggleFragment();
    }
  }

  private void toggleFragment() {
    final Fragment fragment = fragmentsQueue.poll();
    fragmentsQueue.add(fragment);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.content, fragment)
        .commit();
  }
}
