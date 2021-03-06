package com.elementary.tasks.core.view_models.day_view

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.day_view.DayViewProvider
import com.elementary.tasks.day_view.EventsPagerItem
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.reminder.work.SingleBackupWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*

class DayViewViewModel private constructor(private val calculateFuture: Boolean,
                                           private val birthTime: Long = 0) : BaseDbViewModel() {

    private var liveData: DayViewLiveData

    private var _events: MutableLiveData<Pair<EventsPagerItem, List<EventModel>>> = MutableLiveData()
    var events: LiveData<Pair<EventsPagerItem, List<EventModel>>> = _events

    private var _groups: MutableList<ReminderGroup> = mutableListOf()
    val groups: List<ReminderGroup>
        get() = _groups

    init {
        appDb.reminderGroupDao().loadAll().observeForever{
            if (it != null) {
                _groups.clear()
                _groups.addAll(it)
            }
        }
        liveData = DayViewLiveData()
    }

    fun findEvents(item: EventsPagerItem) {
        try {
            liveData.findEvents(item, true) { eventsPagerItem, list ->
                _events.postValue(Pair(eventsPagerItem, list))
            }
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

    fun saveReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            appDb.reminderDao().insert(reminder)
            postInProgress(false)
            postCommand(Commands.SAVED)
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
        }
    }

    fun deleteBirthday(birthday: Birthday) {
        postInProgress(true)
        launchDefault {
            appDb.birthdaysDao().delete(birthday)
            postInProgress(false)
            postCommand(Commands.DELETED)
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
        }
    }

    fun moveToTrash(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                val fromDb = appDb.reminderDao().getById(reminder.uuId)
                if (fromDb != null) {
                    fromDb.isRemoved = true
                    EventControlFactory.getController(fromDb).stop()
                    appDb.reminderDao().insert(fromDb)
                }
            }
            postInProgress(false)
            postCommand(Commands.DELETED)
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
        }
    }

    fun skip(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                val fromDb = appDb.reminderDao().getById(reminder.uuId)
                if (fromDb != null) {
                    EventControlFactory.getController(fromDb).skip()
                }
            }
            postInProgress(false)
            postCommand(Commands.DELETED)
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
        }
    }

    private inner class DayViewLiveData internal constructor() : LiveData<Pair<EventsPagerItem, List<EventModel>>>() {

        private val reminderData = ArrayList<EventModel>()
        private val birthdayData = ArrayList<EventModel>()
        private val birthdays = appDb.birthdaysDao().loadAll()
        private val reminders = appDb.reminderDao().loadType(active = true, removed = false)

        private var eventsPagerItem: EventsPagerItem? = null
        private var job: Job? = null
        private var listener: ((EventsPagerItem, List<EventModel>) -> Unit)? = null
        private var sort = false

        private val birthdayObserver: Observer<in List<Birthday>> = Observer {
            Timber.d("birthdaysChanged: ")
            launchDefault {
                if (it != null) {
                    birthdayData.clear()
                    birthdayData.addAll(DayViewProvider.loadBirthdays(birthTime, it))
                    repeatSearch()
                }
            }
        }
        private val reminderObserver: Observer<in List<Reminder>> = Observer {
            Timber.d("remindersChanged: ")
            launchDefault {
                if (it != null) {
                    reminderData.clear()
                    reminderData.addAll(DayViewProvider.loadReminders(calculateFuture, it))
                    repeatSearch()
                }
            }
        }

        init {
            birthdays.observeForever(birthdayObserver)
            reminders.observeForever(reminderObserver)
        }

        fun findEvents(eventsPagerItem: EventsPagerItem, sort: Boolean, listener: ((EventsPagerItem, List<EventModel>) -> Unit)?) {
            if (listener == null) return
            this.listener = listener
            this.eventsPagerItem = eventsPagerItem
            this.sort = sort
            val toSearch = mutableListOf<EventModel>()
            toSearch.addAll(birthdayData)
            toSearch.addAll(reminderData)
            findMatches(toSearch, eventsPagerItem, sort)
        }

        override fun onInactive() {
            super.onInactive()
            Timber.d("onInactive: ")
            birthdays.observeForever(birthdayObserver)
            reminders.observeForever(reminderObserver)
            this.eventsPagerItem = null
        }

        override fun onActive() {
            super.onActive()
            Timber.d("onActive: ")
            birthdays.removeObserver(birthdayObserver)
            reminders.removeObserver(reminderObserver)
        }

        private fun notifyObserver(eventsPagerItem: EventsPagerItem, list: List<EventModel>) {
            listener?.invoke(eventsPagerItem, list)
        }

        private fun repeatSearch() {
            val item = eventsPagerItem ?: return
            findEvents(item, this.sort, listener)
        }

        private fun findMatches(list: List<EventModel>, eventsPagerItem: EventsPagerItem, sort: Boolean) {
            this.job?.cancel()
            this.job = launchDefault {
                val res = ArrayList<EventModel>()
                Timber.d("Search events: $eventsPagerItem")
                for (item in list) {
                    val mDay = item.day
                    val mMonth = item.month
                    val mYear = item.year
                    val type = item.viewType
                    if (type == EventModel.BIRTHDAY && mDay == eventsPagerItem.day && mMonth == eventsPagerItem.month) {
                        res.add(item)
                    } else {
                        if (mDay == eventsPagerItem.day && mMonth == eventsPagerItem.month && mYear == eventsPagerItem.year) {
                            res.add(item)
                        }
                    }
                }
                Timber.d("Search events: found -> %d", res.size)
                if (!sort) {
                    withUIContext { notifyObserver(eventsPagerItem, res) }
                } else {
                    val sorted = try {
                        res.asSequence().sortedBy { it.getMillis(birthTime) }.toList()
                    } catch (e: IllegalArgumentException) {
                        res
                    }
                    withUIContext { notifyObserver(eventsPagerItem, sorted) }
                }
            }
        }
    }

    class Factory(private val calculateFuture: Boolean,
                  private val birthTime: Long = 0) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DayViewViewModel(calculateFuture, birthTime) as T
        }
    }
}
