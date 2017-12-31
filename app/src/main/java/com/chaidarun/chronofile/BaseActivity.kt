package com.chaidarun.chronofile

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import io.reactivex.disposables.Disposable
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class BaseActivity : AppCompatActivity() {

  protected var disposables: List<Disposable>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  override fun onDestroy() {
    disposables?.forEach {
      if (!it.isDisposed) {
        it.dispose()
        Log.d("BaseActivity", "Disposed of $it")
      }
    }
    super.onDestroy()
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
  }
}
