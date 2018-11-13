package org.code4everything.qiniu.util;

import com.zhazhapan.util.Checker;
import com.zhazhapan.util.dialog.Dialogs;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.util.Pair;
import org.code4everything.qiniu.QiniuApplication;
import org.code4everything.qiniu.api.SdkManager;
import org.code4everything.qiniu.constant.QiniuValueConsts;
import org.code4everything.qiniu.controller.MainWindowController;
import org.code4everything.qiniu.model.BucketBean;

import java.util.Optional;

/**
 * 应用弹窗
 *
 * @author pantao
 * @since 2018/11/13
 */
public class QiniuDialog {

    /**
     * 显示移动文件的弹窗
     */
    public Pair<SdkManager.FileAction, String[]> showFileMovableDialog(String bucket, String key, boolean setKey) {
        MainWindowController main = MainWindowController.getInstance();
        ButtonType ok = new ButtonType(QiniuValueConsts.OK, ButtonBar.ButtonData.OK_DONE);
        Dialog<String[]> dialog = getDialog(ok);
        // 文件名输入框
        TextField keyTextField = new TextField();
        keyTextField.setPrefWidth(300);
        keyTextField.setPromptText(QiniuValueConsts.FILE_NAME);
        keyTextField.setText(key);
        // 空间选择框
        ComboBox<String> bucketCombo = new ComboBox<>();
        bucketCombo.getItems().addAll(main.bucketChoiceCombo.getItems());
        bucketCombo.setValue(bucket);
        // 保存文件副本复选框
        CheckBox copyAsCheckBox = new CheckBox(QiniuValueConsts.COPY_AS);
        copyAsCheckBox.setSelected(true);
        // 设置容器
        GridPane grid = Dialogs.getGridPane();
        grid.add(copyAsCheckBox, 0, 0, 2, 1);
        grid.add(new Label(QiniuValueConsts.BUCKET_NAME), 0, 1);
        grid.add(bucketCombo, 1, 1);
        if (setKey) {
            // 显示文件名输入框
            grid.add(new Label(QiniuValueConsts.FILE_NAME), 0, 2);
            grid.add(keyTextField, 1, 2);
            Platform.runLater(keyTextField::requestFocus);
        }
        dialog.getDialogPane().setContent(grid);
        // 数据转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ok) {
                return new String[]{bucketCombo.getValue(), keyTextField.getText()};
            }
            return null;
        });
        // 读取数据
        Optional<String[]> result = dialog.showAndWait();
        if (result.isPresent()) {
            bucket = bucketCombo.getValue();
            key = keyTextField.getText();
            SdkManager.FileAction action = copyAsCheckBox.isSelected() ? SdkManager.FileAction.COPY :
                    SdkManager.FileAction.MOVE;
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
        ButtonType ok = new ButtonType(QiniuValueConsts.OK, ButtonBar.ButtonData.OK_DONE);
        Dialog<String[]> dialog = getDialog(ok);
        // 文本框
        TextField ak = new TextField();
        ak.setMinWidth(400);
        ak.setPromptText("Access Key");
        TextField sk = new TextField();
        sk.setPromptText("Secret Key");
        // 设置超链接
        Hyperlink hyperlink = new Hyperlink("查看我的KEY：" + QiniuValueConsts.QINIU_KEY_URL);
        hyperlink.setOnAction(event -> QiniuUtils.openLink(QiniuValueConsts.QINIU_KEY_URL));
        // 设置容器
        GridPane grid = Dialogs.getGridPane();
        grid.add(hyperlink, 0, 0, 2, 1);
        grid.add(new Label("Access Key:"), 0, 1);
        grid.add(ak, 1, 1);
        grid.add(new Label("Secret Key:"), 0, 2);
        grid.add(sk, 1, 2);
        Node okButton = dialog.getDialogPane().lookupButton(ok);
        okButton.setDisable(true);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(ak::requestFocus);
        // 监听文本框的输入状态
        ak.textProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(newValue.trim().isEmpty() || sk.getText().trim().isEmpty()));
        sk.textProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(newValue.trim().isEmpty() || ak.getText().trim().isEmpty()));
        // 等待用户操作
        Optional<String[]> result = dialog.showAndWait();
        // 处理结果
        if (result.isPresent() && Checker.isNotEmpty(ak.getText()) && Checker.isNotEmpty(sk.getText())) {
            QiniuApplication.getConfigBean().setAccessKey(ak.getText());
            QiniuApplication.getConfigBean().setSecretKey(sk.getText());
            ConfigUtils.writeConfig();
            return true;
        }
        return false;
    }

    /**
     * 显示添加桶的弹窗
     */
    public void showBucketAddableDialog() {
        ButtonType ok = new ButtonType(QiniuValueConsts.OK, ButtonBar.ButtonData.OK_DONE);
        Dialog<String[]> dialog = getDialog(ok);
        // 桶名称输入框
        TextField bucket = new TextField();
        bucket.setPromptText(QiniuValueConsts.BUCKET_NAME);
        // 桶域名输入框
        TextField url = new TextField();
        url.setPromptText(QiniuValueConsts.BUCKET_URL);
        // 桶区域输入框
        ComboBox<String> zone = new ComboBox<>();
        zone.getItems().addAll(QiniuValueConsts.BUCKET_NAME_ARRAY);
        zone.setValue(QiniuValueConsts.BUCKET_NAME_ARRAY[0]);
        // 设置容器
        GridPane grid = Dialogs.getGridPane();
        grid.add(new Label(QiniuValueConsts.BUCKET_NAME), 0, 0);
        grid.add(bucket, 1, 0);
        grid.add(new Label(QiniuValueConsts.BUCKET_URL), 0, 1);
        grid.add(url, 1, 1);
        grid.add(new Label(QiniuValueConsts.BUCKET_ZONE_NAME), 0, 2);
        grid.add(zone, 1, 2);
        Node okButton = dialog.getDialogPane().lookupButton(ok);
        okButton.setDisable(true);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(bucket::requestFocus);
        // 监听文本框的输入状态
        bucket.textProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(newValue.trim().isEmpty() || url.getText().isEmpty()));
        url.textProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(newValue.trim().isEmpty() || bucket.getText().isEmpty()));
        // 结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ok) {
                return new String[]{bucket.getText(), zone.getValue(), (Checker.isHyperLink(url.getText()) ?
                        url.getText() : "example.com")};
            }
            return null;
        });
        // 等待用户操作
        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(res -> {
            // 处理结果
            Platform.runLater(() -> MainWindowController.getInstance().addItem(res[0]));
            BucketBean bucketBean = new BucketBean(res[0], res[1], res[2]);
            QiniuApplication.getConfigBean().getBuckets().add(bucketBean);
            ConfigUtils.writeConfig();
        });
    }

    private Dialog<String[]> getDialog(ButtonType ok) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(QiniuValueConsts.MAIN_TITLE);
        dialog.setHeaderText(null);
        dialog.initModality(Modality.APPLICATION_MODAL);
        // 自定义确认和取消按钮
        ButtonType cancel = new ButtonType(QiniuValueConsts.CANCEL, ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);
        return dialog;
    }
}
