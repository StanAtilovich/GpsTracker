package ru.stan.gpstracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.stan.gpstracker.databinding.ActivityMainBinding
import ru.stan.gpstracker.fragments.MainFragment
import ru.stan.gpstracker.fragments.SettingFragment
import ru.stan.gpstracker.fragments.TracksFragment
import ru.stan.gpstracker.utils.openFragment
import ru.stan.gpstracker.utils.showToast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBottomNavClicks()
        openFragment(MainFragment.newInstance())
    }
    private fun onBottomNavClicks(){
        binding.bNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    openFragment(MainFragment.newInstance())
                    showToast("home")
                }
                R.id.tracks -> {
                    openFragment(TracksFragment.newInstance())
                    showToast("tracks")
                }
                R.id.settings -> {
                    openFragment(SettingFragment())
                    showToast("settings")
                }
            }
            true
        }
    }
}