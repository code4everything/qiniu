/**
 * 
 */
package com.zhazhapan.qiniu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.util.Checker;
import com.zhazhapan.qiniu.view.Dialogs;

/**
 * @author pantao
 *
 */
public class FileExecutor {

	private Logger logger = Logger.getLogger(FileExecutor.class);

	public void saveFile(String path, String content) {
		saveFile(path, content, false);
	}

	public void saveFile(String path, String content, boolean append) {
		saveFile(new File(path), content, append);
	}

	public void saveFile(File file, String content) {
		saveFile(file, content, false);
	}

	public String readFile(String path) {
		return readFile(new File(path));
	}

	public String readFile(File file) {
		StringBuilder content = new StringBuilder();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				content.append(line + "\r\n");
			}
			reader.close();
		} catch (IOException e) {
			logger.error("read file error, messages: " + e.getMessage());
		}
		return content.toString();
	}

	/**
	 * 保存日志文件
	 */
	public void saveLogFile(String logPath, String content) {
		File file = new File(logPath);
		if (file.exists()) {
			content += readFile(file);
		}
		saveFile(file, content);
	}

	public void saveFile(File file, String content, boolean append) {
		if (Checker.isNotNull(file)) {
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(file, append));
				out.write(content);
				out.close();
				logger.info("save file '" + file.getAbsolutePath() + "' success");
			} catch (IOException e) {
				logger.error("save file failed, messages: " + e.getMessage());
				Dialogs.showException(Values.SAVE_FILE_ERROR, e);
			}
		}
	}
}
