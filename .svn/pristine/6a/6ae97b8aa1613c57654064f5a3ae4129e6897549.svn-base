/**
 * 
 */
package com.zhazhapan.qiniu.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.view.Dialogs;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * @author pantao
 *
 */
@SuppressWarnings("restriction")
public class Utils extends com.sun.javafx.util.Utils {

	private static Logger logger = Logger.getLogger(Utils.class);

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
