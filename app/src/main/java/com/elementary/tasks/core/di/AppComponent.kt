package com.elementary.tasks.core.di

import com.elementary.tasks.birthdays.BirthdayHolder
import com.elementary.tasks.birthdays.EventsDataProvider
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.additional.SelectableTemplatesAdapter
import com.elementary.tasks.core.appWidgets.WidgetDataProvider
import com.elementary.tasks.core.appWidgets.calendar.CalendarMonthFactory
import com.elementary.tasks.core.appWidgets.calendar.CalendarUpdateMinusService
import com.elementary.tasks.core.appWidgets.calendar.CalendarUpdateService
import com.elementary.tasks.core.appWidgets.calendar.CalendarWeekdayFactory
import com.elementary.tasks.core.appWidgets.notes.NotesFactory
import com.elementary.tasks.core.appWidgets.tasks.TasksFactory
import com.elementary.tasks.core.async.BackupSettingTask
import com.elementary.tasks.core.async.BackupTask
import com.elementary.tasks.core.async.SyncTask
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.contacts.ContactsFragment
import com.elementary.tasks.core.contacts.ContactsRecyclerAdapter
import com.elementary.tasks.core.contacts.calls.CallsRecyclerAdapter
import com.elementary.tasks.core.controller.EventManager
import com.elementary.tasks.core.dialogs.BaseDialog
import com.elementary.tasks.core.fileExplorer.FileRecyclerAdapter
import com.elementary.tasks.core.fragments.BaseMapFragment
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.services.*
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.navigation.settings.images.ImagesRecyclerAdapter
import com.elementary.tasks.places.google.GooglePlacesAdapter
import com.elementary.tasks.reminder.lists.ReminderHolder
import com.elementary.tasks.reminder.lists.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.ShoppingHolder
import com.elementary.tasks.voice.ConversationAdapter

import javax.inject.Singleton

import dagger.Component

/**
 * Copyright 2018 Nazar Suhovich
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
@Singleton
@Component(modules = [
    AppModule::class,
    DbModule::class,
    UtilModule::class
])
interface AppComponent {
    fun inject(viewModel: BaseDbViewModel)
    fun inject(adapter: RemindersRecyclerAdapter)
    fun inject(eventManager: EventManager)
    fun inject(callReceiver: CallReceiver)
    fun inject(conversationAdapter: ConversationAdapter)
    fun inject(reminderHolder: ReminderHolder)
    fun inject(shoppingHolder: ShoppingHolder)
    fun inject(google: Google)
    fun inject(backupTool: BackupTool)
    fun inject(adapter: SelectableTemplatesAdapter)
    fun inject(baseDialog: BaseDialog)
    fun inject(googlePlacesAdapter: GooglePlacesAdapter)
    fun inject(adapter: ImagesRecyclerAdapter)
    fun inject(themedActivity: ThemedActivity)
    fun inject(repeatNotificationReceiver: RepeatNotificationReceiver)
    fun inject(reminderUtils: ReminderUtils)
    fun inject(birthdayHolder: BirthdayHolder)
    fun inject(baseFragment: BaseFragment)
    fun inject(calendarMonthFactory: CalendarMonthFactory)
    fun inject(eventsDataProvider: EventsDataProvider)
    fun inject(widgetDataProvider: WidgetDataProvider)
    fun inject(calendarUpdateMinusService: CalendarUpdateMinusService)
    fun inject(calendarUpdateService: CalendarUpdateService)
    fun inject(calendarWeekdayFactory: CalendarWeekdayFactory)
    fun inject(syncTask: SyncTask)
    fun inject(backupSettingTask: BackupSettingTask)
    fun inject(backupTask: BackupTask)
    fun inject(notesFactory: NotesFactory)
    fun inject(tasksFactory: TasksFactory)
    fun inject(dropbox: Dropbox)
    fun inject(contactsRecyclerAdapter: ContactsRecyclerAdapter)
    fun inject(callsRecyclerAdapter: CallsRecyclerAdapter)
    fun inject(contactsFragment: ContactsFragment)
    fun inject(locationTracker: LocationTracker)
    fun inject(baseMapFragment: BaseMapFragment)
    fun inject(fileRecyclerAdapter: FileRecyclerAdapter)
    fun inject(baseBroadcast: BaseBroadcast)
    fun inject(geolocationService: GeolocationService)
    fun inject(eventJobService: EventJobService)
}
