package com.getbase.android.db.cursors

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.app.LoaderManager
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import com.getbase.android.db.loaders.CursorLoaderBuilder
import com.getbase.android.db.test.AsyncTasksMonitor
import com.getbase.android.db.test.TestActivity
import com.getbase.android.db.test.TestContentProvider
import com.getbase.android.db.test.TestContract
import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class ComposedCursorLoaderTest {
  @Rule
  @JvmField
  val rule = ActivityTestRule(TestActivity::class.java)

  private val asyncTasksMonitor = AsyncTasksMonitor()

  @Test
  fun shouldGracefullyHandleTransformationsYieldingTheSameInstance() {
    val initialLoad = TransformData(1, pauseWhenExecuting = false)
    val firstReload = TransformData(2)
    val secondReload = TransformData(2)

    // Check preconditions for this scenario: the reloads have to use the same
    // instance of transformation output object and it has to be different than
    // the result of initial load to trigger onNewDataDelivered callback.
    Truth.assertThat(firstReload.result).isSameAs(secondReload.result)
    Truth.assertThat(initialLoad.result).isNotSameAs(firstReload.result)

    val results = LinkedBlockingDeque<Int>()

    val transforms = LinkedList<TransformData>().apply {
      add(initialLoad)
      add(firstReload)
      add(secondReload)
    }

    // Start the loader on TestContract.BASE_URI. The returned Cursor will be transformed using
    // TransformData objects enqueued in transforms queue.
    rule.runOnUiThread {
      rule
          .activity
          .supportLoaderManager
          .initLoader(0, Bundle.EMPTY, object : LoaderCallbacks<Int> {
            override fun onCreateLoader(id: Int, args: Bundle): Loader<Int> =
                CursorLoaderBuilder
                    .forUri(TestContract.BASE_URI)
                    .transform { transforms.removeFirst().perform() }
                    .build(rule.activity)

            override fun onLoadFinished(loader: Loader<Int>, data: Int) = results.putLast(data)
            override fun onLoaderReset(loader: Loader<Int>) = Unit
          })
    }

    // Wait until the initial load is completed. We do this to ensure the content observers
    // are registered on TestContract.BASE_URI and we can trigger Loader reload with
    // ContentResolver.notifyChange call.
    Truth.assertThat(results.takeFirst()).isEqualTo(1)

    // The reloads are scheduled on the main Looper.
    scheduleLoaderReload()

    // Let's wait until the async task used by our Loader is started.
    firstReload.waitUntilStarted()

    // Now the things get tricky. We have to switch to the main thread to properly
    // orchestrate all threads.
    rule.runOnUiThread {
      // The first reload is paused. We schedule yet another reload on the main Looper. Note
      // that we're on the main thread here, so the reload will be processed *after* everything
      // we do in this runnable.
      scheduleLoaderReload()

      // We want to complete the second reload before the result of first reload are
      // delivered and processed on the main thread. To do that, we schedule yet another
      // task that will be processed on the main thread after the second reload trigger.
      scheduleMainLooperTask {
        // At this point we're after the second reload trigger. The first reload is still
        // waiting in the cursor transformation. We synchronously wait until the task is
        // started...
        secondReload.waitUntilStarted()

        // ...and finished.
        secondReload.proceed()
        asyncTasksMonitor.waitUntilIdle()

        // Again, we synchronously wait for the task to finish. We want to be sure that
        // the results of the first reload are not processed yet. At this point we'll have:
        // - Two delivery tasks for both reloads enqueued in main Looper.
        // - Two pending result entries for the same result in the ComposedCursorLoader
        //   internals.
        // - The first reload results should be cancelled; the second reload results should be
        //   delivered to onLoadFinished callback.
      }

      // We resume the first reload execution on the background thread.
      firstReload.proceed()

      // We synchronously wait until the first reload on the background thread
      // is completed, because we don't want the first reload task to be added as
      // mCancellingTask in the guts of AsyncTaskLoader.onCancelLoad. Instead, we
      // want the results from this task to go through the branch that "cancels"
      // the results delivered from an old task.
      asyncTasksMonitor.waitUntilIdle()

      // At this point the background tasks are completed and we have the following
      // queue on the main Looper:
      // - reload triggered by ContentResolver.notifyChange call
      // - a task that waits for a second reload
      // - result delivery from first reload.
    }

    // The whole machinery is in motion right now, we just need to wait.
    // If everything goes well, the first reload will be cancelled, and the
    // second reload will be successfully delivered.
    Truth.assertThat(results.takeFirst()).isEqualTo(2)
  }

  @Test
  fun shouldNotInvokeTransformationWhenLoaderDestroyedDuringQuery() {
    val asyncTaskIdleLatch = CountDownLatch(1)
    //As throwing exception on background loader thread do not fail test we need flag to ensure
    //that transformation was never invoked
    val transformationInvoked = AtomicBoolean(false)
    //Prepare TestContentProvider to wait when loader start query for data
    TestContentProvider.blockOnQuery()

    rule.runOnUiThread {
      rule
          .activity
          .supportLoaderManager
          .initLoader(1, Bundle.EMPTY, object : LoaderManager.LoaderCallbacks<Nothing> {
            override fun onCreateLoader(id: Int, args: Bundle) =
                CursorLoaderBuilder
                    .forUri(TestContract.BASE_URI)
                    .transform {
                      transformationInvoked.set(true)
                      throw AssertionError("This transformation should never be invoked")
                    }
                    .build(rule.activity)

            override fun onLoadFinished(loader: Loader<Nothing>, data: Nothing?) {
              throw AssertionError("This loader should be cancelled so result should never be returned")
            }

            override fun onLoaderReset(loader: Loader<Nothing>) {}
          })
    }

    //Now when loader is scheduled, test thread should wait until
    //Loader is created and initialized by LoaderManager
    TestContentProvider.waitUntilQueryStarted()

    //As loader is busy querying for data we cancel load in background.
    //Then we allow it to finish query and move forward. In the meantime
    //test thread is waiting until asyncTaskMonitor will be idle.
    rule.runOnUiThread {
      rule
          .activity
          .supportLoaderManager
          .destroyLoader(1)
      TestContentProvider.proceedQuery()
      asyncTasksMonitor.waitUntilIdle()
      asyncTaskIdleLatch.countDown()
    }

    //After all background work is done and check if transformation will not be invoked
    asyncTaskIdleLatch.await()
    Truth.assertThat(transformationInvoked.get()).isFalse()
  }

  @Test
  fun shouldNotInvokeSecondTransformationWhenLoaderDestroyedDuringFirstTransformation() {
    val firstTransformation = TransformData(1)
    val secondTransformationInvoked = AtomicBoolean(false)
    val asyncTaskIdleLatch = CountDownLatch(1)

    rule.runOnUiThread {
      rule
          .activity
          .supportLoaderManager
          .initLoader(1, Bundle.EMPTY, object : LoaderManager.LoaderCallbacks<Nothing> {
            override fun onCreateLoader(id: Int, args: Bundle) =
                CursorLoaderBuilder
                    .forUri(TestContract.BASE_URI)
                    .transform { firstTransformation.perform() }
                    .transform {
                      secondTransformationInvoked.set(true)
                      throw AssertionError("This transformation should never be invoked")
                    }
                    .build(rule.activity)

            override fun onLoadFinished(loader: Loader<Nothing>, data: Nothing?) {
              throw AssertionError("This loader should be cancelled so result should never be returned")
            }

            override fun onLoaderReset(loader: Loader<Nothing>) {}
          })
    }
    firstTransformation.waitUntilStarted()
    rule.runOnUiThread {
      rule
          .activity
          .supportLoaderManager
          .destroyLoader(1)
      firstTransformation.proceed()
      asyncTasksMonitor.waitUntilIdle()
      asyncTaskIdleLatch.countDown()
    }
    asyncTaskIdleLatch.await()
    Truth.assertThat(secondTransformationInvoked.get()).isFalse()
  }

  interface RowTransformation {
    fun transform(): Int;
  }

  @Test
  fun shouldNotInvokeTransformationForSecondRowWhenLoaderDestroyedDuringTransformationOfFirstRow() {
    val secondTransformationInvoked = AtomicBoolean(false)
    val asyncTaskIdleLatch = CountDownLatch(1)
    TestContentProvider.setDataForQuery(mutableListOf(1, 2))
    val firstTransformation = TransformData(1)
    val transformations = mutableListOf(
        object : RowTransformation {
          override fun transform(): Int = firstTransformation.perform()
        },
        object : RowTransformation {
          override fun transform(): Int {
            secondTransformationInvoked.set(true)
            return 2
          }
        }
    )

    rule.runOnUiThread {
      rule
          .activity
          .supportLoaderManager
          .initLoader(1, Bundle.EMPTY, object : LoaderManager.LoaderCallbacks<List<Int>> {
            override fun onCreateLoader(id: Int, args: Bundle) =
                CursorLoaderBuilder
                    .forUri(TestContract.BASE_URI)
                    .transformRow {
                      val transformation = transformations.first()
                      transformations.remove(transformation)
                      transformation.transform()
                    }
                    .build(rule.activity)

            override fun onLoadFinished(loader: Loader<List<Int>>, data: List<Int>?) {
              throw AssertionError("This loader should be cancelled so result should never be returned")
            }

            override fun onLoaderReset(loader: Loader<List<Int>>) {}
          })
    }
    firstTransformation.waitUntilStarted()
    //Data is loaded and loader is blocked on first row transformation
    rule.runOnUiThread {
      rule
          .activity
          .supportLoaderManager
          .destroyLoader(1)
      firstTransformation.proceed()
      asyncTasksMonitor.waitUntilIdle()
      asyncTaskIdleLatch.countDown()
    }
    asyncTaskIdleLatch.await()
    Truth.assertThat(secondTransformationInvoked.get()).isFalse()
  }


  private fun scheduleMainLooperTask(task: () -> Unit) = Handler(Looper.getMainLooper()).post(task)

  private fun scheduleLoaderReload() = rule.activity.contentResolver.notifyChange(TestContract.BASE_URI, null)

  private fun AsyncTasksMonitor.waitUntilIdle() {
    while (!isIdleNow) {
      SystemClock.sleep(10)
    }
  }

  class TransformData(val result: Int, pauseWhenExecuting: Boolean = true) {
    private val startLatch = CountDownLatch(if (pauseWhenExecuting) 1 else 0)
    private val proceedLatch = CountDownLatch(if (pauseWhenExecuting) 1 else 0)

    fun perform(): Int {
      startLatch.countDown()
      proceedLatch.awaitOrFail()
      return result
    }

    fun waitUntilStarted() = startLatch.awaitOrFail()
    fun proceed() = proceedLatch.countDown()

    private fun CountDownLatch.awaitOrFail(): Unit {
      if (!await(3, TimeUnit.SECONDS)) {
        Assert.fail()
      }
    }
  }
}
