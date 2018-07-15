package com.elementary.tasks.core.appWidgets.quickReminder

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews

import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.WidgetUtils
import com.elementary.tasks.reminder.create_edit.AddReminderActivity

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

class QuickReminderWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val sp = context.getSharedPreferences(
                QuickReminderWidgetConfig.QUICK_REMINDER_WIDGET_PREF, Context.MODE_PRIVATE)
        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
        }
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                         sp: SharedPreferences, widgetID: Int) {
            val rv = RemoteViews(context.packageName, R.layout.widget_quick_reminder)
            val widgetColor = sp.getInt(QuickReminderWidgetConfig.QUICK_REMINDER_WIDGET_COLOR + widgetID, 0)
            rv.setInt(R.id.widgetBg, "setBackgroundResource", widgetColor)
            val configIntent = Intent(context, AddReminderActivity::class.java)
            val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            rv.setOnClickPendingIntent(R.id.imageView, configPendingIntent)
            WidgetUtils.setIcon(context, rv, R.drawable.ic_access_time_white_24dp, R.id.imageView)
            appWidgetManager.updateAppWidget(widgetID, rv)
        }
    }
}