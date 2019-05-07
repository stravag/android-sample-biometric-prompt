package biometric.android.sample.ranil.ch.biometricpromptsample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var biometricPromptManager: BiometricPromptManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        biometricPromptManager = BiometricPromptManager(this)

        val secureText = "Secure Text ${System.currentTimeMillis()}"
        textView.text = secureText
        buttonEncrypt.setOnClickListener {
            biometricPromptManager.encryptPrompt(
                dataSupplier = { secureText.toByteArray() },
                fallbackAction = { showToast("save fallback") },
                successAction = {
                    showToast("save success")
                    textView.text = String(it)
                }
            )
        }

        buttonDecrypt.setOnClickListener {
            biometricPromptManager.decryptPrompt(
                fallbackAction = { showToast("restore fallback") },
                successAction = {
                    showToast("restore success")
                    textView.text = String(it)
                }
            )
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
