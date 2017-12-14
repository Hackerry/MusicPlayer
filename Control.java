import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main class
 * @author Hackerry
 *
 */
public class Control extends Application {
	private static BorderPane mainP;
	private static VBox listP;
	private static HBox controlP;
	private static GridPane titleP;
	
	// Title Panel constants
	private Label nowPlayingL;

	// Control Panel constants
	private static final String imgFile = new File("data/button.png").toURI().toString();
	private static ImageView playI, pauseI, prevI, nextI;
	private static final int MAX_VOLUME = 100;
	private static double volume;
	private static Button play;
	private static Label noPlayList, volumeL;
	private static Slider volumeS;
	
	// PlayList Info constants
	private static final File playListFile = new File("data/PlayList.dat");
	private static MediaPlayer player;
	private static ObservableList<SongInfo> songInfos;
	private static TableView<SongInfo> playListTable;
	
	// Progress Box constants
	private HBox progressBox;
	private Slider progressS;
	private Label progressL;
	private boolean beforePlaying = false;
	private String totalLength;
	private Runnable updateT = new Runnable() {
		public void run() {
			try {
				while(player != null && player.getStatus() == MediaPlayer.Status.PLAYING) {
					Platform.runLater(() -> {
						progressS.setValue(player.getCurrentTime().toMillis());
						progressL.setText(Util.parseDuration(player.getCurrentTime().toMillis(), false) + "/" + totalLength);
					});
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	};
	
	// Setting constants
	private static final File settingFile = new File("data/setting.ini");
	private static File songFolder;

	//Initial the stage
	public void start(Stage stage) {
		mainP = new BorderPane();
		
		initTitle(stage);
		initControl();
		initPlayList();
		initList();
		initProgressBar();
		initSetting();
		
		Scene scene = new Scene(mainP, 1007, 600);
		scene.getStylesheets().add(new File("data/Style.css").toURI().toString());
		stage.setTitle("Music Player");
		stage.setOnCloseRequest(e -> {
			saveSettings();
		});
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}
	
	//Save play list and setting to file
	private void saveSettings() {
		if(!playListFile.exists()) {
			try {
				new File(playListFile.getParent()).mkdirs();
				playListFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		PrintWriter pw1 = null;
		try {
			pw1 = new PrintWriter(playListFile);
			for(SongInfo si: songInfos) {
				String path = new File(si.getMedia().getSource()).getAbsolutePath();
				pw1.println(replaceString(path.substring(path.indexOf("file:\\") + 6)));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			if(pw1 != null) {
				pw1.close();
			}
		}
		
		if(!settingFile.exists()) {
			try {
				settingFile.createNewFile();
				new File(settingFile.getParent()).mkdirs();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		PrintWriter pw2 = null;
		try {
			pw2 = new PrintWriter(settingFile);
			pw2.println("Volume:" + (int)volume);
			if(songFolder != null) {
				pw2.println("Folder:" + songFolder.getAbsolutePath());
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			if(pw2 != null) {
				pw2.close();
			}
		}
	}
	
	//Initial visual Title bar 
	private void initTitle(Stage stage) {
		titleP = new GridPane();
		titleP.setHgap(10);
		titleP.setId("upper-pane");
		
		nowPlayingL = new Label("No play");
		Button importB = new Button("Import"), deleteB = new Button("Delete");
		importB.setId("bar-button");deleteB.setId("bar-button");
		
		importB.setOnAction(e -> {
			FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().add(new ExtensionFilter("Only support .mp3 files", "*.mp3"));
			chooser.setTitle("Choose your music");
			chooser.setInitialDirectory(songFolder == null? new File(System.getProperties().getProperty("user.home")): songFolder);
			List<File> files = chooser.showOpenMultipleDialog(stage);
			if(files != null) {
				for(File f: files) {
					addMedia(new Media(f.toURI().toString()));
				}
			}
			songFolder = files.get(files.size()-1).getParentFile();
		});
		deleteB.setOnAction(e -> {
			ObservableList<SongInfo> list = playListTable.getSelectionModel().getSelectedItems();
			if(list.isEmpty()) return;
			else {
				for(SongInfo si: list) {
					if(player != null && si.getMedia() == player.getMedia()) {
						if(player != null) {
							player.stop();
							pause();
							progressBox.setVisible(false);
							break;
						}
					}
				}
				songInfos.removeAll(list);
			}
			
			if(songInfos.isEmpty()) {
				listP.getChildren().set(0, noPlayList);
			}
			
		});
		
		titleP.add(nowPlayingL, 0, 0);
		titleP.add(importB, 1, 0);
		titleP.add(deleteB, 2, 0);
		GridPane.setHgrow(nowPlayingL, Priority.SOMETIMES);
		
		mainP.setTop(titleP);
	}
	
	//Initial Control buttons 
	private void initControl() {
		controlP = new HBox(20);
		controlP.setId("bottom-pane");
		
		play = new Button();
		Button  prev = new Button(), next = new Button();
		play.setId("round-button");prev.setId("round-button");next.setId("round-button");
		playI = new ImageView(imgFile);pauseI = new ImageView(imgFile);prevI = new ImageView(imgFile);nextI = new ImageView(imgFile);
		playI.setViewport(new Rectangle2D(-4,0,60,60));
		pauseI.setViewport(new Rectangle2D(60,0,60,60));
		prevI.setViewport(new Rectangle2D(118,0,60,60));prev.setRotate(180);
		nextI.setViewport(new Rectangle2D(118,0,60,60));
		play.setGraphic(playI);prev.setGraphic(prevI);next.setGraphic(nextI);
		
		play.setOnAction(e -> {
			if(player == null) {
				return;
			} else {
				if(player.getStatus() == MediaPlayer.Status.PLAYING) {
					pause();
				} else {
					play();
				}
			}
		});
		prev.setOnAction(e -> {
			moveSong(-1);
		});
		next.setOnAction(e -> {
			moveSong(1);
		});
		
		HBox buttonBox = new HBox(20);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.getChildren().addAll(prev, play, next);
		
		volumeL = new Label("Volume: " + (int)volume);
		volumeL.setMinWidth(120);
		volumeL.setTextAlignment(TextAlignment.RIGHT);
		volumeS = new Slider(0,MAX_VOLUME,1);
		volumeS.valueProperty().addListener((e, oldVal, newVal) -> {
			volume = newVal.doubleValue();
			volumeL.setText("Volume: " + (int)volume);
			if(player != null) {
				player.setVolume(volume/MAX_VOLUME);
			}
		});
		
		HBox volumeBox = new HBox(20);
		volumeBox.setPadding(new Insets(10,10,10,10));
		volumeBox.setAlignment(Pos.CENTER);
		volumeBox.getChildren().addAll(volumeL, volumeS);
		
		controlP.getChildren().addAll(buttonBox, volumeBox);
		mainP.setBottom(controlP);
	}

	//Initial the visual progress bar
	private void initProgressBar() {
		progressS = new Slider();
		progressL = new Label("00:00/00:00");
		
		progressBox = new HBox(10);
		progressBox.getChildren().addAll(progressS, progressL);
		progressBox.setAlignment(Pos.CENTER);
		progressBox.setPadding(new Insets(10,10,10,10));
		HBox.setHgrow(progressS, Priority.ALWAYS);
		progressBox.setVisible(false);
		
		listP.getChildren().add(progressBox);
	}
	
	//Initial the play list from file
	private void initPlayList() {
		songInfos = FXCollections.observableArrayList();
		
		if(playListFile.exists() && playListFile.length() != 0) {
			Scanner sc = null;
			try {
				sc = new Scanner(playListFile);
				String path;
				while(sc.hasNextLine()) {
					path = sc.nextLine();
					File file = new File(path);
					if(file.exists() && path.endsWith(".mp3")) {
						Media m = new Media(file.toURI().toString());
						addMedia(m);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Read playList error");
			} finally {
				if(sc != null) {
					sc.close();
				}
			}
		}
	}
	
	/**
	 * Add a new Media asynchronous
	 * @param media the media to add
	 */
	private void addMedia(Media media) {
		// Check if it already exists
		for(SongInfo si: songInfos) {
			if(si.getMedia().getSource().equals(media.getSource()))return;
		}
		
		MediaPlayer tempPlayer = new MediaPlayer(media);
		tempPlayer.setOnReady(() -> {
			if(songInfos.isEmpty()) {
				listP.getChildren().set(0, playListTable);
			}
			
			Map<String, Object> metaData = media.getMetadata();
			String name = shortenName(replaceString(media.getSource().substring(media.getSource().lastIndexOf("/")+1, media.getSource().length())));
			String title = replaceString(metaData.containsKey("title")?Util.convert((String)metaData.get("title"), "windows-1252", "GBK"):"");
			String artist = replaceString(metaData.containsKey("artist")?Util.convert((String)metaData.get("artist"), "windows-1252", "GBK"):"");
			String album = replaceString(metaData.containsKey("album")?Util.convert((String)metaData.get("album"), "windows-1252", "GBK"):"");
			String genre = replaceString(metaData.containsKey("genre")?Util.genre((String)metaData.get("genre")):"");
			double duration = media.getDuration().toMillis();
			
			songInfos.add(new SongInfo(media, name, title, artist, album, genre, duration, true));
			//System.out.println(playList);
		});
	}		
	
	/**
	 * Initial the visual list
	 */
	private void initList() {
		listP = new VBox(10);
		listP.setAlignment(Pos.CENTER);
		
		noPlayList = new Label("No Play List");
		noPlayList.setId("no-list");
		
		playListTable = new TableView<>();
		
		TableColumn<SongInfo, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(new PropertyValueFactory<>("name"));
		name.setPrefWidth(350);
		TableColumn<SongInfo, String> title = new TableColumn<>("Title");
		title.setCellValueFactory(new PropertyValueFactory<>("title"));
		title.setPrefWidth(160);
		TableColumn<SongInfo, String> artist = new TableColumn<>("Artist");
		artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
		artist.setPrefWidth(120);
		TableColumn<SongInfo, String> duration = new TableColumn<>("Duration");
		duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
		duration.setPrefWidth(110);
		TableColumn<SongInfo, String> album = new TableColumn<>("Album");
		album.setCellValueFactory(new PropertyValueFactory<>("album"));
		album.setPrefWidth(160);
		TableColumn<SongInfo, String> genre = new TableColumn<>("Genre");
		genre.setCellValueFactory(new PropertyValueFactory<>("genre"));
		genre.setPrefWidth(100);
		
		playListTable.getColumns().addAll(name, title, artist, duration, album, genre);
		playListTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		playListTable.setItems(songInfos);
		
		playListTable.setOnMouseClicked(e -> {
			if(e.getClickCount() == 2) {
				changeSong(playListTable.getSelectionModel().getSelectedItem().getMedia(), true);
			}
		});
		
		listP.getChildren().addAll(noPlayList);
		VBox.setVgrow(playListTable, Priority.ALWAYS);
		mainP.setCenter(listP);
	}
	
	/**
	 * Change the player's media
	 * @param media the new media
	 * @param autoplay whether to play the media after ready
	 */
	private void changeSong(Media media, boolean autoplay) {
		if(player != null) {
			player.stop();
		}
		
		player = new MediaPlayer(media);
		player.setVolume(volume/MAX_VOLUME);
		player.setOnReady(() -> {
			progressS.setMin(0);
			progressS.setMax(player.getMedia().getDuration().toMillis());
			totalLength = Util.parseDuration(player.getMedia().getDuration().toMillis(), false);
			progressL.setText("00:00/" + totalLength);
			progressS.setOnMousePressed(e -> {
				if(player.getStatus() == MediaPlayer.Status.PLAYING) {
					player.pause();
					beforePlaying = true;
				} else {
					beforePlaying = false;
				}
			});
			progressS.valueProperty().addListener((e,oldVal,newVal) -> {
				progressS.setValue(newVal.doubleValue());
				progressL.setText(Util.parseDuration(newVal.doubleValue(), false) + "/" + totalLength);
			});
			progressS.setOnMouseReleased(e -> {
				player.setStartTime(Duration.millis(progressS.getValue()));
				if(beforePlaying) {
					player.play();
				}
			});
			
			player.setOnPlaying(() -> {
				Thread t = new Thread(updateT);
				t.start();
			});
			
			player.setOnEndOfMedia(() -> {
				moveSong(1);
			});
			
			if(autoplay) {
				play();
			}
		});
	}
	
	/**
	 * Play the previous song or next song
	 * @param direction -1 indicate previous song, 1 indicate next song
	 */
	private void moveSong(int direction) {
		if(player == null) return;
		int index = -1;
		for(int i = 0; i < songInfos.size(); i++) {
			if(songInfos.get(i).getMedia() == player.getMedia()) {
				index = i+direction;
			}
		}
		if(index < 0) {
			index = songInfos.size()-1;
		} else if(index >= songInfos.size()) {
			index = 0;
		}
		
		changeSong(songInfos.get(index).getMedia(), true);
		playListTable.getSelectionModel().clearAndSelect(index);
	}
	
	/**
	 * Initial the setting file
	 */
	private void initSetting() {
		if(settingFile.exists()) {
			Scanner sc = null;
			try {
				sc = new Scanner(settingFile);
				while(sc.hasNextLine()) {
					String s= sc.nextLine();
					if(s.startsWith("Volume:")) {
						volumeS.setValue(Double.parseDouble(s.substring(7)));
					} else if(s.startsWith("Folder:")) {
						songFolder = new File(s.substring(7));
					}
				}
			} catch(FileNotFoundException e) {
				System.err.println("Reading setting file error");
				e.printStackTrace();
			} catch(NumberFormatException e) {
				System.err.println("Setting data changed");
			} finally {
				if(sc != null) {
					sc.close();
				}
			}
		}
	}

	
	// Control methods
	// Play current song
	private void play() {
		if(player == null) return;
		play.setGraphic(pauseI);
		player.play();
		progressBox.setVisible(true);
		
		for(int i = 0; i < songInfos.size(); i++) {
			if(songInfos.get(i).getMedia() == player.getMedia()) {
				nowPlayingL.setText("Now playing: " + songInfos.get(i).getName());
			}
		}
	}
	
	// Pause current song
	private void pause() {
		if(player == null) return;
		player.pause();
		play.setGraphic(playI);
	}
	
	
	// Helper methods
	/**
	 * Change some URI characters to file path characters.
	 * Warning: This only works for some of the characters, need more general method.
	 * @param input input URI String
	 * @return changed String
	 */
	private String replaceString(String input) {
		return input.replace("%20", " ").replace("%5B", "[").replace("%5D", "]");
	}
	
	/**
	 * Shorten the name to remove .mp3 and the identifier of musics downloaded from my online player
	 * @param input input String
	 * @return trimmed String
	 */
	private String shortenName(String input) {
		return input.replace(" [mqms2].mp3", "");
	}
	
	/**
	 * Driver method
	 * @param args not used
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}
}
