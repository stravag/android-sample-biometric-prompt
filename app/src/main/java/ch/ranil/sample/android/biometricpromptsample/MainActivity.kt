package ch.ranil.sample.android.biometricpromptsample

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

        val secureText = "Secure Text!"
        textView.text = secureText
        buttonEncrypt.setOnClickListener {
            biometricPromptManager.encryptPrompt(
                data = secureText.toByteArray(),
                failedAction = { showToast("encrypt failed") },
                successAction = {
                    textView.text = String(it)
                    showToast("encrypt success")
                }
            )
        }

        buttonDecrypt.setOnClickListener {
            biometricPromptManager.decryptPrompt(
                failedAction = { showToast("decrypt failed") },
                successAction = {
                    textView.text = String(it)
                    showToast("decrypt success")
                }
            )
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
