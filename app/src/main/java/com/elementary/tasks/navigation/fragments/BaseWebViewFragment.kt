package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

import com.elementary.tasks.databinding.FragmentSettingsWebViewBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

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

abstract class BaseWebViewFragment : BaseSettingsFragment() {

    private var binding: FragmentSettingsWebViewBinding? = null

    protected val webView: WebView
        get() = binding!!.webView

    protected abstract val url: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsWebViewBinding.inflate(inflater, container, false)
        setExtraParams(binding!!.webView)
        binding!!.webView.loadUrl(url)
        return binding!!.root
    }

    protected open fun setExtraParams(webView: WebView) {

    }
}