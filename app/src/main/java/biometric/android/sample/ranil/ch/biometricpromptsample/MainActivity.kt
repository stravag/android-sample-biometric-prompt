package biometric.android.sample.ranil.ch.biometricpromptsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var biometricPromptManager: BiometricPromptManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        biometricPromptManager = BiometricPromptManager(this)

        buttonEncrypt.setOnClickListener {
            val data = "Secure Text".toByteArray(Charset.forName("UTF-8"))
            biometricPromptManager.showSaveFingerprintPrompt(
                    dataSupplier = { data },
                    fallbackAction = { showToast("fallbackAction") },
                    successAction = { showToast("successAction") })
        }

        buttonDecrypt.setOnClickListener {
            biometricPromptManager.showRestoreFingerprintPrompt(
                    fallbackAction = { showToast("fallbackAction") },
                    proceedAction = { showToast(String(it, Charset.forName("UTF-8"))) }
            )
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
