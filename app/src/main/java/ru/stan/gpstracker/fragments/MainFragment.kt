package ru.stan.gpstracker.fragments

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.stan.gpstracker.R
import ru.stan.gpstracker.databinding.FragmentMainBinding
import ru.stan.gpstracker.location.LocationService
import ru.stan.gpstracker.utils.DialogManager
import ru.stan.gpstracker.utils.checkPermission
import ru.stan.gpstracker.utils.showToast


class MainFragment : Fragment() {
    private var isServiceRunning = false
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding
    // запускаем 1 раз чтоб показывало шоу тост private var isLocationEnabledShown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        settingOsm()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissions()
        setOnClicks()
        checkServiceState()
    }

    private fun setOnClicks() = with(binding) {
        val listener = onClicks()
        icStartStop.setOnClickListener(listener)
    }

    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener {
            when (it.id) {
                R.id.ic_start_stop -> {
                    startStopService()
                }
            }
        }
    }

    private fun startStopService() {
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.icStartStop.setImageResource(R.drawable.play)
        }
        isServiceRunning = !isServiceRunning
    }

    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning){
            binding.icStartStop.setImageResource(R.drawable.stop)
        }
    }
    private fun startLocService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.icStartStop.setImageResource(R.drawable.stop)
    }

    override fun onResume() {
        super.onResume()
        checkLocPermission()
    }

    private fun settingOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOSM() = with(binding) {
        map.controller.setZoom(20.0)
        val mLogProvider = GpsMyLocationProvider(activity)
        val myLogOverlay = MyLocationNewOverlay(mLogProvider, map)
        myLogOverlay.enableMyLocation()
        myLogOverlay.enableFollowLocation()
        myLogOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(myLogOverlay)
        }
    }

    private fun registerPermissions() {
        pLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                if (it[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    initOSM()
                    checkLocationEnabled()
                } else {
                    showToast("Дайте разрешение для использования локации")
                }
            }
    }

    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOSM()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    private fun checkPermissionBefore10() {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            initOSM()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    private fun checkLocationEnabled() {
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) {
            DialogManager.showLocEnabledDialog(activity as AppCompatActivity,
                object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }

                })
        } else {
            //включаем чтоб только 1 раз запускалось шоу тост
//            if (!isLocationEnabledShown) {
//                showToast("Местоположение включено")
//                isLocationEnabledShown = true
//            }
            showToast("Местоположение включено")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}