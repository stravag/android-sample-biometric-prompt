package biometric.android.sample.ranil.ch.biometricpromptsample

import android.content.ContextWrapper
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricPrompt
import androidx.core.os.CancellationSignal
import androidx.fragment.app.FragmentActivity
import java.security.Key
import java.security.KeyStore
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class BiometricPromptManager(private val activity: FragmentActivity) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    fun showRestoreFingerprintPrompt(fallbackAction: () -> Unit, successAction: (ByteArray) -> Unit) {
        try {
            val secretKey = getKey()
            val initializationVector = getInitializationVector()
            if (secretKey != null && initializationVector != null) {
                val cipher = getRestoreCipher(secretKey, initializationVector)
                handleRestoreFingerprint(cipher, fallbackAction, successAction)
            } else {
                fallbackAction()
            }
        } catch (e: Exception) {
            fallbackAction()
        }
    }

    fun showSaveFingerprintPrompt(
        dataSupplier: () -> ByteArray,
        fallbackAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {
        try {
            val secretKey = createKey()
            val cipher = getSaveCipher(secretKey)
            handleSaveFingerprint(cipher, dataSupplier, fallbackAction, successAction)
        } catch (e: Exception) {
            fallbackAction()
        }
    }

    private fun getKey(): Key? = keyStore.getKey(KEY_NAME, null)

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, KEYSTORE)
        val keyGenParameterSpec =
            KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                .setUserAuthenticationRequired(true)
                .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun getInitializationVector(): ByteArray? {
        val iv = sharedPreferences.getString(INITIALIZATION_VECTOR, null)
        return when {
            iv != null -> Base64.decode(iv, Base64.DEFAULT)
            else -> null
        }
    }

    private fun getEncryptedData(): ByteArray? {
        val iv = sharedPreferences.getString(DATA_ENCRYPTED, null)
        return when {
            iv != null -> Base64.decode(iv, Base64.DEFAULT)
            else -> null
        }
    }

    private fun saveEncryptedData(dataEncrypted: ByteArray, initializationVector: ByteArray) {
        val editor = sharedPreferences.edit()
        editor.putString(DATA_ENCRYPTED, Base64.encodeToString(dataEncrypted, Base64.DEFAULT))
        editor.putString(INITIALIZATION_VECTOR, Base64.encodeToString(initializationVector, Base64.DEFAULT))
        editor.apply()
    }

    private fun getRestoreCipher(key: Key, iv: ByteArray): Cipher =
        Cipher.getInstance(keyTransformation()).apply { init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv)) }

    private fun getSaveCipher(key: Key): Cipher =
        Cipher.getInstance(keyTransformation()).apply { init(Cipher.ENCRYPT_MODE, key) }

    private fun handleRestoreFingerprint(
        cipher: Cipher,
        fallbackAction: () -> Unit,
        proceedAction: (ByteArray) -> Unit
    ) {

        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { cipher ->
                    val encrypted = getEncryptedData()
                    val data = cipher.doFinal(encrypted)
                    activity.runOnUiThread { proceedAction(data) }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                activity.runOnUiThread { fallbackAction() }
            }
        })

        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun handleSaveFingerprint(
        cipher: Cipher,
        dataSupplier: () -> ByteArray,
        fallbackAction: () -> Unit,
        successAction: (ByteArray) -> Unit
    ) {

        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { resultCipher ->
                    val iv = resultCipher.iv
                    val loginTokenEncrypted = cipher.doFinal(dataSupplier())
                    saveEncryptedData(loginTokenEncrypted, iv)
                    activity.runOnUiThread { successAction(loginTokenEncrypted) }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                activity.runOnUiThread { fallbackAction() }
            }
        })

        val promptInfo = biometricPromptInfo()
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
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
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_NAME = "MY_KEY"
        private const val DATA_ENCRYPTED = "DATA_ENCRYPTED"
        private const val INITIALIZATION_VECTOR = "INITIALIZATION_VECTOR"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private fun keyTransformation() = listOf(ALGORITHM, BLOCK_MODE, PADDING).joinToString(separator = "/")
    }
}