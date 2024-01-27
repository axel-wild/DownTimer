package de.wildsolutions.downtimer.ui.settings

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import de.wildsolutions.downtimer.AppDatabase
import de.wildsolutions.downtimer.DataStoreManager
import de.wildsolutions.downtimer.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Calendar

/**
 *
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
 */

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private lateinit var dataStore : DataStoreManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val calStart = Calendar.getInstance()
        val calEnd = Calendar.getInstance()

        dataStore = DataStoreManager.getInstance(this.requireContext())

        val textView : TextView = binding.textSettings
        val dateStart = LocalDate.ofEpochDay(dataStore.getDateStart())
        val dateEnd = LocalDate.ofEpochDay(dataStore.getDateEnd())

        settingsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.inCreditHours.setText(dataStore.getCreditHours().toString())
        binding.inFreeDays.setText(dataStore.getFreeDays().toString())
        binding.dateStart.init(dateStart.year, dateStart.monthValue-1, dateStart.dayOfMonth, null)
        //binding.dateStart.init(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH), null)
        binding.dateEnd.init(dateEnd.year, dateEnd.monthValue-1, dateEnd.dayOfMonth, null)

        binding.btnCommit.setOnClickListener(){
            lifecycleScope.launch {
                dataStore.setCreditHours(binding.inCreditHours.text.toString().toFloat())
                dataStore.setFreeDays(binding.inFreeDays.text.toString().toInt())
                dataStore.setDateStart(LocalDate.of(binding.dateStart.year, binding.dateStart.month+1, binding.dateStart.dayOfMonth).toEpochDay())
                dataStore.setDateEnd(LocalDate.of(binding.dateEnd.year, binding.dateEnd.month+1, binding.dateEnd.dayOfMonth).toEpochDay())
            }
        }

        binding.btnReset.setOnClickListener(){
            confirm()
        }
        return root
    }


    private fun confirm(){
        val alertDialogBuilder = AlertDialog.Builder(this@SettingsFragment.requireContext())
        alertDialogBuilder.setTitle("")
        alertDialogBuilder.setMessage("Alle Daten lösche?")
        alertDialogBuilder.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
            clearData()
        }
        alertDialogBuilder.setNegativeButton("Abbrechen", { dialogInterface: DialogInterface, i: Int -> })
        alertDialog = alertDialogBuilder.create()
        alertDialog?.show()
    }

    private fun clearData(){
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(requireContext()).timingDao().truncate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}