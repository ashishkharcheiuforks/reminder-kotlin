package com.elementary.tasks.birthdays.create

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.birthdays.BirthdayViewModel
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import kotlinx.android.synthetic.main.activity_add_birthday.*
import java.text.ParseException
import java.util.*
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
class AddBirthdayActivity : ThemedActivity() {

    private lateinit var viewModel: BirthdayViewModel

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var number: String = ""
    private var mBirthday: Birthday? = null
    private var date: Long = 0
    private var mIsLogged = false

    @Inject
    lateinit var backupTool: BackupTool

    private var mDateCallBack: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        birthDate.text = createBirthDate(mDay, mMonth, mYear)
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIsLogged = intent.getBooleanExtra(ARG_LOGGED, false)
        setContentView(R.layout.activity_add_birthday)
        initActionBar()
        container.visibility = View.GONE
        contactCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                container.visibility = View.VISIBLE
            else
                container.visibility = View.GONE
        }

        if (prefs.isTelephonyAllowed) {
            contactCheck.visibility = View.VISIBLE
        } else {
            contactCheck.visibility = View.GONE
        }

        birthDate.setOnClickListener { dateDialog() }
        pickContact.setOnClickListener { pickContact() }

        loadBirthday()
        if (prefs.hasPinCode && !mIsLogged) {
            PinLoginActivity.verify(this, PinLoginActivity.REQ_CODE)
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
    }

    private fun showBirthday(birthday: Birthday?) {
        this.mBirthday = birthday

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        toolbar.setTitle(R.string.add_birthday)
        if (birthday != null) {
            birthName.setText(birthday.name)
            try {
                val dt = TimeUtil.BIRTH_DATE_FORMAT.parse(birthday.date)
                if (dt != null) calendar.time = dt
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (!TextUtils.isEmpty(birthday.number)) {
                numberView.setText(birthday.number)
                contactCheck.isChecked = true
            }
            toolbar.setTitle(R.string.edit_birthday)
            this.number = birthday.number
        } else if (date != 0L) {
            calendar.timeInMillis = date
        }
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        birthDate.text = TimeUtil.BIRTH_DATE_FORMAT.format(calendar.time)
    }

    private fun loadBirthday() {
        date = intent.getLongExtra(Constants.INTENT_DATE, 0)
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        if (intent.data != null) {
            try {
                val name = intent.data ?: return
                val scheme = name.scheme
                mBirthday = if (ContentResolver.SCHEME_CONTENT != scheme) {
                    backupTool.getBirthday(name.path, null)
                } else null
                showBirthday(mBirthday)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
            try {
                mBirthday = intent.getSerializableExtra(Constants.INTENT_ITEM) as Birthday?
                showBirthday(mBirthday)
            } catch (e: Exception) {
            }
        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, BirthdayViewModel.Factory(application, id)).get(BirthdayViewModel::class.java)
        viewModel.birthday.observe(this, Observer<Birthday> { this.showBirthday(it) })
        viewModel.result.observe(this, Observer<Commands> {
            if (it != null) {
                when (it) {
                    Commands.SAVED, Commands.DELETED -> closeScreen()
                    else -> {
                    }
                }
            }
        })
    }

    private fun checkContactPermission(code: Int): Boolean {
        if (!Permissions.ensurePermissions(this, code, Permissions.READ_CONTACTS)) {
            return false
        }
        return true
    }

    private fun pickContact() {
        if (!checkContactPermission(101)) {
            return
        }
        SuperUtil.selectContact(this, Constants.REQUEST_CODE_CONTACTS)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_simple_save_action, menu)
        if (mBirthday != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                saveBirthday()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            MENU_ITEM_DELETE -> {
                deleteItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveBirthday() {
        val contact = birthName.text.toString().trim()
        if (contact == "") {
            birthNameLayout.error = getString(R.string.must_be_not_empty)
            birthNameLayout.isErrorEnabled = true
            return
        }
        var contactId = 0L
        if (contactCheck.isChecked) {
            number = numberView.text.toString().trim()
            if (TextUtils.isEmpty(number)) {
                numberLayout.error = getString(R.string.you_dont_insert_number)
                numberLayout.isErrorEnabled = true
                return
            }
            if (!checkContactPermission(CONTACT_PERM)) {
                return
            }
            contactId = Contacts.getIdFromNumber(number, this)
        }
        var birthday = mBirthday
        if (birthday == null) {
            birthday = Birthday(contact, birthDate.text.toString().trim(), number, 0, contactId, mDay, mMonth)
        }
        birthday.name = contact
        birthday.contactId = contactId
        birthday.date = birthDate.text.toString()
        birthday.number = number
        birthday.day = mDay
        birthday.month = mMonth
        viewModel.saveBirthday(birthday)
    }

    private fun closeScreen() {
        sendBroadcast(Intent(this, PermanentBirthdayReceiver::class.java)
                .setAction(PermanentBirthdayReceiver.ACTION_SHOW))
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun deleteItem() {
        val birthday = mBirthday
        if (birthday != null) {
            viewModel.deleteBirthday(birthday)
        }
    }

    private fun dateDialog() {
        TimeUtil.showDatePicker(this, themeUtil.dialogStyle, prefs, mYear, mMonth, mDay, mDateCallBack)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == Activity.RESULT_OK) {
                val name = data?.getStringExtra(Constants.SELECTED_CONTACT_NAME)
                number = data?.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: ""
                if (birthName.text.toString().matches("".toRegex())) {
                    birthName.setText(name)
                }
                numberView.setText(number)
            }
        } else if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> if (Permissions.isAllGranted(grantResults)) {
                SuperUtil.selectContact(this@AddBirthdayActivity, Constants.REQUEST_CODE_CONTACTS)
            }
            CONTACT_PERM -> if (Permissions.isAllGranted(grantResults)) {
                saveBirthday()
            }
        }
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
        private const val CONTACT_PERM = 102
        private const val ARG_LOGGED = "arg_logged"

        fun openLogged(context: Context, intent: Intent? = null) {
            if (intent == null) {
                context.startActivity(Intent(context, AddBirthdayActivity::class.java)
                        .putExtra(ARG_LOGGED, true))
            } else {
                intent.putExtra(ARG_LOGGED, true)
                context.startActivity(intent)
            }
        }

        fun createBirthDate(day: Int, month: Int, year: Int): String {
            val monthStr: String = if (month < 9) {
                "0" + (month + 1)
            } else {
                (month + 1).toString()
            }
            val dayStr: String = if (day < 10) {
                "0$day"
            } else {
                day.toString()
            }
            return SuperUtil.appendString(year.toString(), "-", monthStr, "-", dayStr)
        }
    }
}