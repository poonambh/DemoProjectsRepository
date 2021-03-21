package com.demo.store.web.security;

import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AESEncryptionSecurityProviderImpl implements AESEncryptionSecurityProvider {
	private static Logger logger = LogManager.getLogger(AESEncryptionSecurityProviderImpl.class);
	private static String salt = "AesEncryptionNextGen";

	private String encrypt(String strToEncrypt, String secret, int keyLength){
		if(StringUtils.isEmpty(strToEncrypt) || StringUtils.isEmpty(secret)) {
			logger.error("Secretkey/content to encrypt is empty/null.");
			throw new IllegalArgumentException("Secretkey/content to encrypt is empty/null");
		}
		try
		{
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, keyLength);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		}catch (Exception e){
			logger.error("Error while encrypting: " + e.toString());
			throw new RuntimeException("Error while encrypting: " + e.toString());
		}
	}

	private String decrypt(String strToDecrypt, String secret, int keyLength){
		if(StringUtils.isEmpty(strToDecrypt) || StringUtils.isEmpty(secret)) {
			logger.error("Secretkey/content to decrypt is empty/null.");
			throw new IllegalArgumentException("Secretkey/content to decrypt is empty/null");
		}
		try
		{
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, keyLength);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		}catch (Exception e){
			logger.error("Error while decrypting: " + e.toString());
			throw new RuntimeException("Error while decrypting: " + e.toString());
		}
	}

	@Override
	public String encryptAES256(String strToEncrypt, String secret) {
		return encrypt(strToEncrypt, secret, 256);
	}

	@Override
	public String decryptAES256(String strToDecrypt, String secret) {
		return decrypt(strToDecrypt, secret, 256);
	}

}
