package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.reminders.ActiveRemindersViewModel
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.SearchModifier
import timber.log.Timber

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
class RemindersFragment : BaseNavigationFragment<FragmentRemindersBinding>(), (List<Reminder>) -> Unit {

    private lateinit var viewModel: ActiveRemindersViewModel

    private val reminderResolver = ReminderResolver(
            dialogAction = { return@ReminderResolver dialogues },
            saveAction = { reminder -> viewModel.saveReminder(reminder) },
            toggleAction = { reminder ->
                if (Reminder.isGpsType(reminder.type)) {
                    if (Permissions.ensureForeground(activity!!, 1122)) {
                        viewModel.toggleReminder(reminder)
                    }
                } else {
                    viewModel.toggleReminder(reminder)
                }
            },
            deleteAction = { reminder -> viewModel.moveToTrash(reminder) },
            skipAction = { reminder -> viewModel.skip(reminder) },
            allGroups = { return@ReminderResolver viewModel.groups }
    )

    private val mAdapter = RemindersRecyclerAdapter()
    private val searchModifier = SearchModifier(null, this)

    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchModifier.setSearchValue(query)
            if (mSearchMenu != null) {
                mSearchMenu?.collapseActionView()
            }
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            searchModifier.setSearchValue(newText)
            return false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_active_menu, menu)

        ViewUtils.tintMenuIcon(context!!, menu, 0, R.drawable.ic_twotone_search_24px, isDark)
        if (Module.hasMicrophone(context!!)) {
            menu.getItem(1)?.isVisible = true
            ViewUtils.tintMenuIcon(context!!, menu, 1, R.drawable.ic_twotone_mic_24px, isDark)
        } else {
            menu.getItem(1)?.isVisible = false
        }

        mSearchMenu = menu.findItem(R.id.action_search)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (mSearchMenu != null) {
            mSearchView = mSearchMenu?.actionView as SearchView?
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            }
            mSearchView?.setOnQueryTextListener(queryTextListener)
        }
        val isNotEmpty = searchModifier.hasOriginal()
        menu.getItem(0)?.isVisible = isNotEmpty

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_voice -> if (callback != null) {
                buttonObservable.fireAction(view!!, GlobalButtonObservable.Action.VOICE)
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_reminders

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener { CreateReminderActivity.openLogged(context!!) }
        binding.fab.setOnLongClickListener {
            buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
            true
        }
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ActiveRemindersViewModel::class.java)
        viewModel.events.observe(this, Observer { reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
        viewModel.error.observe(this, Observer {
            Timber.d("initViewModel: onError -> $it")
            if (it != null) {
                mAdapter.notifyDataSetChanged()
                Toast.makeText(context!!, it, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showData(result: List<Reminder>) {
        searchModifier.original = result.toMutableList()
        activity?.invalidateOptionsMenu()
    }

    private fun initList() {
        mAdapter.prefsProvider = { prefs }
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                if (t != null) {
                    reminderResolver.resolveAction(view, t, actions)
                }
            }
        }
        if (prefs.isTwoColsEnabled && ViewUtils.isHorizontal(context!!) && resources.getBoolean(R.bool.is_tablet)) {
            binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
        }
        binding.recyclerView.adapter = mAdapter
        ViewUtils.listenScrollableView(binding.recyclerView, listener = { setScroll(it) }) {
            if (it) binding.fab.show()
            else binding.fab.hide()
        }
        reloadView(0)
    }

    override fun getTitle(): String = getString(R.string.tasks)

    private fun reloadView(count: Int) {
        if (count > 0) {
            binding.emptyItem.visibility = View.GONE
        } else {
            binding.emptyItem.visibility = View.VISIBLE
        }
    }

    override fun invoke(result: List<Reminder>) {
        mAdapter.submitList(result)
        binding.recyclerView.smoothScrollToPosition(0)
        reloadView(result.size)
    }
}