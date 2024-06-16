package capstone.app.mediguide.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import capstone.app.mediguide.R
import capstone.app.mediguide.databinding.ActivityHomeBinding
import capstone.app.mediguide.ui.fragment.ChatFragment
import capstone.app.mediguide.ui.fragment.HistoryFragment
import capstone.app.mediguide.ui.fragment.HomeFragment
import capstone.app.mediguide.ui.fragment.ProfileFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var isHistoryFragmentVisible: Boolean = false
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

    override fun onResume() {
        super.onResume()
        isHistoryFragmentVisible =
            supportFragmentManager.findFragmentById(R.id.frameFragment) is HistoryFragment
    }

    override fun onPause() {
        super.onPause()
        isHistoryFragmentVisible = false
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameFragment, fragment)
        fragmentTransaction.commit()
    }

    fun hideBottomNavView() {
        binding.bottomNavView.visibility = View.GONE
    }

    fun showBottomNavView() {
        binding.bottomNavView.visibility = View.VISIBLE
    }
}
