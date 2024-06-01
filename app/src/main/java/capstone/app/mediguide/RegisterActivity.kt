package capstone.app.mediguide

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import capstone.app.mediguide.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.regiterButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

    }

}