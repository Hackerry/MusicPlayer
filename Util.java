import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnmappableCharacterException;

/**
 * Util class
 * @author Hackerry
 *
 */
public class Util {
	/**
	 * Convert a String from a charset to another charset
	 * @param input String to be mapped
	 * @param encode encode charset
	 * @param decode decode charset
	 * @return converted String
	 */
	public static String convert(String input, String encode, String decode) {
		try {
			CharsetEncoder encoder = Charset.forName(encode).newEncoder();
			ByteBuffer buff = encoder.encode(CharBuffer.wrap(input.toCharArray()));
			return new String(buff.array(), Charset.forName(decode));
		} catch (UnmappableCharacterException e) {
			return input;
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Parse a millisecond duration to String HH:mm:ss
	 * @param duration duration milliseconds
	 * @param detail whether show milliseconds remaining
	 * @return converted String
	 */
	public static String parseDuration(double duration, boolean detail) {
		int hour = (int)(duration/(1000*3600));
		int min = (int)((duration-=(hour*1000*3600))/(1000*60));
		int sec = (int)(duration-=min*1000*60)/(1000);
		int milli = (int)(duration-=sec*1000);
		
		//System.out.println(hour + ":" + min + ":" + sec + ":" + milli);
		if(hour == 0) {
			if(detail) {
				return String.format("%02d:%02d:%02d", min, sec, milli);
			} else {
				return String.format("%02d:%02d", min, sec);
			}
		} else {
			if(detail) {
				return String.format("%02d:%02d:%02d:%02d", hour, min, sec, milli);
			} else {
				return String.format("%02d:%02d", min, sec);
			}
		}
	}

	public static final String[] list = new String[] { "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk",
			"Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock",
			"Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno",
			"Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid",
			"House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space",
			"Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial",
			"Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
			"Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave",
			"Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical",
			"Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin",
			"Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock",
			"Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
			"Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire",
			"Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle",
			"Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass",
			"Club-House", "Hardcore", "Terror", "Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat",
			"Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary C", "Christian Rock",
			"Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop" };
	
	/**
	 * Get the genre of a song
	 * @param genre integer String
	 * @return genre of the song
	 */
	public static String genre(String genre) {
		if (genre.equals("")) {
			return genre;
		}
		
		int number;
		number = Integer.parseInt(genre.substring(1, genre.length()-1));

		if (number >= list.length || number < 0) {
			return "Unknown";
		}
		return list[number];
	}
}
