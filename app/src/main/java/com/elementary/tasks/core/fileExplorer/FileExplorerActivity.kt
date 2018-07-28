package com.elementary.tasks.core.fileExplorer

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.activity_file_explorer.*
import java.io.File
import java.io.FilenameFilter
import java.util.*

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
class FileExplorerActivity : ThemedActivity() {

    private val str = ArrayList<String>()
    private var firstLvl: Boolean = true
    private var mFilter: Boolean = false

    private var mDataList: MutableList<FileDataItem> = mutableListOf()
    private var path = File(Environment.getExternalStorageDirectory().toString() + "")
    private var mFileName: String = ""
    private var mFilePath: String = ""

    private var filType: String = ""

    private val mAdapter: FileRecyclerAdapter = FileRecyclerAdapter()
    private var mSound: Sound? = null

    private val directoryIcon: Int
        get() = if (isDark) R.drawable.ic_folder_white_24dp else R.drawable.ic_folder_black_24dp

    private val undoIcon: Int
        get() = if (isDark) R.drawable.ic_undo_white_24dp else R.drawable.ic_undo_black_24dp

    private val mListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.fab -> saveChoice()
            R.id.playButton -> play()
            R.id.stopButton -> stop()
            R.id.pauseButton -> pause()
            R.id.clearButton -> searchField.setText("")
        }
    }

    private fun selectFile(position: Int) {
        val item = mAdapter.getItem(position)
        mFileName = item.fileName
        mFilePath = item.filePath
        val sel = File(path.toString() + "/" + mFileName)
        if (sel.isDirectory) {
            firstLvl = false
            str.add(mFileName)
            mDataList.clear()
            path = File(sel.toString() + "")
            loadFileList()
            loadList()
        } else if (mFileName.equals(getString(R.string.up), ignoreCase = true) && !sel.exists()) {
            moveUp()
        } else {
            if (filType.matches("any".toRegex())) {
                sendFile()
            } else if (filType == TYPE_PHOTO) {
                if (isImage(mFileName)) {
                    showFullImage()
                } else {
                    Toast.makeText(this, R.string.not_a_image_file, Toast.LENGTH_SHORT).show()
                }
            } else {
                if (isMelody(mFileName)) {
                    play()
                } else {
                    Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFullImage() {
        val builder = dialogues.getDialog(this)
        builder.setTitle(mFileName)
        val imageView = ImageView(this)
        val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, MeasureUtils.dp2px(this, 256))
        imageView.layoutParams = layoutParams
        Glide.with(this)
                .load(File(mFilePath))
                .into(imageView)
        builder.setView(imageView)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
            sendFile()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun moveUp() {
        val s = str.removeAt(str.size - 1)
        path = File(path.toString().substring(0, path.toString().lastIndexOf(s)))
        mDataList.clear()
        if (str.isEmpty()) {
            firstLvl = true
        }
        loadFileList()
        loadList()
    }

    private fun sendFile() {
        val intent = Intent()
        intent.putExtra(Constants.FILE_PICKED, mFilePath)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSound = Sound(this, prefs)
        setContentView(R.layout.activity_file_explorer)
        filType = intent.getStringExtra(Constants.FILE_TYPE)
        if (filType == "") filType = TYPE_MUSIC
        initActionBar()
        initRecyclerView()
        initPlayer()
        initSearch()
        initButtons()
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL)) {
            loadFileList()
            loadList()
        } else {
            Permissions.requestPermission(this, SD_CARD, Permissions.READ_EXTERNAL)
        }
    }

    private fun initPlayer() {
        playerLayout.visibility = View.GONE
    }

    private fun initRecyclerView() {
        mAdapter.filterCallback = { recyclerView.scrollToPosition(0) }
        mAdapter.clickListener = { this.selectFile(it) }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    private fun initSearch() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (mFilter) mAdapter.filter(s.toString(), mDataList)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun initButtons() {
        fab.setOnClickListener(mListener)
        pauseButton.setOnClickListener(mListener)
        stopButton.setOnClickListener(mListener)
        playButton.setOnClickListener(mListener)
        clearButton.setOnClickListener(mListener)
    }

    private fun loadList() {
        if (mDataList.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_files), Toast.LENGTH_SHORT).show()
            finish()
        }
        recyclerView.adapter = mAdapter
    }

    private fun play() {
        if (!mSound!!.isPlaying) {
            if (playerLayout.visibility == View.GONE) {
                ViewUtils.expand(playerLayout)
            }
            if (mSound!!.isPaused && mSound!!.isSameFile(mFilePath)) {
                mSound!!.resume()
            } else {
                mSound!!.play(mFilePath)
                currentMelody.text = mFileName
            }
        } else {
            if (mSound!!.isSameFile(mFilePath)) {
                return
            }
            mSound!!.play(mFilePath)
            currentMelody.text = mFileName
        }
    }

    private fun pause() {
        if (mSound!!.isPlaying) {
            mSound!!.pause()
        }
    }

    private fun stop() {
        if (mSound!!.isPlaying) {
            mSound!!.stop(true)
        }
        ViewUtils.collapse(playerLayout)
    }

    private fun loadFileList() {
        try {
            path.mkdirs()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        mFilter = false
        searchField.setText("")
        mFilter = true
        if (path.exists()) {
            createFilteredFileList()
        }
        mAdapter.setData(mDataList)
    }

    private fun createFilteredFileList() {
        val filter = FilenameFilter { dir, filename ->
            val sel = File(dir, filename)
            (sel.isFile || sel.isDirectory) && !sel.isHidden
        }

        val list = try {
            Arrays.asList(*path.list(filter))
        } catch (e: NullPointerException) {
            arrayListOf<String>()
        }

        list.sort()
        mDataList = ArrayList(list.size)
        for (i in list.indices) {
            val fileName = list[i]
            val sel = File(path, fileName)
            mDataList.add(i, FileDataItem(fileName, 0, sel.toString()))

            if (sel.isDirectory) {
                mDataList[i].icon = directoryIcon
            }
        }

        if ((!firstLvl)) {
            addUpItem()
        }
    }

    private fun addUpItem() {
        val temp = ArrayList<FileDataItem>(mDataList.size + 1)
        temp.add(0, FileDataItem(getString(R.string.up), undoIcon, ""))
        temp.addAll(mDataList)
        mDataList = temp
    }

    private fun isMelody(file: String?): Boolean {
        return file != null && (file.endsWith(".mp3") || file.endsWith(".ogg")
                || file.endsWith(".m4a") || file.endsWith(".flac"))
    }

    private fun isImage(file: String?): Boolean {
        return file != null && (file.endsWith(".jpg") || file.endsWith(".jpeg")
                || file.endsWith(".png") || file.endsWith(".tiff"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                exit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if ((!firstLvl)) {
            moveUp()
        } else {
            exit()
        }
    }

    private fun exit() {
        if (isMelody(mFileName)) {
            stop()
        }
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun saveChoice() {
        if (isMelody(mFileName)) {
            stop()
            sendFile()
        } else {
            Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            SD_CARD -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFileList()
                loadList()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    companion object {

        private const val SD_CARD = 444
        const val TYPE_MUSIC = "music"
        const val TYPE_PHOTO = "photo"
    }
}
