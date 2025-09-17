import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES 加解密工具类
 * 使用 Android Keystore 安全地存储密钥，支持 GCM 模式（推荐）
 */
object AESCryptoUtils {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "safetyWallet" // 密钥别名，可自定义
    private const val TRANSFORMATION = "AES/GCM/NoPadding" // 使用GCM模式，提供认证和加密
    private const val GCM_TAG_LENGTH = 128 // GCM认证标签长度（比特）

    /**
     * 获取或创建 AES 密钥
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        return if (keyStore.containsAlias(KEY_ALIAS)) {
            // 密钥已存在，直接获取
            (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            // 创建新密钥
            createKey()
        }
    }

    /**
     * 创建新的 AES 密钥并存入 KeyStore
     */
    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM) // 使用GCM模式
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE) // GCM不需要Padding
            .setKeySize(256) // 使用256位密钥（更安全）
            .setUserAuthenticationRequired(false) // 设置为true则需要用户认证（如指纹）才能使用密钥
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * 加密字符串
     * @param plaintext 明文字符串
     * @return 加密后的字符串（Base64编码），格式为: IV + 密文
     */
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateSecretKey()

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // 加密数据
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // 获取初始化向量（IV）
        val iv = cipher.iv

        // 组合 IV + 密文，并进行Base64编码
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * 解密字符串
     * @param encryptedText 加密后的字符串（Base64编码）
     * @return 解密后的明文字符串
     */
    @Throws(Exception::class)
    fun decrypt(encryptedText: String): String {
        // Base64解码
        val decoded = Base64.decode(encryptedText, Base64.DEFAULT)

        // 分离 IV 和密文
        // GCM模式的IV通常是12字节
        val iv = decoded.copyOfRange(0, 12)
        val ciphertext = decoded.copyOfRange(12, decoded.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateSecretKey()

        // 创建GCMParameterSpec
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        // 解密数据
        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * 加密字节数组
     * @param plainData 明文字节数组
     * @return 加密后的字节数组（IV + 密文）
     */
    fun encryptBytes(plainData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateSecretKey()

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(plainData)
        val iv = cipher.iv

        return iv + encryptedBytes
    }

    /**
     * 解秘密文字节数组
     * @param encryptedData 加密的字节数组（IV + 密文）
     * @return 解密后的明文字节数组
     */
    @Throws(Exception::class)
    fun decryptBytes(encryptedData: ByteArray): ByteArray {
        val iv = encryptedData.copyOfRange(0, 12)
        val ciphertext = encryptedData.copyOfRange(12, encryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateSecretKey()

        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * 删除密钥（用于测试或重置）
     */
    fun deleteKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }
}