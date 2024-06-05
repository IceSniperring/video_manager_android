package com.example.my_video_player.utils

import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import kotlin.io.encoding.ExperimentalEncodingApi

object RSAEncrypt {
    fun encrypt(str: String): String {
        // 注册BouncyCastleProvider
        Security.addProvider(BouncyCastleProvider())

        // 公钥字符串
        val publicKeyString = (
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvsJ+dMOJfUM6iGhEE4VwxfF0L\n" +
                        "6psLruCT2qbipf9IE8rmbipou/PargU+ygsl2ycOvhkrZwnxalo1xTWaWPjVRHt0\n" +
                        "3T6vbYmO3zhP3JRySgULV1tscsQZecbAAT3EunJHXGwtNoaV0Jfy8K6UWarretpZ\n" +
                        "ZlsFo+6+ltwnu6ZXvQIDAQAB"
                ).replace("\n", "")

        // Base64解码公钥
        val publicKeyBytes = Base64.getDecoder().decode(publicKeyString)

        // 生成PublicKey对象
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        // 要加密的字符串
        val plainText = str

        // 使用公钥加密（PKCS1填充）
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())

        // 将加密后的字节数组进行Base64编码
        val encryptedString = Base64.getEncoder().encodeToString(encryptedBytes)

        println("加密后的字符串: $encryptedString")

        Log.d("ice", encryptedString)
        return encryptedString
    }
}