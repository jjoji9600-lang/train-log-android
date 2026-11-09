package io.github.overtimeevidence.app.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts the evidence bytes with a non-exportable Android Keystore key.
 *  Format: 5 ASCII magic bytes (OTEV1) + 12 byte GCM IV + ciphertext and tag.
 *  Device clock and imported screenshots are NOT independently authenticated.
 */
class CryptoVault {
    private val alias = "overtime-evidence-aes-v1"
    private val magic = "OTEV1".toByteArray(Charsets.US_ASCII)

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
            }.generateKey()
    }

    fun encrypt(source: InputStream, destination: File): Pair<String, Long> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        try {
            FileOutputStream(destination).use { file ->
                file.write(magic)
                file.write(cipher.iv)
                CipherOutputStream(file, cipher).use { encrypted ->
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        val read = source.read(buffer)
                        if (read == -1) break
                        digest.update(buffer, 0, read)
                        encrypted.write(buffer, 0, read)
                        size += read
                    }
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) } to size
        } catch (e: Exception) {
            destination.delete()
            throw e
        }
    }

    fun open(file: File): InputStream {
        val stream = FileInputStream(file)
        try {
            val data = DataInputStream(stream)
            val header = ByteArray(5)
            data.readFully(header)
            require(header.contentEquals(magic)) {
                "证据文件头已损坏"
            }
            val iv = ByteArray(12)
            data.readFully(iv)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            return CipherInputStream(stream, cipher)
        } catch (e: Exception) {
            stream.close()
            throw e
        }
    }
}
