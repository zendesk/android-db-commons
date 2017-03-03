package com.getbase.android.db.test

import android.os.Looper
import java.util.concurrent.ThreadPoolExecutor

class AsyncTasksMonitor {
  private val pool: ThreadPoolExecutor by lazy {
    check(Looper.myLooper() == Looper.getMainLooper())

    Class
        .forName("android.support.v4.content.ModernAsyncTask")
        .getField("THREAD_POOL_EXECUTOR")
        .get(null) as ThreadPoolExecutor
  }

  val isIdleNow: Boolean
    get() = pool.queue.isEmpty() && pool.activeCount == 0
}
