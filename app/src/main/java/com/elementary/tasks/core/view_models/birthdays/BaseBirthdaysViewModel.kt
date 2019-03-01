package com.elementary.tasks.core.view_models.birthdays

import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands

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
abstract class BaseBirthdaysViewModel : BaseDbViewModel() {

    fun deleteBirthday(birthday: Birthday) {
        postInProgress(true)
        launchDefault {
            appDb.birthdaysDao().delete(birthday)
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
            }
        }
    }

    fun saveBirthday(birthday: Birthday) {
        postInProgress(true)
        launchDefault {
            appDb.birthdaysDao().insert(birthday)
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }
}