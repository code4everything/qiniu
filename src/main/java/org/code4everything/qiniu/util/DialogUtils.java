package org.code4everything.qiniu.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.code4everything.qiniu.constant.QiniuValueConsts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * 弹窗工具类
 *
 * @author pantao 对JavaFX对话框进行封装
 */
public class DialogUtils {

    private DialogUtils() {}

    public static String showInputDialog(String header, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(QiniuValueConsts.MAIN_TITLE);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static Optional<ButtonType> showInformation(String content) {
        return showInformation(null, content);
    }

    public static Optional<ButtonType> showInformation(String header, String content) {
        return showAlert(header, content, AlertType.INFORMATION);
    }

    public static Optional<ButtonType> showWarning(String content) {
        return showWarning(null, content);
    }

    public static Optional<ButtonType> showWarning(String header, String content) {
        return showAlert(header, content, AlertType.WARNING);
    }

    public static Optional<ButtonType> showError(String content) {
        return showError(null, content);
    }

    public static Optional<ButtonType> showError(String header, String content) {
        return showAlert(header, content, AlertType.ERROR);
    }

    public static Optional<ButtonType> showConfirmation(String content) {
        return showConfirmation(null, content);
    }

    public static Optional<ButtonType> showConfirmation(String header, String content) {
        return showAlert(header, content, AlertType.CONFIRMATION);
    }

    public static Optional<ButtonType> showException(Exception e) {
        return showException(null, e);
    }

    public static void showFatalError(String header, Exception e) {
        showException(header, e);
        System.exit(0);
    }

    public static Optional<ButtonType> showException(String header, Exception e) {
        // 获取一个警告对象
        Alert alert = getAlert(header, "错误信息追踪：", AlertType.ERROR);
        // 打印异常信息
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String exception = stringWriter.toString();
        // 显示异常信息
        TextArea textArea = new TextArea(exception);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        // 设置弹窗容器
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);
        alert.getDialogPane().setExpandableContent(gridPane);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showAlert(String content) {
        return showAlert(null, content);
    }

    public static Optional<ButtonType> showAlert(String content, AlertType alertType) {
        return showAlert(null, content, alertType);
    }

    public static Optional<ButtonType> showAlert(String header, String content) {
        return showAlert(header, content, AlertType.INFORMATION);
    }

    public static Optional<ButtonType> showAlert(String header, String content, AlertType alertType) {
        return showAlert(header, content, alertType, Modality.NONE, null, StageStyle.DECORATED);
    }

    public static Optional<ButtonType> showAlert(String header, String content, AlertType alertType,
                                                 Modality modality, Window window, StageStyle style) {
        return getAlert(header, content, alertType, modality, window, style).showAndWait();
    }

    public static Alert getAlert(String header, String content, AlertType alertType) {
        return getAlert(header, content, alertType, Modality.APPLICATION_MODAL, null, StageStyle.DECORATED);
    }

    public static Alert getAlert(String header, String content, AlertType alertType, Modality modality, Window window
            , StageStyle style) {
        Alert alert = new Alert(alertType);
        alert.setTitle(QiniuValueConsts.MAIN_TITLE);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initModality(modality);
        alert.initOwner(window);
        alert.initStyle(style);
        return alert;
    }
}
