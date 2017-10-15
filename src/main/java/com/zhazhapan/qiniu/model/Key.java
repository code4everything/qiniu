/**
 * 
 */
package com.zhazhapan.qiniu.model;

/**
 * @author pantao
 *
 */
public class Key {

	private String accessKey;

	private String secretKey;

	public Key(String accessKey, String secretKey) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
