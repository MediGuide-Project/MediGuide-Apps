package capstone.app.mediguide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import capstone.app.mediguide.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
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
                    // Handle error
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
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

        binding.Logout.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        auth.signOut()
        // Add logic to clear credentials and navigate to login screen
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
