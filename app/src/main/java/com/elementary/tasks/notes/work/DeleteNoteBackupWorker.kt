package com.elementary.tasks.notes.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.converters.NoteConverter
import com.elementary.tasks.core.cloud.repositories.NoteRepository
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault

class DeleteNoteBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            launchDefault {
                DataFlow(NoteRepository(), NoteConverter(),
                        CompositeStorage(DataFlow.availableStorageList(applicationContext)), null)
                        .delete(uuId, IndexTypes.TYPE_NOTE, true)
            }
        }
        return Result.success()
    }
}