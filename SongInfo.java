import javafx.scene.media.Media;

/**
 * JavaBean for a media and used in TableView
 * @author Hackerry
 *
 */
public class SongInfo {
	private Media media;
	private String name;
	private String title;
	private String artist;
	private String duration;
	private String album;
	private String genre;
	private boolean valid;

	public SongInfo(Media media, String name, String title, String artist, String album, String genre, double duration, boolean valid) {
		this.media = media;
		this.name = name;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.genre = genre;
		this.duration = Util.parseDuration(duration, true);
		this.valid = valid;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = Util.parseDuration(duration, true);
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public Media getMedia() {
		return media;
	}
	
	public String toString() {
		return title + " " + artist + " " + album + " " + duration + " " + valid;
	}
}
