package capstone.app.mediguide.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import capstone.app.mediguide.R
import capstone.app.mediguide.databinding.ActivityHomeBinding
import capstone.app.mediguide.fragment.ChatFragment
import capstone.app.mediguide.fragment.HistoryFragment
import capstone.app.mediguide.fragment.HomeFragment
import capstone.app.mediguide.fragment.ProfileFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())

        binding.bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.chat -> replaceFragment(ChatFragment())
                R.id.history -> replaceFragment(HistoryFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameFragment, fragment)
        fragmentTransaction.commit()
    }
}
