package com.getbase.android.db.cursors

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import com.getbase.android.db.loaders.CursorLoaderBuilder
import com.getbase.android.db.test.TestActivity
import com.getbase.android.db.test.TestContentProvider
import com.getbase.android.db.test.TestContract
import com.google.common.truth.Truth.assertThat
import dk.ilios.spanner.*
import dk.ilios.spanner.config.RuntimeInstrumentConfig
import dk.ilios.spanner.junit.SpannerRunner
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch

@RunWith(SpannerRunner::class)
open class ComposedCursorLoaderBenchmark {

  @JvmField
  @Rule
  var rule = ActivityTestRule(TestActivity::class.java, false, false)

  @JvmField
  @BenchmarkConfiguration
  val configuration = SpannerConfig
      .Builder()
      .addInstrument(RuntimeInstrumentConfig.Builder().measurements(100).build())
      .build()

  @BeforeRep
  fun before() {
    rule.launchActivity(Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  }

  @AfterRep
  fun afterRep() {
    rule.activity.finish()
  }

  @JvmField
  @Param()
  var scenario: SCENARIO? = null

  enum class SCENARIO(val numberOfRows: Int) {
    SINGLE(1),
    TEN(10),
    HUNDRED(100),
    THOUSAND(1000),
    TEN_THOUSAND(10000)
  }

  @Benchmark
  open fun transformedLoaderBuilderBenchmark() {
    benchmarkLoader { builder ->
      builder
          .transform { cursor ->
            cursor.mapTo {
              val index = it.getColumnIndexOrThrow(BaseColumns._ID)
              it.getLong(index)
            }
          }
          .transform { list -> list!!.map { it + 4L } }
          .transform { list -> list!!.map { it - 4L } }
          .build(rule.activity)
    }
  }

  @Benchmark
  open fun transformedRowLoaderBuilderBenchmark() {
    benchmarkLoader { builder ->
      builder
          .transformRow { cursor ->
            val index = cursor!!.getColumnIndexOrThrow(BaseColumns._ID)
            cursor.getLong(index)
          }
          .transformRow { it!! + 4L }
          .transformRow { it!! - 4L }
          .build(rule.activity)
    }
  }

  @Benchmark
  open fun transformedCancellableLoaderBuilderBenchmark() {
    benchmarkLoader { builder ->
      builder
          .cancellableTransform { cursor ->
            cursor.mapTo {
              val index = it.getColumnIndexOrThrow(BaseColumns._ID)
              it.getLong(index)
            }
          }
          .cancellableTransform { list -> list!!.map { it + 4L } }
          .cancellableTransform { list -> list!!.map { it - 4L } }
          .build(rule.activity)
    }
  }

  @Benchmark
  open fun transformedRowCancellableLoaderBuilderBenchmark() {
    benchmarkLoader { builder ->
      builder
          .cancellableTransformRow { cursor ->
            val index = cursor!!.getColumnIndexOrThrow(BaseColumns._ID)
            cursor.getLong(index)
          }
          .cancellableTransformRow { it!! + 4L }
          .cancellableTransformRow { it!! - 4L }
          .build(rule.activity)
    }
  }

  private fun benchmarkLoader(loader: (CursorLoaderBuilder) -> Loader<List<Long>>) {
    val latch = CountDownLatch(1)
    val data = prepareTestData()
    var result: List<Long>? = null

    TestContentProvider.setDataForQuery(data)
    rule.runOnUiThread {
      initLoader(
          { builder -> loader.invoke(builder) },
          { data ->
            result = data
            latch.countDown() // Data is loaded - unlock test thread
          })
    }
    latch.await()//Wait until data is loaded
    assertThat(data).isEqualTo(result)//Ensure that everything went well and data was identically transformed
    TestContentProvider.clearDataForQuery()
  }

  private fun initLoader(loader: (CursorLoaderBuilder) -> Loader<List<Long>>, onFinish: (data: List<Long>?) -> Unit) {
    rule
        .activity
        .supportLoaderManager
        .initLoader(0, Bundle.EMPTY, object : LoaderManager.LoaderCallbacks<List<Long>> {
          override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<Long>> =
              loader.invoke(CursorLoaderBuilder.forUri(TestContract.BASE_URI))

          override fun onLoadFinished(loader: Loader<List<Long>>, data: List<Long>?) {
            onFinish.invoke(data)
          }

          override fun onLoaderReset(loader: Loader<List<Long>>) {
          }
        })
  }

  private fun prepareTestData(): MutableList<Long> {
    return mutableListOf<Long>().apply {
      val random = Random()
      for (i in 0..scenario!!.numberOfRows)
        add(random.nextLong())
    }
  }

  private fun <T> Cursor?.mapTo(transform: (Cursor) -> T): Collection<T> {
    val destination = mutableListOf<T>()
    this?.run {
      moveToFirst()
      while (!isAfterLast) {
        destination.add(transform(this))
        moveToNext()
      }
    }
    return destination
  }
}
