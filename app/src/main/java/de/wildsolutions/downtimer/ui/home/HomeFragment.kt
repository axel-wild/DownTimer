package de.wildsolutions.downtimer.ui.home


import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import de.wildsolutions.downtimer.AppDatabase
import de.wildsolutions.downtimer.DataStoreManager
import de.wildsolutions.downtimer.R
import de.wildsolutions.downtimer.Timing
import de.wildsolutions.downtimer.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.String
import java.util.concurrent.TimeUnit

/**
 *
 * @author: Axel Wild
 * @license https://opensource.org/license/gpl-3-0 GPL 3.0 - GNU General Public License version 3
 */

class HomeFragment : Fragment(), View.OnClickListener  {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dataStore : DataStoreManager

    var isWorking = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // datastore
        dataStore = DataStoreManager.getInstance(this.requireContext())

        // database
        val db = AppDatabase.getInstance(requireContext())
        var timingDao = db.timingDao()

        val btn = binding.btnStartStop

        lifecycleScope.launch(Dispatchers.IO) {
            var lastTiming : Timing = timingDao.getLast()
            // timer is running
            if(lastTiming != null) {
                if (lastTiming.isRunning()) {
                    this@HomeFragment.isWorking = true
                    checkTimerOnCreate(lastTiming)
                }
            }
            updateView()
            //btn.setText(if (isWorking) R.string.stop else R.string.start)
        }
        btn?.setOnClickListener(this)
        return root
    }

    /**
     * Start / stop timer
     */
    override fun onClick(view: View?){
        when(view?.id){
            R.id.btnStartStop -> {

                val db = AppDatabase.getInstance(requireContext())
                var timingDao = db.timingDao()

                val meter = binding.viewTimer
                val downMeter = binding.viewDowntimer

                var txtCount = binding.txtCount
                var txtSum = binding.txtSum

                var sumMillis = 0L
                lifecycleScope.launch(Dispatchers.IO) {
                    val count = timingDao.count()
                    txtCount.setText("Zeiterfassungen: " + count)

                    sumMillis = timingDao.sum()
                    val hms = millisTimeFormat(sumMillis)
                    txtSum.setText("Gesamtzeit: " + hms)
                }

                if (!isWorking) {
                    isWorking = true
                    var timing = Timing()
                    timing.start()
                    lifecycleScope.launch(Dispatchers.IO) {
                        timingDao.insert(timing)
                        sumMillis = timingDao.sum()
                        meter.base = SystemClock.elapsedRealtime() - sumMillis
                        meter.start()
                        downMeter.base = SystemClock.elapsedRealtime() - sumMillis + dataStore.getTotalTimeToWorkMillis()
                        downMeter.isCountDown = true
                        downMeter.start()
                    }

                } else {
                    meter.stop()
                    downMeter.stop()
                    isWorking = false
                    lifecycleScope.launch(Dispatchers.IO) {
                        var timing = timingDao.getLast()
                        timing.stop()
                        timingDao.update(timing)
                    }
                }
                updateView()
            }
        }
    }

    fun updateView(){
        var sumMillis = 0L

        val meter = binding.viewTimer
        val downMeter = binding.viewDowntimer

        var txtCount = binding.txtCount
        var txtSum = binding.txtSum
        var txtDays = binding.txtDays
        var txtWeeklyWorkingHours = binding.txtWeeklyWorkingHours
        val btn = binding.btnStartStop
        btn.setText(if (isWorking) R.string.stop else R.string.start)

        val workingDaysFormat = String.format("%.2f", dataStore.getWorkingDays())
        val timeToWorkMillis = dataStore.getTotalTimeToWorkMillis()
        val hmsTimeToWork = millisTimeFormat(timeToWorkMillis)

        val db = AppDatabase.getInstance(requireContext())
        var timingDao = db.timingDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val count = timingDao.count()
            txtCount.setText("Zeiterfassungen: " + count)
            sumMillis = timingDao.sum()
            val hms = millisTimeFormat(sumMillis)
            txtSum.setText("Gesamtzeit: " + hms)


            sumMillis = timingDao.sum()
            meter.base = SystemClock.elapsedRealtime() - sumMillis
            downMeter.base = SystemClock.elapsedRealtime() - sumMillis + dataStore.getTotalTimeToWorkMillis()
            downMeter.isCountDown = true
        }

        val sStats = "Kalendertage: " + dataStore.getDaysDiff() + " | Arbeitstage: " + workingDaysFormat +" | Zeit: " + hmsTimeToWork
        txtDays.setText(sStats)

        val weeklyWorkingMillis = dataStore.getWeeklyWorkingMillis()
        val hmsWeeklyWorkingMillis = millisTimeFormat(weeklyWorkingMillis)
        txtWeeklyWorkingHours.setText("wöchentliche Arbeitszeit: " + hmsWeeklyWorkingMillis)
    }

    /**
     * Check timer after startup. If measurement started a day before, terminate it. If measuremnt started today and has not been stopped manually, continue it.
     */
    fun checkTimerOnCreate(timing : Timing) {
        val meter = binding.viewTimer
        val downMeter = binding.viewDowntimer
        val db = AppDatabase.getInstance(requireContext())

        // check, when measurment has been started
        if(timing.startedToday()){
            // continue
            meter.base = SystemClock.elapsedRealtime() - db.timingDao().sum()
            meter.start()
            downMeter.base = SystemClock.elapsedRealtime() - db.timingDao().sum() + dataStore.getTotalTimeToWorkMillis()
            downMeter.isCountDown = true
            downMeter.start()
            this@HomeFragment.isWorking = true
        }
        else{
            // terminate
            timing.stop()
            db.timingDao().update(timing)
            this@HomeFragment.isWorking = false
        }
        this.updateView()
    }

    fun millisTimeFormat(millis : Long) : kotlin.String {
        return  String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}