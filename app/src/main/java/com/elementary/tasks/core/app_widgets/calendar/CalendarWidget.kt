package com.elementary.tasks.core.app_widgets.calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.format.DateUtils
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.app_widgets.buttons.VoiceWidgetDialog
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import java.util.*

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
class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val sp = context.getSharedPreferences(CalendarWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
        for (i in appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, i)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val editor = context.getSharedPreferences(
                CalendarWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit()
        for (widgetID in appWidgetIds) {
            editor.remove(CalendarWidgetConfigActivity.WIDGET_BG + widgetID)
            editor.remove(CalendarWidgetConfigActivity.WIDGET_HEADER_BG + widgetID)
            editor.remove(CalendarWidgetConfigActivity.CALENDAR_WIDGET_MONTH + widgetID)
            editor.remove(CalendarWidgetConfigActivity.CALENDAR_WIDGET_YEAR + widgetID)
        }
        editor.apply()
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager,
                         sp: SharedPreferences, widgetID: Int) {
            val cal = GregorianCalendar()
            val month = sp.getInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_MONTH + widgetID, 0)
            val year = sp.getInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_YEAR + widgetID, 0)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.YEAR, year)
            val monthYearStringBuilder = StringBuilder(50)
            val monthYearFormatter = Formatter(
                    monthYearStringBuilder, Locale.getDefault())
            val monthYearFlag = (DateUtils.FORMAT_SHOW_DATE
                    or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR)
            val date = DateUtils.formatDateRange(context,
                    monthYearFormatter, cal.timeInMillis, cal.timeInMillis, monthYearFlag)
                    .toString().toUpperCase()

            val headerBgColor = sp.getInt(CalendarWidgetConfigActivity.WIDGET_HEADER_BG + widgetID, 0)
            val bgColor = sp.getInt(CalendarWidgetConfigActivity.WIDGET_BG + widgetID, 0)

            val rv = RemoteViews(context.packageName, R.layout.widget_calendar)

            rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))
            rv.setInt(R.id.widgetBg, "setBackgroundResource", WidgetUtils.newWidgetBg(bgColor))

            rv.setTextViewText(R.id.widgetTitle, date)

            if (WidgetUtils.isDarkBg(headerBgColor)) {
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_white, R.id.btn_settings, CalendarWidgetConfigActivity::class.java) {
                    it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
                    return@initButton it
                }
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_white, R.id.btn_add_task, CreateReminderActivity::class.java)
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_mic_white, R.id.btn_voice, VoiceWidgetDialog::class.java)

                WidgetUtils.setIcon(context, rv, R.drawable.ic_twotone_keyboard_arrow_left_24px, R.id.btn_prev, R.color.pureWhite)
                WidgetUtils.setIcon(context, rv, R.drawable.ic_twotone_keyboard_arrow_right_24px, R.id.btn_next, R.color.pureWhite)

                rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
            } else {
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_settings_24px, R.id.btn_settings, CalendarWidgetConfigActivity::class.java) {
                    it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
                    return@initButton it
                }
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_add_24px, R.id.btn_add_task, CreateReminderActivity::class.java)
                WidgetUtils.initButton(context, rv, R.drawable.ic_twotone_mic_24px, R.id.btn_voice, VoiceWidgetDialog::class.java)

                WidgetUtils.setIcon(context, rv, R.drawable.ic_twotone_keyboard_arrow_left_24px, R.id.btn_prev, R.color.pureBlack)
                WidgetUtils.setIcon(context, rv, R.drawable.ic_twotone_keyboard_arrow_right_24px, R.id.btn_next, R.color.pureBlack)

                rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
            }

            val weekdayAdapter = Intent(context, CalendarWeekdayService::class.java)
            weekdayAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(R.id.weekdayGrid, weekdayAdapter)

            val startActivityIntent = Intent(context, MainActivity::class.java)
            startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_calendar)
            val startActivityPendingIntent = PendingIntent.getActivity(context, 0,
                    startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.monthGrid, startActivityPendingIntent)

            val monthAdapter = Intent(context, CalendarMonthService::class.java)
            monthAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            rv.setRemoteAdapter(R.id.monthGrid, monthAdapter)

            var serviceIntent = Intent(context, CalendarUpdateService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            serviceIntent.putExtra("actionPlus", 2)
            var servicePendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.btn_next, servicePendingIntent)

            serviceIntent = Intent(context, CalendarUpdateMinusService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            serviceIntent.putExtra("actionMinus", 1)
            servicePendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.btn_prev, servicePendingIntent)

            appWidgetManager.updateAppWidget(widgetID, rv)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.weekdayGrid)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.monthGrid)
        }
    }
}