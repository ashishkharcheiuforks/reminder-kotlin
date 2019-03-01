package com.elementary.tasks.core.app_widgets.notes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.databinding.ActivityWidgetNoteConfigBinding

/**
 * Copyright 2015 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class NotesWidgetConfigActivity : ThemedActivity<ActivityWidgetNoteConfigBinding>() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null

    override fun layoutRes(): Int = R.layout.activity_widget_note_config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()

        binding.fabSave.setOnClickListener { savePrefs() }
        binding.bgColorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        binding.bgColorSlider.setListener { position, _ ->
            binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
            updateIcons(position)
        }
        updateIcons(0)

        showCurrentTheme()
    }

    private fun showCurrentTheme() {
        val sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)
        val headerBg = sp.getInt(WIDGET_HEADER_BG_COLOR + widgetID, 0)
        binding.bgColorSlider.setSelection(headerBg)
        updateIcons(headerBg)
    }

    private fun updateIcons(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            binding.btnSettings.setImageResource(R.drawable.ic_twotone_settings_white)
            binding.btnAddNote.setImageResource(R.drawable.ic_twotone_add_white)
            binding.widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            binding.btnSettings.setImageResource(R.drawable.ic_twotone_settings_24px)
            binding.btnAddNote.setImageResource(R.drawable.ic_twotone_add_24px)
            binding.widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
    }

    private fun readIntent() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        resultValue = Intent()
        resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
        setResult(RESULT_CANCELED, resultValue)
    }

    private fun savePrefs() {
        val sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)
        sp.edit()
                .putInt(WIDGET_HEADER_BG_COLOR + widgetID, binding.bgColorSlider.selectedItem)
                .apply()
        val appWidgetManager = AppWidgetManager.getInstance(this)
        NotesWidget.updateWidget(this, appWidgetManager, sp, widgetID)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val WIDGET_PREF = "new_notes_prefs"
        const val WIDGET_HEADER_BG_COLOR = "widget_header_bg_color"
    }
}