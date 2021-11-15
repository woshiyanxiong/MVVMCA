package com.mvvm.logcat

import android.annotation.SuppressLint
import android.os.Build
import java.lang.Exception
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Created by yan_x
 * @date 2021/11/15/015 15:46
 * @description 解密日志文件
 */
object LogAes {
    // 解密调用
    @Throws(Exception::class)
    fun decrypt(seed: String, encrypted: String): String{
        val rawKey: ByteArray = getRawKey(seed.toByteArray())
        val enc: ByteArray = toByte(encrypted)
        val result: ByteArray = decrypts(rawKey, enc)
        return String(result)
    }

    @SuppressLint("DeletedProvider")
    @Throws(Exception::class)
    private fun getRawKey(seed: ByteArray): ByteArray {
        val kgen: KeyGenerator = KeyGenerator.getInstance("AES")
        // SHA1PRNG 强随机种子算法
        var sr: SecureRandom? = null
        sr = if (Build.VERSION.SDK_INT >= 17) {
            SecureRandom.getInstance("SHA1PRNG", "Crypto") // Android4.2以上版本的调用此方法
        } else {
            SecureRandom.getInstance("SHA1PRNG")
        }
        sr.setSeed(seed)
        kgen.init(128, sr) // 192和256位可能不支持
        val skey: SecretKey = kgen.generateKey()
        return skey.getEncoded()
    }

    private fun toByte(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for (i in 0 until len) result[i] = Integer.valueOf(
            hexString.substring(2 * i, 2 * i + 2),
            16
        ).toByte()
        return result
    }

    @Throws(Exception::class)
    private fun decrypts(raw: ByteArray, encrypted: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher: Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)
        return cipher.doFinal(encrypted)
    }

}
