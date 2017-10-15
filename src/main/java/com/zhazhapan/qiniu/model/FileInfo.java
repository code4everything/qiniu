/**
 * 
 */
package com.zhazhapan.qiniu.model;

/**
 * @author pantao
 *
 */
public class FileInfo {

	private String name;

	private String type;

	private String size;

	private String time;

	public FileInfo() {

	}

	public FileInfo(String name, String type, String size, String time) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
