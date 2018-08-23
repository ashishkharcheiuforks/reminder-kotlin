package com.elementary.tasks.core.viewModels.googleTasks

import android.app.Application
import androidx.lifecycle.LiveData
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.viewModels.Commands
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.experimental.CommonPool
import com.elementary.tasks.core.utils.temp.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.IOException
import java.util.*

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
class GoogleTaskListsViewModel(application: Application) : BaseTaskListsViewModel(application) {

    var googleTaskLists: LiveData<List<GoogleTaskList>>

    init {
        googleTaskLists = appDb.googleTaskListsDao().loadAll()
    }

    fun sync() {
        val mGoogle = Google.getInstance()
        if (mGoogle?.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        launch(CommonPool) {
            var lists: TaskLists? = null
            try {
                lists = mGoogle.tasks?.taskLists
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (lists != null && lists.size > 0 && lists.items != null) {
                for (item in lists.items) {
                    val listId = item.id
                    var taskList = appDb.googleTaskListsDao().getById(listId)
                    if (taskList != null) {
                        taskList.update(item)
                    } else {
                        val r = Random()
                        val color = r.nextInt(15)
                        taskList = GoogleTaskList(item, color)
                    }
                    appDb.googleTaskListsDao().insert(taskList)
                    val tasks = mGoogle.tasks?.getTasks(listId)
                    if (tasks == null || tasks.isEmpty()) {
                        withContext(UI) {
                            isInProgress.postValue(false)
                            result.postValue(Commands.UPDATED)
                            updatesHelper.updateTasksWidget()
                        }
                    } else {
                        val googleTasks = ArrayList<GoogleTask>()
                        for (task in tasks) {
                            var googleTask = appDb.googleTasksDao().getById(task.id)
                            if (googleTask != null) {
                                googleTask.listId = listId
                                googleTask.update(task)
                            } else {
                                googleTask = GoogleTask(task, listId)
                            }
                            googleTasks.add(googleTask)
                        }
                        appDb.googleTasksDao().insertAll(googleTasks)
                        withContext(UI) {
                            isInProgress.postValue(false)
                            result.postValue(Commands.UPDATED)
                            updatesHelper.updateTasksWidget()
                        }
                    }
                }
            }
        }
    }

    fun reload() {

    }

    fun clearList(googleTaskList: GoogleTaskList) {
        val mGoogle = Google.getInstance()
        if (mGoogle?.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
        } else {
            isInProgress.postValue(true)
            launch(CommonPool) {
                val googleTasks = appDb.googleTasksDao().getAllByList(googleTaskList.listId, Google.TASKS_COMPLETE)
                appDb.googleTasksDao().deleteAll(googleTasks)
                mGoogle.tasks!!.clearTaskList(googleTaskList.listId)
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.UPDATED)
                    updatesHelper.updateTasksWidget()
                }
            }
        }
    }
}
