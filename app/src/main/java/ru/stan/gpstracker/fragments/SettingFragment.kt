package ru.stan.gpstracker.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.stan.gpstracker.R
import ru.stan.gpstracker.utils.showToast

class SettingFragment : PreferenceFragmentCompat() {
    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
        initPrefs()
    }

    private fun init() {
        timePref = findPreference("update_time_key")!!
        colorPref = findPreference("color_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
    }

    private fun onChangeListener(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { pref, value ->
            when (pref.key) {
                "update_time_key" -> onTimeChange(value.toString())
                "color_key" -> {
                    pref.icon?.setTint(Color.parseColor(value.toString()))
                    showToast("Выбрали цвет: ${getColorName(value.toString())}")
                }
            }
            true
        }
    }

    private fun getColorName(colorValue: String): String {
        return when (colorValue) {
            "#303F9F" -> "Blue"
            "#D32F2F" -> "Red"
            "#FF000000" -> "Black"
            else -> "Неизвестный цвет"
        }
    }



    private fun onTimeChange(value: String) {
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title.toString().substringBefore(":")
        val pos = valueArray.indexOf(value)
        timePref.title = "$title: ${nameArray[pos]}"
        showToast("Value changed: ${nameArray[pos]}")
    }

    private fun initPrefs() {
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title
        val pos = valueArray.indexOf(pref?.getString("update_time_key", "3000"))
        timePref.title = "$title: ${nameArray[pos]}"


        val trackColor = pref?.getString("color_key", "#D32F2F")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }
}