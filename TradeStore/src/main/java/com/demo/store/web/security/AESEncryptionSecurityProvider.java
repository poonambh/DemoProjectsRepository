package com.demo.store.web.security;

public interface AESEncryptionSecurityProvider {
	public String encryptAES256(String strToEncrypt, String secret);
	public String decryptAES256(String strToDecrypt, String secret);
}
