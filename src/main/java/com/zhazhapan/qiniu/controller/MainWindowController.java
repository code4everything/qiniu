/**
 * 
 */
package com.zhazhapan.qiniu.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.qiniu.common.QiniuException;
import com.zhazhapan.qiniu.Downloader;
import com.zhazhapan.qiniu.FileExecutor;
import com.zhazhapan.qiniu.QiManager;
import com.zhazhapan.qiniu.QiniuApplication;
import com.zhazhapan.qiniu.ThreadPool;
import com.zhazhapan.qiniu.QiManager.FileAction;
import com.zhazhapan.qiniu.config.ConfigLoader;
import com.zhazhapan.qiniu.config.QiConfiger;
import com.zhazhapan.qiniu.model.FileInfo;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.Utils;
import com.zhazhapan.qiniu.view.Dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.util.Pair;

/**
 * @author pantao
 *
 */
@SuppressWarnings("restriction")
public class MainWindowController {

	private Logger logger = Logger.getLogger(MainWindowController.class);

	@FXML
	public ComboBox<String> bucketChoiceCombo;

	@FXML
	public TextField zoneText;

	@FXML
	private Button bucketAddableButton;

	@FXML
	private Button resetKeyButton;

	@FXML
	public TextArea uploadStatusTextArea;

	@FXML
	private TextArea selectedFileTextArea;

	@FXML
	public ComboBox<String> filePrefixCombo;

	@FXML
	private TextField bucketDomainTextField;

	@FXML
	public TableView<FileInfo> resTable;

	@FXML
	public TextField searchTextField;

	@FXML
	private TableColumn<FileInfo, String> nameCol;

	@FXML
	private TableColumn<FileInfo, String> typeCol;

	@FXML
	private TableColumn<FileInfo, String> sizeCol;

	@FXML
	private TableColumn<FileInfo, String> timeCol;

	@FXML
	private Hyperlink toCsdnBlog;

	@FXML
	private Hyperlink toHexoBlog;

	@FXML
	private Hyperlink toGithubSource;

	@FXML
	private Hyperlink toIntro;

	@FXML
	private Hyperlink toIntro1;

	@FXML
	private Label totalSizeLabel;

	@FXML
	private Label totalLengthLabel;

	@FXML
	private AreaChart<String, Long> bucketFluxChart;

	@FXML
	private AreaChart<String, Long> bucketBandChart;

	@FXML
	private DatePicker startDate;

	@FXML
	private DatePicker endDate;

	@FXML
	public ProgressBar uploadProgress;

	@FXML
	public ProgressBar downloadProgress;

	private static MainWindowController mainWindowController = null;

	private double upProgress = 0;

	private String status = "";

	public static MainWindowController getInstance() {
		return mainWindowController;
	}

	public enum DownloadWay {
		// 下载的方式，包括私有和公有
		PRIVATE, PUBLIC
	}

	/**
	 * 初始化
	 */
	@FXML
	private void initialize() {
		mainWindowController = this;
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		// 设置文件名可编辑
		nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
		nameCol.setOnEditCommit(v -> {
			String name = "";
			FileInfo fileInfo = v.getTableView().getItems().get(v.getTablePosition().getRow());
			if (new QiManager().renameFile(bucketChoiceCombo.getValue(), v.getOldValue(), v.getNewValue())) {
				name = v.getNewValue();
			} else {
				name = v.getOldValue();
			}
			if (Checker.isNotEmpty(searchTextField.getText())) {
				QiniuApplication.data.get(QiniuApplication.data.indexOf(fileInfo)).setName(name);
			}
			fileInfo.setName(name);
		});
		typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
		// 设置文件类型可编辑
		typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
		typeCol.setOnEditCommit(v -> {
			FileInfo fileInfo = v.getTableView().getItems().get(v.getTablePosition().getRow());
			String type = "";
			if (new QiManager().changeType(fileInfo.getName(), v.getNewValue(), bucketChoiceCombo.getValue())) {
				type = v.getNewValue();
			} else {
				type = v.getOldValue();
			}
			if (Checker.isNotEmpty(searchTextField.getText())) {
				QiniuApplication.data.get(QiniuApplication.data.indexOf(fileInfo)).setType(type);
			}
			fileInfo.setType(type);
		});
		sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
		timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
		resTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// 设置默认的开始和结束日期，并调用dateChange事件刷新数据
		endDate.setValue(LocalDate.now());
		long startTime = System.currentTimeMillis() - Values.DATE_SPAN_OF_THIRTY_ONE;
		LocalDate endDate = Formatter.dateToLocalDate(new Date(startTime));
		startDate.setValue(endDate);
		// 设置BucketChoiceComboBox改变事件，改变后并配置新的上传环境
		bucketChoiceCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			String[] zones = QiniuApplication.buckets.get(newValue).split(" ");
			zoneText.setText(zones[0]);
			searchTextField.clear();
			if (Checker.isHyperLink(zones[1])) {
				bucketDomainTextField.setText(zones[1]);
			} else {
				logger.warn("doesn't config the domain of bucket correctly yet");
				bucketDomainTextField.setText(Values.DOMAIN_CONFIG_ERROR);
			}
			ThreadPool.executor.submit(() -> {
				if (new QiConfiger().configUploadEnv(zones[0], newValue)) {
					// 加载文件列表
					setResTableData();
					// 刷新流量带宽统计
					dateChange();
				}
			});
		});
		// 设置超链接监听
		toCsdnBlog.setOnAction(e -> Utils.openLink("http://csdn.zhazhapan.com"));
		toHexoBlog.setOnAction(e -> Utils.openLink("http://zhazhapan.com"));
		toGithubSource.setOnAction(e -> Utils.openLink("https://github.com/zhazhapan/qiniu"));
		String introPage = "http://zhazhapan.com/2017/10/15/%E4%B8%83%E7%89%9B%E4%BA%91%E2%80"
				+ "%94%E2%80%94%E5%AF%B9%E8%B1%A1%E5%AD%98%E5%82%A8%E7%AE%A1%E7%90%86%E5%B7%A5"
				+ "%E5%85%B7%E4%BB%8B%E7%BB%8D/";
		toIntro.setOnAction(e -> Utils.openLink(introPage));
		toIntro1.setOnAction(e -> Utils.openLink("http://blog.csdn.net/qq_26954773/article/details/78245100"));
	}

	/**
	 * 拖曳文件至TextArea
	 */
	public void dragFileOver(DragEvent event) {
		logger.info("drog file in textarea");
		event.acceptTransferModes(TransferMode.ANY);
	}

	/**
	 * 拖曳文件松开鼠标
	 */
	public void dragFileDropped(DragEvent event) {
		logger.info("drag file dropped");
		setFiles(event.getDragboard().getFiles());
	}

	/**
	 * 开始日期或结束日期改变，刷新流量、带宽统计
	 */
	public void dateChange() {
		Date start = Formatter.localDateToDate(startDate.getValue());
		Date end = Formatter.localDateToDate(endDate.getValue());
		String fromDate = Formatter.dateToString(start);
		String toDate = Formatter.dateToString(end);
		String domain = bucketDomainTextField.getText();
		// 获取开始日期和结束日期的时间差
		long timeSpan = end.getTime() - start.getTime();
		if (Checker.isNotEmpty(domain) && timeSpan >= 0 && timeSpan <= Values.DATE_SPAN_OF_THIRTY_ONE) {
			String[] domains = { domain };
			logger.info("start to get flux of domain: " + domain);
			ThreadPool.executor.submit(() -> {
				QiManager manager = new QiManager();
				Platform.runLater(() -> {
					bucketFluxChart.getData().clear();
					bucketFluxChart.getData().add(manager.getBucketFlux(domains, fromDate, toDate));
					bucketBandChart.getData().clear();
					bucketBandChart.getData().add(manager.getBucketBandwidth(domains, fromDate, toDate));
				});
			});
		} else {
			logger.info("domain is empty or time span is invalid, can't get flux of this bucket");
		}
	}

	/**
	 * 日志下载，cdn相关
	 */
	public void downloadCdnLog() {
		String date = Dialogs.showInputDialog(null, Values.INPUT_LOG_DATE, Formatter.dateToString(new Date()));
		new QiManager().downloadCdnLog(date);
	}

	/**
	 * 文件刷新，cdn相关
	 */
	public void refreshFile() {
		new QiManager().refreshFile(resTable.getSelectionModel().getSelectedItems(),
				"http://" + bucketDomainTextField.getText());
	}

	/**
	 * 通过链接下载其他的网络文件
	 */
	public void downloadFromURL() {
		String url = Dialogs.showInputDialog(null, Values.DOWNLOAD_URL, "http://example.com");
		new Downloader().downloadFromNet(url);
	}

	/**
	 * 用浏览器打开文件
	 */
	public void openFile() {
		ObservableList<FileInfo> selectedItems = resTable.getSelectionModel().getSelectedItems();
		if (Checker.isNotEmpty(selectedItems)) {
			String filename = selectedItems.get(0).getName();
			String url = "http://" + new QiManager().getPublicURL(filename, bucketDomainTextField.getText());
			Utils.openLink(url);
		}
	}

	/**
	 * 私有下载
	 */
	public void privateDownload() {
		download(DownloadWay.PRIVATE);
	}

	public void download(DownloadWay way) {
		ObservableList<FileInfo> selectedItems = resTable.getSelectionModel().getSelectedItems();
		if (Checker.isNotEmpty(selectedItems)) {
			QiManager manager = new QiManager();
			String domain = "http://" + bucketDomainTextField.getText();
			Downloader downloader = new Downloader();
			if (way == DownloadWay.PUBLIC) {
				logger.debug("start to public download");
				for (FileInfo fileInfo : selectedItems) {
					manager.publicDownload(fileInfo.getName(), domain, downloader);
				}
			} else {
				logger.debug("start to private download");
				for (FileInfo fileInfo : selectedItems) {
					manager.privateDownload(fileInfo.getName(), domain, downloader);
				}
			}
		}
	}

	/**
	 * 公有下载
	 */
	public void publicDownload() {
		download(DownloadWay.PUBLIC);
	}

	/**
	 * 更新镜像源
	 */
	public void updateFile() {
		ObservableList<FileInfo> selectedItems = resTable.getSelectionModel().getSelectedItems();
		if (Checker.isNotEmpty(selectedItems)) {
			QiManager manager = new QiManager();
			for (FileInfo fileInfo : selectedItems) {
				manager.updateFile(bucketChoiceCombo.getValue(), fileInfo.getName());
			}
		}
	}

	/**
	 * 设置文件生存时间
	 */
	public void setLife() {
		ObservableList<FileInfo> selectedItems = resTable.getSelectionModel().getSelectedItems();
		if (Checker.isNotEmpty(selectedItems)) {
			String lifeStr = Dialogs.showInputDialog(null, Values.FILE_LIFE, Values.DEFAULT_FILE_LIFE);
			if (Checker.isNumber(lifeStr)) {
				int life = Formatter.stringToInt(lifeStr);
				QiManager manager = new QiManager();
				for (FileInfo fileInfo : selectedItems) {
					manager.setFileLife(bucketChoiceCombo.getValue(), fileInfo.getName(), life);
				}
			}
		}
	}

	/**
	 * 显示移动或复制文件的窗口
	 */
	public void showFileMovableDialog() {
		ObservableList<FileInfo> selectedItems = resTable.getSelectionModel().getSelectedItems();
		Pair<FileAction, String[]> pair = null;
		String bucket = bucketChoiceCombo.getValue();
		if (Checker.isEmpty(selectedItems)) {
			// 没有选择文件，结束方法
			return;
		} else if (selectedItems.size() > 1) {
			pair = new Dialogs().showFileMovableDialog(bucket, "", false);
		} else {
			pair = new Dialogs().showFileMovableDialog(bucket, selectedItems.get(0).getName(), true);
		}
		if (Checker.isNotNull(pair)) {
			boolean useNewKey = Checker.isNotEmpty(pair.getValue()[1]);
			ObservableList<FileInfo> resData = resTable.getItems();
			QiManager manager = new QiManager();
			for (FileInfo fileInfo : selectedItems) {
				String fb = bucketChoiceCombo.getValue();
				String tb = pair.getValue()[0];
				String name = useNewKey ? pair.getValue()[1] : fileInfo.getName();
				boolean move = manager.moveOrCopyFile(fb, fileInfo.getName(), tb, name, pair.getKey());
				if (pair.getKey() == FileAction.MOVE && move) {
					boolean sear = Checker.isNotEmpty(searchTextField.getText());
					if (fb.equals(tb)) {
						// 删除数据源
						QiniuApplication.data.remove(fileInfo);
						QiniuApplication.totalLength--;
						QiniuApplication.totalSize -= Formatter.sizeToLong(fileInfo.getSize());
						if (sear) {
							resData.remove(fileInfo);
						}
					} else {
						// 更新文件名
						fileInfo.setName(name);
						if (sear) {
							QiniuApplication.data.get(QiniuApplication.data.indexOf(fileInfo)).setName(name);
						}
					}
				}
			}
			setBucketCount();
		}
	}

	/**
	 * 删除文件
	 */
	public void deleteFiles() {
		ObservableList<FileInfo> fileInfos = resTable.getSelectionModel().getSelectedItems();
		new QiManager().deleteFiles(fileInfos, bucketChoiceCombo.getValue());
	}

	/**
	 * 复制链接
	 */
	public void copyLink() {
		ObservableList<FileInfo> fileInfos = resTable.getSelectionModel().getSelectedItems();
		if (Checker.isNotEmpty(fileInfos)) {
			// 只复制选中的第一个文件的链接
			String link = "http://" + bucketDomainTextField.getText() + "/" + fileInfos.get(0).getName();
			Utils.copyToClipboard(link);
			logger.info("copy link: " + link);
		}
	}

	/**
	 * 搜索资源文件，忽略大小写
	 */
	public void searchFile(KeyEvent event) {
		ArrayList<FileInfo> files = new ArrayList<FileInfo>();
		String search = Checker.checkNull(searchTextField.getText());
		logger.info("search file: " + search);
		QiniuApplication.totalLength = 0;
		QiniuApplication.totalSize = 0;
		try {
			// 正则匹配查询
			Pattern pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
			for (FileInfo file : QiniuApplication.data) {
				if (pattern.matcher(file.getName()).find()) {
					files.add(file);
					QiniuApplication.totalLength++;
					QiniuApplication.totalSize += Formatter.sizeToLong(file.getSize());
				}
			}
		} catch (Exception e) {
			logger.warn("pattern '" + search + "' compile error, message: " + e.getMessage());
		}
		setBucketCount();
		resTable.setItems(FXCollections.observableArrayList(files));
	}

	/**
	 * 统计空间文件的数量以及大小
	 */
	public void setBucketCount() {
		totalLengthLabel.setText(Formatter.customFormatDecimal(QiniuApplication.totalLength, ",###") + " 个文件");
		totalSizeLabel.setText(Formatter.formatSize(QiniuApplication.totalSize));
	}

	/**
	 * 刷新资源列表
	 */
	public void refreshResTable() {
		if (!new QiConfiger().checkNet()) {
			Dialogs.showWarning(Values.NET_ERROR);
			return;
		}
		setResTableData();
		Dialogs.showInformation(Values.REFRESH_SUCCESS);
	}

	/**
	 * 将从存储空间获取的文件列表映射到Table
	 */
	public void setResTableData() {
		ThreadPool.executor.submit(() -> {
			new QiManager().listFileOfBucket();
			Platform.runLater(() -> {
				resTable.setItems(QiniuApplication.data);
				setBucketCount();
			});
		});

	}

	/**
	 * 添加bucket到ComboBox
	 */
	public void addItem(String bucket) {
		if (!bucketChoiceCombo.getItems().contains(bucket)) {
			bucketChoiceCombo.getItems().add(bucket);
		}
	}

	/**
	 * 保存文件的上传状态
	 */
	public void saveUploadStatus() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(Values.FILE_CHOOSER_TITLE);
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File file = chooser.showSaveDialog(QiniuApplication.stage);
		new FileExecutor().saveFile(file, uploadStatusTextArea.getText());
	}

	/**
	 * 复制上传状态到剪贴板
	 */
	public void copyUploadStatus() {
		Utils.copyToClipboard(uploadStatusTextArea.getText());
	}

	/**
	 * 清空文件的上传状态
	 */
	public void clearUploadStatus() {
		uploadStatusTextArea.clear();
	}

	/**
	 * 选择要上传的文件
	 */
	public void selectFile() {
		logger.info("show file chooser dialog");
		FileChooser chooser = new FileChooser();
		chooser.setTitle(Values.FILE_CHOOSER_TITLE);
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		setFiles(chooser.showOpenMultipleDialog(QiniuApplication.stage));
	}

	public void setFiles(List<File> files) {
		if (Checker.isNotEmpty(files)) {
			for (File file : files) {
				if (!selectedFileTextArea.getText().contains(file.getAbsolutePath())) {
					selectedFileTextArea.insertText(0, file.getAbsolutePath() + "\r\n");
				}
			}
		}
	}

	/**
	 * 上传选择的文件
	 */
	public void uploadFile() {
		if (Checker.isNullOrEmpty(zoneText.getText()) || Checker.isNullOrEmpty(selectedFileTextArea.getText())) {
			// 没有选择存储空间或文件，不能上传文件
			Dialogs.showWarning(Values.NEED_CHOOSE_BUCKET_OR_FILE);
			return;
		}
		// 新建一个上传文件的线程
		ThreadPool.executor.submit(() -> {
			Platform.runLater(() -> {
				uploadProgress.setVisible(true);
				uploadProgress.setProgress(0);
				uploadStatusTextArea.insertText(0, Values.CONFIGING_UPLOAD_ENVIRONMENT);
			});
			String bucket = bucketChoiceCombo.getValue();
			// 默认不指定key的情况下，以文件内容的hash值作为文件名
			String key = Checker.checkNull(filePrefixCombo.getValue());
			String[] paths = selectedFileTextArea.getText().split("\n");
			// 去掉\r\n的长度
			int end = Values.UPLOADING.length() - 2;
			Platform.runLater(
					() -> uploadStatusTextArea.deleteText(0, Values.CONFIGING_UPLOAD_ENVIRONMENT.length() - 1));
			// 总文件数
			double total = paths.length;
			upProgress = 0;
			for (String path : paths) {
				if (Checker.isNotEmpty(path)) {
					Platform.runLater(() -> uploadStatusTextArea.insertText(0, Values.UPLOADING));
					logger.info("start to upload file: " + path);
					String filename = "undefined";
					String url = "http://" + QiniuApplication.buckets.get(bucket).split(" ")[1] + "/";
					File file = new File(path);
					try {
						// 判断文件是否存在
						if (file.exists()) {
							filename = key + file.getName();
							String upToken = QiniuApplication.auth.uploadToken(bucket, filename);
							QiniuApplication.uploadManager.put(path, filename, upToken);
							status = Formatter.datetimeToString(new Date()) + "\tsuccess\t" + url + filename + "\t"
									+ path;
							logger.info("upload file '" + path + "' to bucket '" + bucket + "' success");
						} else if (Checker.isHyperLink(path)) {
							// 抓取网络文件到空间中
							logger.info(path + " is a hyper link");
							filename = key + Formatter.getFileName(path);
							QiniuApplication.bucketManager.fetch(path, bucket, filename);
							status = Formatter.datetimeToString(new Date()) + "\tsuccess\t" + url + filename + "\t"
									+ path;
							logger.info("fetch remote file '" + path + "' to bucket '" + bucket + "' success");
						} else {
							// 文件不存在
							logger.info("file '" + path + "' not exists");
							status = Formatter.datetimeToString(new Date()) + "\tfailed\t" + path;
						}
					} catch (QiniuException e) {
						status = Formatter.datetimeToString(new Date()) + "\terror\t" + path;
						logger.error("upload error, message: " + e.getMessage());
						Platform.runLater(() -> Dialogs.showException(Values.UPLOAD_ERROR, e));
					}
					Platform.runLater(() -> {
						uploadStatusTextArea.deleteText(0, end);
						uploadStatusTextArea.insertText(0, status);
						// 设置上传的进度
						uploadProgress.setProgress(++upProgress / total);
					});
				}
				Platform.runLater(() -> selectedFileTextArea.deleteText(0, path.length() + (paths.length > 1 ? 1 : 0)));
			}
			Platform.runLater(() -> {
				// 将光标移到最前面
				uploadStatusTextArea.positionCaret(0);
				// 清空待上传的文件列表
				selectedFileTextArea.clear();
				// 上传完成时，设置上传进度度不可见
				uploadProgress.setVisible(false);
			});
			// 添加文件前缀到配置文件
			if (Checker.isNotEmpty(key) && !QiniuApplication.prefix.contains(key)) {
				Platform.runLater(() -> filePrefixCombo.getItems().add(key));
				QiniuApplication.prefix.add(key);
				ConfigLoader.writeConfig();
			}
			setResTableData();
		});
	}

	/**
	 * 打开配置文件
	 */
	public void openConfigFile() {
		try {
			Desktop.getDesktop().open(new File(ConfigLoader.configPath));
			logger.info("open config file");
			Optional<ButtonType> result = Dialogs.showConfirmation(Values.RELOAD_CONFIG);
			if (result.get() == ButtonType.OK) {
				// 重新载入配置文件
				QiniuApplication.buckets = new HashMap<String, String>(10);
				QiniuApplication.prefix = new ArrayList<String>();
				bucketChoiceCombo.getItems().clear();
				filePrefixCombo.getItems().clear();
				ConfigLoader.loadConfig();
			}
		} catch (IOException e) {
			logger.error("open config file error, message: " + e.getMessage());
			Dialogs.showException(Values.OPEN_FILE_ERROR, e);
		} catch (Exception e) {
			logger.error("can't open config file, message: " + e.getMessage());
			Dialogs.showException(Values.OPEN_FILE_ERROR, e);
		}
	}

	/**
	 * 重置Key
	 */
	public void resetKey() {
		boolean ok = new Dialogs().showInputKeyDialog();
		if (ok && Checker.isNotEmpty(zoneText.getText())) {
			new QiConfiger().configUploadEnv(zoneText.getText(), bucketChoiceCombo.getValue());
		}
	}

	/**
	 * 添加存储空间
	 */
	public void showBucketAddableDialog(ActionEvent event) {
		logger.info("show bucket addable dialog");
		new Dialogs().showBucketAddableDialog();
	}
}
