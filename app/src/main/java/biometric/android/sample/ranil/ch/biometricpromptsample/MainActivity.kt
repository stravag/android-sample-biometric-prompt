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

        val secureText = "Secure Text!"
        textView.text = secureText
        buttonEncrypt.setOnClickListener {
            biometricPromptManager.showSaveFingerprintPrompt(
                dataSupplier = { secureText.utf8ByteArray() },
                fallbackAction = { showToast("save fallback") },
                successAction = {
                    showToast("save success")
                    textView.text = it.utf8String()
                }
            )
        }

        buttonDecrypt.setOnClickListener {
            biometricPromptManager.showRestoreFingerprintPrompt(
                fallbackAction = { showToast("restore fallback") },
                successAction = {
                    showToast("restore success")
                    textView.text = it.utf8String()
                }
            )
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun String.utf8ByteArray() = toByteArray(Charset.forName("UTF-8"))
    private fun ByteArray.utf8String() = String(this, Charset.forName("UTF-8"))
}
