/**
 * 
 */
package com.zhazhapan.qiniu.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.log4j.Logger;

import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.view.Dialogs;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * @author pantao
 *
 */
public class Utils {

	private static Logger logger = Logger.getLogger(Utils.class);

	public static Date localDateToDate(LocalDate localDate) {
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
		return Date.from(instant);
	}

	public static LocalDate dateToLocalDate(Date date) {
		Instant instant = date.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		return LocalDateTime.ofInstant(instant, zone).toLocalDate();
	}

	public static void copyToClipboard(String string) {
		ClipboardContent content = new ClipboardContent();
		content.putString(string);
		Clipboard.getSystemClipboard().setContent(content);
		logger.info("copy '" + string + "' to clipboard");
	}

	public static void openLink(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException e) {
			logger.error("open url '" + url + "' failed, message: " + e.getMessage());
			Dialogs.showException(Values.OPEN_LINK_ERROR, e);
		}
	}
}
