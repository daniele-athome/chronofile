package com.chaidarun.chronofile

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_editor.*

class EditorActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_editor)
    setSupportActionBar(editorToolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    editorText.setText(Store.state.config?.serialize() ?: "")
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_editor, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_editor_cancel -> Log.d(TAG, "Canceled config file edit")
      R.id.action_editor_save -> {
        Store.dispatch(Action.SetConfigFromText(editorText.text.toString()))
        Log.d(TAG, "Edited config file")
      }
      else -> return super.onOptionsItemSelected(item)
    }
    NavUtils.navigateUpFromSameTask(this)
    return true
  }
}
