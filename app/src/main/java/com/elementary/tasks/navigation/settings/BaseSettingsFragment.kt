package com.elementary.tasks.navigation.settings

import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
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
abstract class BaseSettingsFragment : BaseNavigationFragment() {

    @Inject
    lateinit var language: Language

    init {
        ReminderApp.appComponent.inject(this)
    }

    protected fun priorityList(): Array<String> {
        return arrayOf(
                getString(R.string.priority_lowest),
                getString(R.string.priority_low),
                getString(R.string.priority_normal),
                getString(R.string.priority_high),
                getString(R.string.priority_highest)
        )
    }
}
