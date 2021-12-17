package com.mvvm.logcat.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * Created by yan_x
 *
 * @date 2021/12/17/017 16:41
 * @description
 */
public class AESUtils {
    /**
     * 偏移量,必须是16位字符串,可修改，但必须保证加密解密都相同
     */
    private static final String IV_STRING = "0123456789012345";

    /**
     * 加密文件
     *
     * @param key
     * @param byteContent
     * @return
     */
    public static byte[] encryptData(String key, byte[] byteContent) {
        byte[] encryptedBytes = null;
        try {
            byte[] enCodeFormat = key.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            byte[] initParam = IV_STRING.getBytes();
            // 用于产生密文的第一个block，以使最终生成的密文产生差异（明文相同的情况下），
            // 使密码攻击变得更为困难，除此之外IvParameterSpec并无其它用途。
            // 为了方便也可以动态跟随key生成new IvParameterSpec(key.getBytes("utf-8"))
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            encryptedBytes = cipher.doFinal(byteContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedBytes;
    }

    /**
     * 解密文件
     *
     * @param key
     * @param encryptedBytes
     * @return
     */
    public static byte[] decryptData(String key, byte[] encryptedBytes) {
        byte[] result = null ;
        try {
            byte[] sEnCodeFormat = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(sEnCodeFormat, "AES");
            byte[] initParam = IV_STRING.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            result = cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
