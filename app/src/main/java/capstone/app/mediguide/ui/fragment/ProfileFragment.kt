package capstone.app.mediguide.ui.fragment

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import capstone.app.mediguide.R
import capstone.app.mediguide.databinding.FragmentProfileBinding
import capstone.app.mediguide.ui.activity.MainActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        updateProfileUI(currentUser)

        binding.Logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun updateProfileUI(user: FirebaseUser) {
        // Tampilkan nama dari FirebaseUser
        val displayName = user.displayName
        if (!displayName.isNullOrEmpty()) {
            binding.username.text = displayName
        }

        // Ambil nama dari Firestore jika ada
        db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val name = document.getString("name")
                binding.username.text = name ?: displayName
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }

        // Tampilkan email
        binding.emailTV.text = user.email

        // Tampilkan foto profil
        val photoUrl = user.photoUrl
        if (photoUrl != null) {
            Glide.with(this).load(photoUrl).into(binding.profileImageView)
        } else {
            Glide.with(this).load(R.drawable.baseline_account_circle).into(binding.profileImageView)
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Apakah Anda yakin ingin logout?")
        builder.setPositiveButton("Ya") { dialog, which ->
            // Logout user
            signOut()
        }
        builder.setNegativeButton("Tidak") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun signOut() {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(requireContext())
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
