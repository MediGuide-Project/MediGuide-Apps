package capstone.app.mediguide

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import capstone.app.mediguide.databinding.ActivityProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.bottomNavView.selectedItemId = R.id.profile
        auth = Firebase.auth
        currentUser = auth.currentUser!!

        // Set user data to views
        binding.username.text = currentUser.displayName

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    binding.username.text = name
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        binding.emailTV.text = currentUser.email

        val photoUrl = currentUser.photoUrl
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .into(binding.profileImageView)
        } else {
            Glide.with(this)
                .load(R.drawable.baseline_account_circle_24)
                .into(binding.profileImageView)
        }

        binding.bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }

                R.id.history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }

                R.id.profile -> {
                    true
                }

                else -> false
            }
        }

        binding.Logout.setOnClickListener {
            signOut()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    private fun signOut() {

        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@ProfileActivity)
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

    }
}