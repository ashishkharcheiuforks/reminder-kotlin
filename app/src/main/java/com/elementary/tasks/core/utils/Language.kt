package com.elementary.tasks.core.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics

import com.elementary.tasks.R

import java.util.ArrayList
import java.util.Collections
import java.util.Locale

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

class Language {

    /**
     * Holder locale for tts.
     *
     * @param context application context.
     * @param isBirth flag for birthdays.
     * @return Locale
     */
    fun getLocale(context: Context, isBirth: Boolean): Locale? {
        var res: Locale? = null
        val locale: String?
        if (isBirth) {
            locale = Prefs.getInstance(context).birthdayTtsLocale
        } else {
            locale = Prefs.getInstance(context).ttsLocale
        }
        if (locale == null) {
            return Locale.ENGLISH
        }
        when (locale) {
            ENGLISH -> res = Locale.ENGLISH
            FRENCH -> res = Locale.FRENCH
            GERMAN -> res = Locale.GERMAN
            JAPANESE -> res = Locale.JAPANESE
            ITALIAN -> res = Locale.ITALIAN
            KOREAN -> res = Locale.KOREAN
            POLISH -> res = Locale("pl", "")
            RUSSIAN -> res = Locale("ru", "")
            SPANISH -> res = Locale("es", "")
            UKRAINIAN -> res = Locale("uk", "")
        }
        return res
    }

    companion object {
        val ENGLISH = "en"
        val FRENCH = "fr"
        val GERMAN = "de"
        val ITALIAN = "it"
        val JAPANESE = "ja"
        val KOREAN = "ko"
        val POLISH = "pl"
        val RUSSIAN = "ru"
        val SPANISH = "es"
        val UKRAINIAN = "uk"

        val EN = "en-US"
        val RU = "ru-RU"
        val UK = "uk-UA"

        fun onAttach(context: Context): Context {
            return setLocale(context, getScreenLanguage(Prefs.getInstance(context).appLanguage))
        }

        fun setLocale(context: Context, locale: Locale): Context {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                updateResources(context, locale)
            } else updateResourcesLegacy(context, locale)
        }

        @TargetApi(Build.VERSION_CODES.N)
        private fun updateResources(context: Context, locale: Locale): Context {
            Locale.setDefault(locale)
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            return context.createConfigurationContext(configuration)
        }

        private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
            Locale.setDefault(locale)
            val resources = context.resources
            val configuration = resources.configuration
            configuration.locale = locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLayoutDirection(locale)
            }
            resources.updateConfiguration(configuration, resources.displayMetrics)
            return context
        }

        fun getLocalized(context: Context, id: Int): String {
            if (Module.isJellyMR1) {
                val configuration = Configuration(context.resources.configuration)
                configuration.setLocale(Locale(Language.getTextLanguage(Prefs.getInstance(context).voiceLocale)))
                return context.createConfigurationContext(configuration).resources.getString(id)
            } else {
                val standardResources = context.resources
                val assets = standardResources.assets
                val metrics = standardResources.displayMetrics
                val config = Configuration(standardResources.configuration)
                config.locale = Locale(Language.getTextLanguage(Prefs.getInstance(context).voiceLocale))
                val defaultResources = Resources(assets, metrics, config)
                return defaultResources.getString(id)
            }
        }

        fun getLanguages(context: Context): List<String> {
            val locales = ArrayList<String>()
            locales.add(context.getString(R.string.english) + " (" + EN + ")")
            locales.add(context.getString(R.string.russian) + " (" + RU + ")")
            locales.add(context.getString(R.string.ukrainian) + " (" + UK + ")")
            return locales
        }

        fun getScreenLanguage(code: Int): Locale {
            when (code) {
                0 -> return Locale.getDefault()
                1 -> return Locale.ENGLISH
                2 -> return Locale.GERMAN
                3 -> return Locale("es", "")
                4 -> return Locale.FRENCH
                5 -> return Locale.ITALIAN
                6 -> return Locale("pt", "")
                7 -> return Locale("pl", "")
                8 -> return Locale("cs", "")
                9 -> return Locale("ro", "")
                10 -> return Locale("tr", "")
                11 -> return Locale("uk", "")
                12 -> return Locale("ru", "")
                13 -> return Locale.JAPANESE
                14 -> return Locale.CHINESE
                15 -> return Locale("hi", "")
                else -> return Locale.getDefault()
            }
        }

        fun getTextLanguage(code: Int): String {
            when (code) {
                0 -> return ENGLISH
                1 -> return RUSSIAN
                2 -> return UKRAINIAN
                else -> return ENGLISH
            }
        }

        fun getLanguage(code: Int): String {
            when (code) {
                0 -> return EN
                1 -> return RU
                2 -> return UK
                else -> return EN
            }
        }

        fun getLocaleByPosition(position: Int): String {
            var locale = Language.ENGLISH
            if (position == 0) locale = Language.ENGLISH
            if (position == 1) locale = Language.FRENCH
            if (position == 2) locale = Language.GERMAN
            if (position == 3) locale = Language.ITALIAN
            if (position == 4) locale = Language.JAPANESE
            if (position == 5) locale = Language.KOREAN
            if (position == 6) locale = Language.POLISH
            if (position == 7) locale = Language.RUSSIAN
            if (position == 8) locale = Language.SPANISH
            if (position == 9 && Module.isJellyMR2) locale = Language.UKRAINIAN
            return locale
        }

        fun getLocalePosition(locale: String?): Int {
            if (locale == null) {
                return 0
            }
            var mItemSelect = 0
            if (locale.matches(Language.ENGLISH.toRegex())) {
                mItemSelect = 0
            } else if (locale.matches(Language.FRENCH.toRegex())) {
                mItemSelect = 1
            } else if (locale.matches(Language.GERMAN.toRegex())) {
                mItemSelect = 2
            } else if (locale.matches(Language.ITALIAN.toRegex())) {
                mItemSelect = 3
            } else if (locale.matches(Language.JAPANESE.toRegex())) {
                mItemSelect = 4
            } else if (locale.matches(Language.KOREAN.toRegex())) {
                mItemSelect = 5
            } else if (locale.matches(Language.POLISH.toRegex())) {
                mItemSelect = 6
            } else if (locale.matches(Language.RUSSIAN.toRegex())) {
                mItemSelect = 7
            } else if (locale.matches(Language.SPANISH.toRegex())) {
                mItemSelect = 8
            } else if (locale.matches(Language.UKRAINIAN.toRegex()) && Module.isJellyMR2) {
                mItemSelect = 9
            }
            return mItemSelect
        }

        fun getScreenLocaleName(context: Context): String {
            return context.resources.getStringArray(R.array.app_languages)[Prefs.getInstance(context).appLanguage]
        }

        fun getScreenLocaleNames(context: Context): List<String> {
            val names = ArrayList<String>()
            Collections.addAll(names, *context.resources.getStringArray(R.array.app_languages))
            return names
        }

        fun getLocaleNames(mContext: Context): List<String> {
            val names = ArrayList<String>()
            names.add(mContext.getString(R.string.english))
            names.add(mContext.getString(R.string.french))
            names.add(mContext.getString(R.string.german))
            names.add(mContext.getString(R.string.italian))
            names.add(mContext.getString(R.string.japanese))
            names.add(mContext.getString(R.string.korean))
            names.add(mContext.getString(R.string.polish))
            names.add(mContext.getString(R.string.russian))
            names.add(mContext.getString(R.string.spanish))
            if (Module.isJellyMR2) {
                names.add(mContext.getString(R.string.ukrainian))
            }
            return names
        }
    }
}