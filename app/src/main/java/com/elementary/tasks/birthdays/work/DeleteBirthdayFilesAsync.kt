package com.elementary.tasks.birthdays.work

import android.content.Context
import android.os.AsyncTask

import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.SuperUtil

import java.io.File
import java.io.IOException

/**
 * Copyright 2017 Nazar Suhovich
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

class DeleteBirthdayFilesAsync(private val mContext: Context) : AsyncTask<String, Void, Void>() {

    override fun doInBackground(vararg params: String): Void? {
        for (uid in params) {
            val exportFileName = uid + FileConfig.FILE_NAME_BIRTHDAY
            var dir = MemoryUtil.birthdaysDir
            if (dir != null) {
                val file = File(dir, exportFileName)
                if (file.exists()) file.delete()
            }
            dir = MemoryUtil.dropboxBirthdaysDir
            if (dir != null) {
                val file = File(dir, exportFileName)
                if (file.exists()) file.delete()
            }
            dir = MemoryUtil.googleBirthdaysDir
            if (dir != null) {
                val file = File(dir, exportFileName)
                if (file.exists()) file.delete()
            }
            val isConnected = SuperUtil.isConnected(mContext)
            if (isConnected) {
                Dropbox().deleteBirthday(exportFileName)
                val google = Google.getInstance()
                if (google?.drive != null) {
                    try {
                        google.drive!!.deleteBirthdayFileByName(exportFileName)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        return null
    }
}
