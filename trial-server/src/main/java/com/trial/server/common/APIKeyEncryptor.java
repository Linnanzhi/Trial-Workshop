package com.trial.server.common;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * API密钥加密工具类
 * 使用 AES-128-GCM 加密算法（兼容 Java 8）
 */
public class APIKeyEncryptor {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 128;  // 改为 128 位，兼容 Java 8
    
    // 从环境变量读取加密密钥，如果没有则使用默认密钥（生产环境必须配置）
    private static final String SECRET_KEY = System.getenv("ENCRYPTION_KEY") != null 
        ? System.getenv("ENCRYPTION_KEY") 
        : "trial-workshop-secret-key-2024";  // 16字节密钥
    
    /**
     * 加密API密钥
     * 
     * @param apiKey 明文API密钥
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String apiKey) {
        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // 准备密钥
            SecretKey secretKey = getSecretKey();
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // 加密
            byte[] encryptedData = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            
            // 将IV和加密数据组合
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);
            
            // 返回Base64编码
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
    
    /**
     * 解密API密钥
     * 
     * @param encryptedApiKey 加密的Base64字符串
     * @return 明文API密钥
     */
    public static String decrypt(String encryptedApiKey) {
        try {
            // Base64解码
            byte[] decodedData = Base64.getDecoder().decode(encryptedApiKey);
            
            // 提取IV和加密数据
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);
            
            // 准备密钥
            SecretKey secretKey = getSecretKey();
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // 解密
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
    
    /**
     * 获取密钥
     */
    private static SecretKey getSecretKey() {
        try {
            // 使用16字节密钥用于 AES-128
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] key = new byte[16];  // AES-128 需要 16 字节
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 16));
            
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new RuntimeException("密钥生成失败", e);
        }
    }
}
