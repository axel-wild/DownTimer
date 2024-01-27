package de.wildsolutions.downtimer.ui.list

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.wildsolutions.downtimer.AppDatabase
import de.wildsolutions.downtimer.DataStoreManager
import de.wildsolutions.downtimer.Timing
import de.wildsolutions.downtimer.TimingDao
import de.wildsolutions.downtimer.databinding.FragmentListBinding
import de.wildsolutions.downtimer.ui.home.HomeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 *
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
 */

class ListFragment : Fragment(), TimePickerDialog.OnTimeSetListener{

    private var _binding: FragmentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dataStore : DataStoreManager

    private lateinit var listView : ListView

    private lateinit var timingDao : TimingDao

    private lateinit var items : Array<Timing>

    private lateinit var timing : Timing

    var alertDialog: AlertDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // database
        val db = AppDatabase.getInstance(requireContext())
        this.timingDao = db.timingDao()
        this.listView = binding.listView

        // listener
        val cal = Calendar.getInstance()

        listView.setOnItemClickListener{parent,view, position, id ->
            this.timing = items.get(position)

            val stop = if (this.timing.stop == null) 0 else this.timing.stop!!

            cal.set(Calendar.HOUR_OF_DAY, TimeUnit.MILLISECONDS.toHours(stop).toInt())
            cal.set(Calendar.MINUTE, (TimeUnit.MILLISECONDS.toMinutes(stop) % TimeUnit.HOURS.toMinutes(1)).toInt())
            TimePickerDialog(
                this@ListFragment.requireContext(),
                this,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        updateList()
        return root
    }

    fun updateList(){
        lifecycleScope.launch(Dispatchers.IO) {
            val listItems = arrayOfNulls<kotlin.String>(timingDao.count())
            items = timingDao.getAll()

            for (i in 0 until items.size) {
                val timing = items[i]
                var start = this@ListFragment.millisTimeFormat(timing.start, "yyyy-MM-dd HH:mm")
                var end = this@ListFragment.millisTimeFormat(timing.stop, "yyyy-MM-dd HH:mm")
                listItems[i] = timing.id.toString() + " " + start + " - " + end.toString()
            }
            val adapter = ArrayAdapter(this@ListFragment.requireContext(), android.R.layout.simple_list_item_1, listItems)
            getActivity()?.runOnUiThread(){
                listView.adapter = adapter
            }
        }
    }


    @Override
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val cal = Calendar.getInstance()
        var stop = if (this.timing.stop == null) 0 else this.timing.stop!!
        cal.setTimeInMillis(stop)
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)

        stop = cal.timeInMillis
        if(stop > this@ListFragment.timing.start) {
            this@ListFragment.timing.stop = stop
            lifecycleScope.launch(Dispatchers.IO) {
                this@ListFragment.timingDao.update(this@ListFragment.timing)
            }
            updateList()
        }
        else {
            showMessage()
        }
    }

    fun showMessage(){
        val alertDialogBuilder = AlertDialog.Builder(this@ListFragment.requireContext())
        alertDialogBuilder.setTitle("Ungültige Zeit")
        alertDialogBuilder.setMessage("Die Zeit muss nach der Startzeit sein.")
        alertDialogBuilder.setPositiveButton("Ok", { dialogInterface: DialogInterface, i: Int -> })
        alertDialog = alertDialogBuilder.create()
        alertDialog?.show()
    }


    fun millisTimeFormat(millis : Long?, format : kotlin.String) : kotlin.String {
        val formatter = SimpleDateFormat(format)
        if(millis != null) {
            return formatter.format(millis)
        }
        else{
            return ""
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}