package org.code4everything.qiniu.view;

import com.zhazhapan.util.Checker;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.api.QiManager;
import org.code4everything.qiniu.model.ConfigBean;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.util.ConfigUtils;
import org.code4everything.qiniu.util.QiniuUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * @author pantao 对JavaFX对话框进行封装
 */
public class Dialogs {

    private Logger logger = Logger.getLogger(Dialogs.class);

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
        return alert(header, content, AlertType.INFORMATION);
    }

    public static Optional<ButtonType> showWarning(String content) {
        return showWarning(null, content);
    }

    public static Optional<ButtonType> showWarning(String header, String content) {
        return alert(header, content, AlertType.WARNING);
    }

    public static Optional<ButtonType> showError(String content) {
        return showError(null, content);
    }

    public static Optional<ButtonType> showError(String header, String content) {
        return alert(header, content, AlertType.ERROR);
    }

    public static Optional<ButtonType> showConfirmation(String content) {
        return showConfirmation(null, content);
    }

    public static Optional<ButtonType> showConfirmation(String header, String content) {
        return alert(header, content, AlertType.CONFIRMATION);
    }

    public static Optional<ButtonType> showException(Exception e) {
        return showException(null, e);
    }

    public static void showFatalError(String header, Exception e) {
        showException(header, e);
        System.exit(0);
    }

    public static Optional<ButtonType> showException(String header, Exception e) {
        Alert alert = getAlert(header, "错误信息追踪：", AlertType.ERROR);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String exception = stringWriter.toString();

        TextArea textArea = new TextArea(exception);
        textArea.setEditable(false);
        textArea.setWrapText(true);

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

    public static Optional<ButtonType> alert(String content) {
        return alert(null, content);
    }

    public static Optional<ButtonType> alert(String content, AlertType alertType) {
        return alert(null, content, alertType);
    }

    public static Optional<ButtonType> alert(String header, String content) {
        return alert(header, content, AlertType.INFORMATION);
    }

    public static Optional<ButtonType> alert(String header, String content, AlertType alertType) {
        return alert(header, content, alertType, Modality.NONE, null, StageStyle.DECORATED);
    }

    public static Optional<ButtonType> alert(String header, String content, AlertType alertType, Modality modality,
                                             Window window, StageStyle style) {
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

    public Pair<QiManager.FileAction, String[]> showFileMovableDialog(String bucket, String key, boolean setKey) {
        MainWindowController main = MainWindowController.getInstance();
        ButtonType ok = new ButtonType(QiniuValueConsts.OK, ButtonData.OK_DONE);
        Dialog<String[]> dialog = getDialog(ok);

        TextField keyTextField = new TextField();
        keyTextField.setPrefWidth(300);
        keyTextField.setPromptText(QiniuValueConsts.FILE_NAME);
        keyTextField.setText(key);
        ComboBox<String> bucketCombo = new ComboBox<String>();
        bucketCombo.getItems().addAll(main.bucketChoiceCombo.getItems());
        bucketCombo.setValue(bucket);
        CheckBox copyasCheckBox = new CheckBox(QiniuValueConsts.COPY_AS);
        copyasCheckBox.setSelected(true);

        GridPane grid = com.zhazhapan.util.dialog.Dialogs.getGridPane();
        grid.add(copyasCheckBox, 0, 0, 2, 1);
        grid.add(new Label(QiniuValueConsts.BUCKET_NAME), 0, 1);
        grid.add(bucketCombo, 1, 1);
        if (setKey) {
            grid.add(new Label(QiniuValueConsts.FILE_NAME), 0, 2);
            grid.add(keyTextField, 1, 2);
            Platform.runLater(keyTextField::requestFocus);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ok) {
                return new String[]{bucketCombo.getValue(), keyTextField.getText()};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        if (result.isPresent()) {
            bucket = bucketCombo.getValue();
            key = keyTextField.getText();
            QiManager.FileAction action = copyasCheckBox.isSelected() ? QiManager.FileAction.COPY :
                    QiManager.FileAction.MOVE;
            return new Pair<>(action, new String[]{bucket, key});
        } else {
            return null;
        }
    }

    /**
     * 显示输入密钥的对话框
     *
     * @return 返回用户是否点击确定按钮
     */
    public boolean showInputKeyDialog() {
        ButtonType ok = new ButtonType(QiniuValueConsts.OK, ButtonData.OK_DONE);
        Dialog<String[]> dialog = getDialog(ok);

        TextField ak = new TextField();
        ak.setMinWidth(400);
        ak.setPromptText("Access Key");
        TextField sk = new TextField();
        sk.setPromptText("Secret Key");

        Hyperlink hyperlink = new Hyperlink("查看我的KEY：" + QiniuValueConsts.QINIU_KEY_URL);
        hyperlink.setOnAction(event -> QiniuUtils.openLink(QiniuValueConsts.QINIU_KEY_URL));

        GridPane grid = com.zhazhapan.util.dialog.Dialogs.getGridPane();
        grid.add(hyperlink, 0, 0, 2, 1);
        grid.add(new Label("Access Key:"), 0, 1);
        grid.add(ak, 1, 1);
        grid.add(new Label("Secret Key:"), 0, 2);
        grid.add(sk, 1, 2);

        Node okButton = dialog.getDialogPane().lookupButton(ok);
        okButton.setDisable(true);

        // 监听文本框的输入状态
        ak.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || sk.getText().trim().isEmpty());
        });
        sk.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || ak.getText().trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(ak::requestFocus);

        Optional<String[]> result = dialog.showAndWait();
        if (result.isPresent() && Checker.isNotEmpty(ak.getText()) && Checker.isNotEmpty(sk.getText())) {
            QiniuApplication.getConfigBean().setAccessKey(ak.getText());
            QiniuApplication.getConfigBean().setSecretKey(sk.getText());
            ConfigUtils.writeConfig();
            return true;
        }
        return false;
    }

    public void showBucketAddableDialog() {
        ButtonType ok = new ButtonType(QiniuValueConsts.OK, ButtonData.OK_DONE);
        Dialog<String[]> dialog = getDialog(ok);

        TextField bucket = new TextField();
        bucket.setPromptText(QiniuValueConsts.BUCKET_NAME);
        TextField url = new TextField();
        url.setPromptText(QiniuValueConsts.BUCKET_URL);
        ComboBox<String> zone = new ComboBox<>();
        zone.getItems().addAll(QiniuValueConsts.BUCKET_NAME_ARRAY);
        zone.setValue(QiniuValueConsts.BUCKET_NAME_ARRAY[0]);

        GridPane grid = com.zhazhapan.util.dialog.Dialogs.getGridPane();
        grid.add(new Label(QiniuValueConsts.BUCKET_NAME), 0, 0);
        grid.add(bucket, 1, 0);
        grid.add(new Label(QiniuValueConsts.BUCKET_URL), 0, 1);
        grid.add(url, 1, 1);
        grid.add(new Label(QiniuValueConsts.BUCKET_ZONE_NAME), 0, 2);
        grid.add(zone, 1, 2);

        Node okButton = dialog.getDialogPane().lookupButton(ok);
        okButton.setDisable(true);

        // 监听文本框的输入状态
        bucket.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || url.getText().isEmpty());
        });
        url.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || bucket.getText().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(bucket::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ok) {
                return new String[]{bucket.getText(), zone.getValue(), (Checker.isHyperLink(url.getText()) ?
                        url.getText() : "example.com")};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(res -> {
            Platform.runLater(() -> MainWindowController.getInstance().addItem(res[0]));
            ConfigBean.BucketBean bucketBean = new ConfigBean().new BucketBean();
            bucketBean.setBucket(res[0]);
            bucketBean.setUrl(res[2]);
            bucketBean.setZone(res[1]);
            QiniuApplication.getConfigBean().getBuckets().add(bucketBean);
            ConfigUtils.writeConfig();
        });
    }

    public Dialog<String[]> getDialog(ButtonType ok) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(QiniuValueConsts.MAIN_TITLE);
        dialog.setHeaderText(null);

        dialog.initModality(Modality.APPLICATION_MODAL);

        // 自定义确认和取消按钮
        ButtonType cancel = new ButtonType(QiniuValueConsts.CANCEL, ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);
        return dialog;
    }
}
