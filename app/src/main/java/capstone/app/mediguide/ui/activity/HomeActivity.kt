package capstone.app.mediguide.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import capstone.app.mediguide.R
import capstone.app.mediguide.ui.fragment.HistoryFragment
import capstone.app.mediguide.ui.fragment.HomeFragment
import capstone.app.mediguide.ui.fragment.ProfileFragment
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation

class HomeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavView)
        if (bottomNavigation == null) {
            Log.e("HomeActivity", "CurvedBottomNavigation view not found")
            return
        }

        bottomNavigation.add(
            CurvedBottomNavigation.Model(1,"Home",R.drawable.baseline_home)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(2,"History",R.drawable.baseline_history)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(3,"Profile",R.drawable.baseline_profile)
        )

        bottomNavigation.setOnClickMenuListener {
            when(it.id){
                1 -> {
                    replaceFragment(HomeFragment())
                }
                2 -> {
                    replaceFragment(HistoryFragment())
                }
                3 -> {
                    replaceFragment(ProfileFragment())
                }
            }
        }

        replaceFragment(HomeFragment())
        bottomNavigation.show(1)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameFragment,fragment)
            .commit()
    }

    fun hideBottomNavView() {
        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavView)
        bottomNavigation?.visibility = View.GONE
    }

    fun showBottomNavView() {
        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavView)
        bottomNavigation?.visibility = View.VISIBLE
    }

}
