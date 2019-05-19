package ch.ranil.sample.android.biometricpromptsample

import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import com.google.crypto.tink.Aead
import com.google.crypto.tink.Config
import com.google.crypto.tink.config.TinkConfig
import java.util.concurrent.Executors
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadFactory


class BiometricPromptTinkManager(private val activity: FragmentActivity) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    private val aead: Aead

    init {
        Config.register(TinkConfig.LATEST)
        aead = AeadFactory.getPrimitive(getOrGenerateNewKeysetHandle())
    }

    fun decryptPrompt(failedAction: () -> Unit, successAction: (ByteArray) -> Unit) {
        try {
            handleDecrypt(failedAction, successAction)
        } catch (e: Exception) {
            Log.d(TAG, "Decrypt BiometricPrompt exception", e)
            failedAction()
        }
    }

    fun encryptPrompt(
        data: ByteArray,
        failedAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {
        try {
            handleEncrypt(data, failedAction, successAction)
        } catch (e: Exception) {
            Log.d(TAG, "Encrypt BiometricPrompt exception", e)
            failedAction()
        }
    }

    private fun getOrGenerateNewKeysetHandle(): KeysetHandle {
        return AndroidKeysetManager.Builder()
            .withSharedPref(activity, TINK_KEYSET_NAME, null)
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build().keysetHandle
    }

    private fun handleEncrypt(
        data: ByteArray,
        failedAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {

        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val encryptedData = aead.encrypt(data, EMPTY_ASSOCIATED_DATA)
                saveEncryptedData(encryptedData)
                activity.runOnUiThread { successAction(encryptedData) }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "Authentication error. $errString ($errorCode)")
                activity.runOnUiThread { failedAction() }
            }
        })

        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun saveEncryptedData(dataEncrypted: ByteArray) {
        sharedPreferences.edit {
            putString(DATA_ENCRYPTED, Base64.encodeToString(dataEncrypted, Base64.DEFAULT))
        }
    }

    private fun getEncryptedData(): ByteArray? {
        val data = sharedPreferences.getString(DATA_ENCRYPTED, null)
        return when {
            data != null -> Base64.decode(data, Base64.DEFAULT)
            else -> null
        }
    }

    private fun handleDecrypt(
        failedAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {

        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val decryptedData = aead.decrypt(getEncryptedData(), EMPTY_ASSOCIATED_DATA)
                activity.runOnUiThread { successAction(decryptedData) }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "Authentication error. $errString ($errorCode)")
                activity.runOnUiThread { failedAction() }
            }
        })

        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun biometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Prompt Title")
            .setSubtitle("Prompt Subtitle")
            .setDescription("Prompt Description: lorem ipsum dolor sit amet.")
            .setNegativeButtonText(activity.getString(android.R.string.cancel))
            .build()
    }

    companion object {
        private const val TAG = "BiometricPrompt"
        private const val TINK_KEYSET_NAME = "tink_keyset"
        private const val DATA_ENCRYPTED = "data_encrypted"
        private const val MASTER_KEY_URI = "android-keystore://tink_master_key"
        private val EMPTY_ASSOCIATED_DATA = ByteArray(0)
    }
}