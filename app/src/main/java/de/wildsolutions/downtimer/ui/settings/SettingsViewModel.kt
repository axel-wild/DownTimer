package de.wildsolutions.downtimer.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 *
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
 */

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Einstellungen"
    }
    val text: LiveData<String> = _text
}