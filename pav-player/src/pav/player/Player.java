package pav.player;

import java.awt.FileDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import ddf.minim.AudioListener;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;

/**
 * A simple audio player based on Processing with PAV support.
 * 
 * @author christopher
 */
public class Player extends PApplet
{
	/**
	 * Player status - Unknown status
	 */
	public static final int STATUS_UNKNOWN = 0;
	
	/**
	 * Player status - Playback is stopped.
	 */
	public static final int STATUS_STOPPED = 1;
	
	/**
	 * Player status - Playback is currently paused.
	 */
	public static final int STATUS_PAUSED = 2;
	
	/**
	 * Player status - The player is currently playing, PAV support is active.
	 */
	public static final int STATUS_PLAYING_PAV = 3;
	
	/**
	 * Player status - The player is currently playing, PAV support is inactive.
	 */
	public static final int STATUS_PLAYING_NOPAV = 4;
	
	private static final long serialVersionUID = -4078221446680493121L;
		
	private final Menu _menu;
	private final Playlist _playlist;
	private final StatusBar _statusBar;
	private final PAVControl _pavControl;
	private final SongFilenameFilter _filter;
	private final Thread _pavControlThread;
	private final BlockingDeque<float[]> _sampleQueue;

	private Minim _minim;
	private AudioPlayer _player;
	private MusicListener _listener;
	
	private File _songPath;
	private Integer _playing;
	private boolean _playbackPausedByUser;
	private final TreeMap<Integer, Song> _songs;
	private final TreeMap<String, Album> _albums;
	
	private boolean _forceRedraw;
	private int _lastWidth, _lastHeight;
	
	/**
	 * Ctor.
	 */
	public Player()
	{
		_filter = new SongFilenameFilter();
		
		_menu = new Menu();
		_playlist = new Playlist();
		_statusBar = new StatusBar();
		_pavControl = new PAVControl();
		_sampleQueue = new LinkedBlockingDeque<float[]>(5);
		_pavControlThread = new Thread(_pavControl, "PAVControl");

		_songs = new TreeMap<Integer, Player.Song>();
		_albums = new TreeMap<String, Player.Album>();
		
		_setSongPath(new File(System.getProperty("user.dir")));
		
		_pavControlThread.start();
	}

	/**
	 * Internal method called by Processing. Not to be called from outside.
	 */
	@Override
	public void setup()
	{
		size(Config.width, Config.height, Config.renderer);
		frameRate(15);
		smooth();
		
		frame.setTitle("PAV Player");
		frame.setResizable(Config.resizable);
		
		WindowListener[] listeners = frame.getWindowListeners();

		for(int i = 0; i < listeners.length; i++) {
			frame.removeWindowListener(listeners[i]);
		}
		
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				exit();
			}
		});
		
		_minim = new Minim(this);
	}
	
	/**
	 * Closes the player.
	 */
	@Override
	public void exit()
	{
		Console.out("Shutting down ...");
		
		stopPlayback();		
		_minim.stop();
		_pavControlThread.interrupt();
		
		try {
			_pavControlThread.join(500);
		}
		catch (InterruptedException e) { }

		super.exit();
	}
	
	/**
	 * Internal method called by Processing. Not to be called from outside.
	 */
	@Override
	public void draw()
	{
		_menu.draw();
		_playlist.draw();
		_statusBar.draw();
		
		// the player length and position methods are very inaccurate, so we are using this "hack"
		if(_player != null && ! _playbackPausedByUser && ! _player.isPlaying()) {
			playNext();
		}
		
		_lastWidth = width;
		_lastHeight = height;
	}
	
	/**
	 * Internal method called by Processing. Not to be called from outside.
	 */
	@Override
	public void mousePressed()
	{
		int button = _menu.onMousePressed();
		
		switch(button) {
			case 0 :
				break;
			case Menu.BUTTON_OPEN :
				_selectSongPath();
				break;
			case Menu.BUTTON_PREV :
				playPrevious();
				break;
			case Menu.BUTTON_NEXT :
				playNext();
				break;
			case Menu.BUTTON_STOP :
				stopPlayback();
				break;
			case Menu.BUTTON_PLAY :
				_togglePlayback();
				break;
		}
		
		_playlist.onMousePressed();
	}
	
	/**
	 * Internal method called by Processing. Not to be called from outside.
	 */
	@Override
	public void mouseReleased()
	{
		_playlist.onMouseReleased();
	}
	
	/**
	 * Internal method called by Processing. Not to be called from outside.
	 */
	@Override
	public void mouseDragged()
	{
		_playlist.onMouseDragged();
	}
			
	/**
	 * Returns true if the player is currently playing music.
	 * 
	 * @return True if playing music
	 */
	public boolean isPlaying()
	{
		return _player != null && _player.isPlaying();
	}
	
	/**
	 * Returns true if there is a queued song, but playback has been paused by the user.
	 * 
	 * @return True if playback is paused
	 */
	public boolean isPlaybackPaused()
	{
		return _playbackPausedByUser;
	}
	
	/**
	 * Returns true if there is no song playing or queued.
	 * 
	 * @return True if playback is stopped
	 */
	public boolean isPlaybackStopped()
	{
		return ! isPlaying() && ! _playbackPausedByUser;
	}
	
	/**
	 * Returns the playback status. See STATUS constants of this class.
	 * 
	 * @return The playback status
	 */
	public int getStatus()
	{
		if(isPlaying()) {
			return (_pavControl.isActive()) ? STATUS_PLAYING_PAV : STATUS_PLAYING_NOPAV;
		}
		
		if(isPlaybackPaused()) {
			return STATUS_PAUSED;
		}
		
		if(isPlaybackStopped()) {
			return STATUS_STOPPED;
		}
		
		return STATUS_UNKNOWN;
	}
	
	/**
	 * Plays the next song on the playlist. Does nothing if the playlist is empty.
	 */
	public void playNext()
	{
		if(_songs.isEmpty()) {
			return;
		}
		
		if(_playing == null) {
			_play(_songs.firstKey());
		}
		else {
			Integer next = _songs.higherKey(_playing);
			
			if(next != null) {
				_play(next);
			}
			else {
				_play(_songs.firstKey());
			}
		}
	}
	
	/**
	 * Plays the previous song on the playlist. Does nothing if the playlist is empty.
	 */
	public void playPrevious()
	{
		if(_songs.isEmpty()) {
			return;
		}
		
		if(_playing == null) {
			_play(_songs.lastKey());
		}
		else {
			Integer next = _songs.lowerKey(_playing);
			
			if(next != null) {
				_play(next);
			}
			else {
				_play(_songs.lastKey());
			}
		}
	}
	
	/**
	 * Stops playback and dequeues the playing song, if any.
	 */
	public void stopPlayback()
	{
		_playing = null;
		_playbackPausedByUser = false;
		
		if(_player != null) {
			_player.removeListener(_listener);
			_player.close();
			_player = null;
			_listener = null;
			
			// icetea's pulseaudio threads sometimes crashes when closing the player
		}
	}
	
	private void _selectSongPath()
	{
		FileDialog dialog = new FileDialog(this.frame, "Select Folder");
		dialog.setDirectory(_songPath.getAbsolutePath());
		
		// filters in file dialog are bugged
		
		noLoop();
		dialog.setVisible(true);
		loop();
		
		String dir = dialog.getDirectory();
		dialog.dispose();
		
		if(dir == null) {
			return;
		}
		
		File f = new File(dir);
		
		if(f.isDirectory()) {
			_setSongPath(f);
		}
		else {
			if(f.getParentFile() != null) {
				_setSongPath(f.getParentFile());	
			}
		}
	}
	
	private void _togglePlayback()
	{
		if(isPlaying()) {
			_player.pause();
			_playbackPausedByUser = true;
		}
		else if(isPlaybackPaused()) {
			_player.play();
			_playbackPausedByUser = false;
			
		}
		else {
			playNext();
		}
	}
	
	private void _play(int id)
	{
		if(! _songs.containsKey(id)) {
			return;
		}
		
		stopPlayback();
		
		_player = _minim.loadFile(_songs.get(id).path.getAbsolutePath(), Config.frameSize);
		_player.play();
		_listener = new MusicListener();
		_player.addListener(_listener);

		_playing = id;		
		_pavControl.onPlaybackStarted();
	}
	
	private void _setSongPath(File path)
	{
		stopPlayback();
		
		_songPath = path;
		_update();
	}
	
	private void _findSongs(File path, LinkedList<Song> addTo)
	{
		File[] songPaths = path.listFiles(_filter);
		
		for(File p : songPaths) {
			Song song = new Song();
			song.path = p;
			
			try {
				AudioFile f = AudioFileIO.read(p);
				Tag tag = f.getTag();

				song.artist = tag.getFirst(FieldKey.ARTIST);
				song.album = tag.getFirst(FieldKey.ALBUM);
				song.title = tag.getFirst(FieldKey.TITLE);
				song.track = tag.getFirst(FieldKey.TRACK);
			}
			catch(Exception e) { }
			finally {
				if(song.artist == null || song.artist.isEmpty()) {
					song.artist = "Unknown";
				}
				if(song.album == null || song.album.isEmpty()) {
					song.album = "Unknown";
				}
				if(song.title == null || song.title.isEmpty()) {
					song.title = p.getName();
				}
				if(song.track == null || song.track.isEmpty()) {
					song.track = "?";
				}
				
				addTo.add(song);
			}
		}
	}
	
	private void _update()
	{
		_songs.clear();
		_albums.clear();
		LinkedList<Song> songs = new LinkedList<Song>();
		
		_findSongs(_songPath, songs);
		
		for(File s : _songPath.listFiles()) {
			if(s.isDirectory()) {
				_findSongs(s, songs);
			}
		}
		
		Collections.sort(songs);
		int i = 1;
		
		for(Song s : songs) {
			String hash = s.artist + "-" + s.album;
			Album album;
			
			if(! _albums.containsKey(hash)) {
				album = new Album();
				album.artist = s.artist;
				album.title = s.album;
				
				_albums.put(hash, album);
			}
			else {
				album = _albums.get(hash);
			}
			
			album.songs.add(i);
			_songs.put(i, s);
			i++;
		}
	}
	
	private boolean _sizeChanged()
	{
		return !(width == _lastWidth && height == _lastHeight);
	}
	
	/**
	 * Filename filter for supported encodings.
	 * 
	 * @author christopher
	 */
	private class SongFilenameFilter implements FilenameFilter
	{
		@Override
		public boolean accept(File file, String name)
		{
			File f = new File(file, name);
			String n = name.toLowerCase();
			
			if(!f.isFile()) {
				return false;
			}
			
			if(n.endsWith(".mp3")) {
				return true;
			}
			
			if(n.endsWith(".wav")) {
				return true;
			}
			
			if(n.endsWith(".aiff")) {
				return true;
			}
			
			if(n.endsWith(".au")) {
				return true;
			}
			
			if(n.endsWith(".snd")) {
				return true;
			}
			
			return false;
		}
	}
	
	/**
	 * Listens to music played via Minims AudioPlayer and queues received frames for PAV.
	 * 
	 * @author christopher
	 */
	private class MusicListener implements AudioListener
	{
		private float[] _samples = {};
		
		@Override
		public void samples(float[] samples)
		{
			if(!_pavControl.isActive()) return;
			
			if(! _sampleQueue.offerLast(samples)) {
				Console.error("Failed to add sample to PAV queue.");
			}
		}

		@Override
		public void samples(float[] left, float[] right)
		{
			if(!_pavControl.isActive()) return;
			
			int len = left.length;
			
			if(_samples.length != len) {
				_samples = new float[len];
			}
			
			for(int i = 0; i < len; i++) {
				_samples[i] = (left[i] + right[i]) / 2;
			}
			
			samples(_samples);
		}
	}
	
	/**
	 * PAV control.
	 * 
	 * @author christopher
	 */
	private class PAVControl implements Runnable
	{
		private Socket _control, _data;
		private PrintWriter _controlOut;
		private BufferedReader _controlIn;
		private ObjectOutputStream _dataOut;
		private float _lastSampleRate;
		private boolean _active;
		
		@Override
		public void run()
		{
			if(! Config.usePav) {
				return;
			}
			
			try {
				_control = new Socket(InetAddress.getByName(Config.pavHost), Config.pavPort);
				_controlOut = new PrintWriter(_control.getOutputStream(), true);
				_controlIn = new BufferedReader(new InputStreamReader(_control.getInputStream()));
				
				String[] in = _controlIn.readLine().split(" ");
				
				if(in.length == 2 && in[0].equals("!ok")) {
					_data = new Socket(InetAddress.getByName(Config.pavHost), Integer.parseInt(in[1]));
					_dataOut = new ObjectOutputStream(_data.getOutputStream());
					_active = true;
				}
				else {
					throw new IOException("Received an invalid welcome message.");
				}
			}
			catch (Exception e) {
				Console.error("An error occurred while trying to connect to PAV:");
				Console.error(e);
				_tryClose();
				return;
			}
			
			try {
				while(true) {					
					float[] sample = _sampleQueue.takeLast();

					if(_sampleQueue.remainingCapacity() == 1) {
						_sampleQueue.clear();
					}
					
					_dataOut.writeObject(sample);
					_dataOut.flush();
					_dataOut.reset();
				}
			}
			catch (InterruptedException e) { }
			catch (IOException e) {
				Console.error("An error occured while sending data to PAV ... stopping PAV support.");
			}
			finally {
				_active = false;
				_tryClose();
			}
		}
		
		/**
		 * Whether PAV support is currently active.
		 * 
		 * @return True if PAV support is active
		 */
		public boolean isActive()
		{
			return _active;
		}
		
		/**
		 * Event handler - Audio playback started.
		 */
		public void onPlaybackStarted()
		{
			if(! isActive()) {
				return;
			}
			
			if(_player.sampleRate() != _lastSampleRate) {
				_controlOut.println("!sr " + _player.sampleRate());
				_lastSampleRate = _player.sampleRate();
			}
		}
		
		private void _tryClose()
		{
			if(_controlOut != null) {
				_controlOut.println("!close");
				_controlOut.close();
			}
			
			if(_dataOut != null) try { _dataOut.close(); } catch (IOException e) { }
			if(_data != null) try { _data.close(); } catch (IOException e) { }
			if(_controlIn != null) try { _controlIn.close(); } catch (IOException e) { }
			if(_control != null) try { _control.close(); } catch (IOException e) { }
		}
	}
	
	/**
	 * A music album.
	 * 
	 * @author christopher
	 */
	private class Album
	{
		/**
		 * The album artist.
		 */
		public String artist;
		
		/**
		 * The album title.
		 */
		public String title;
		
		/**
		 * The ids of all songs of the album.
		 */
		public LinkedList<Integer> songs;
		
		/**
		 * Ctor.
		 */
		public Album()
		{
			songs = new LinkedList<Integer>();
		}
	}
	
	/**
	 * A song.
	 * 
	 * @author christopher
	 */
	private class Song implements Comparable<Song>
	{			
		/**
		 * The song artist.
		 */
		public String artist;
		
		/**
		 * The song album.
		 */
		public String album;
		
		/**
		 * The song title.
		 */
		public String title;
		
		/**
		 * The track number.
		 */
		public String track;
		
		/**
		 * The path to the song.
		 */
		public File path;

		@Override
		public int compareTo(Song o)
		{
			if(! artist.equals(o.artist)) {
				return artist.compareTo(o.artist);
			}
			
			if(! album.equals(o.album)) {
				return album.compareTo(o.album);
			}
			
			try {
				int tt = Integer.parseInt(track);
				int to = Integer.parseInt(o.track);
				
				return (tt < to) ? -1 : 1;
			}
			catch(NumberFormatException e) {
				return 0;
			}
		}
	}
	
	/**
	 * The playlist.
	 * 
	 * @author christopher
	 */
	private class Playlist
	{
		private final PFont _font;
		private PGraphics _buffer;
		private Integer _lastPlayed;
		private File _lastPath;
		private int _lastNumAlbums;
		private int _lastNumSongs;
		private float _dy;
		private int _space;
		private float _sliderStart;
		private float _lastSliderStart;
		private boolean _sliderDragging;
		private float _dragLastY;

		/**
		 * Ctor.
		 */
		public Playlist()
		{
			_font = createFont("sans", 11);
		}
		
		/**
		 * Draws the playlist.
		 */
		public void draw()
		{
			if(! _forceRedraw && _sliderStart == _lastSliderStart && _playing == _lastPlayed && _songPath != null && _songPath.equals(_lastPath) && !_sizeChanged()) {
				return;
			}
			
			fill(0);
			noStroke();
			rectMode(CORNERS);
			rect(0, 40, width, height - 20);
			
			_drawPath();
			_drawPlaylist();
			
			_lastPlayed = _playing;
			_lastPath = _songPath;
		}
		
		/**
		 * Event handler - mouse pressed.
		 */
		public void onMousePressed()
		{
			if(_dy > 1) {
				return;
			}
			
			float y1 = 66 + _sliderStart * _space;

			if(width - 8 <= mouseX && mouseX <= width - 3 && y1 <= mouseY && mouseY <= y1 + _space * _dy) {
				_sliderDragging = true;
				_dragLastY = mouseY;
			}
		}
		
		/**
		 * Event handler - mouse released.
		 */
		public void onMouseReleased()
		{
			_sliderDragging = false;
		}
		
		/**
		 * Event handler - mouse dragged.
		 */
		public void onMouseDragged()
		{
			if(_sliderDragging) {
				float dy = mouseY - _dragLastY;
				
				if(dy == 0) {
					return;
				}
				
				float dyr = dy / _space;
				float sn = _sliderStart + dyr;
				
				if(sn < 0) {
					sn = 0;
				}
				else if(sn > 1 - _dy) {
					sn = 1 - _dy;
				}
				
				_lastSliderStart = _sliderStart;
				_sliderStart = sn;
				_dragLastY = mouseY;
			}
		}
		
		private void _drawPath()
		{
			rectMode(CORNER);
			noStroke();
			fill(50);
			rect(0, 40, width, 22);
			textAlign(RIGHT, CENTER);
			textFont(_font);
			fill(175);
			text(_songPath.toString() + "/", width - 6, 49);
		}
		
		private void _drawPlaylist()
		{
			int numAlbums = _albums.size();
			int numSongs = _songs.size();
			int heightPerAlbum = 16;
			int heightPerSong = 14;
			
			if(_buffer == null || numAlbums != _lastNumAlbums || numSongs != _lastNumSongs || _sizeChanged()) {
				if(_buffer != null) {
					_buffer.dispose();
				}
				
				int bufferHeight = numAlbums * heightPerAlbum + numSongs * heightPerSong;
				
				if(bufferHeight == 0) {
					bufferHeight = 1;
				}
				
				_buffer = createGraphics(width, bufferHeight, JAVA2D);
				_buffer.textAlign(LEFT);
				_buffer.textFont(_font);
				
				_lastNumAlbums = numAlbums;
				_lastNumSongs = numSongs;
				_sliderStart = 0;
			}
			
			if(_forceRedraw || _playing != _lastPlayed || (_songPath != null && ! _songPath.equals(_lastPath)) || _sizeChanged())
			{
				int y = 11;
				_buffer.beginDraw();
				_buffer.background(0);
				
				for(Album album : _albums.values()) {
					_buffer.fill(200);
					_buffer.text(album.artist + " - " + album.title, 5, y);
					y += heightPerAlbum;
		
					for(int id : album.songs) {
						_buffer.fill(100);
						
						if(_playing != null && id == _playing) {
							_buffer.fill(0xFF00FF00);
						}
						
						Song s = _songs.get(id);
						String track = (s.track.length() == 1 && !s.track.equals("?")) ? "0" + s.track : s.track;
						_buffer.text(track + " - " + s.title, 15, y);
						y += heightPerSong;
					}
				}
				
				_buffer.endDraw();
			}
			
			_space = height - 66 - 24;
			_dy = _space / (float) _buffer.height;

			if(_buffer.height <= _space) {
				image(_buffer, 0, 66);
				return;
			} else {
				float yStart = ((_buffer.height - _space) / (1 - _dy)) * _sliderStart;
				copy(_buffer, 0, Math.round(yStart), width, _space, 0, 66, width, _space);

				rectMode(CORNER);
				noStroke();
				fill(40);
				rect(width - 10, 66 + _sliderStart * _space, 7, _space * _dy);
			}
		}
	}
	
	/**
	 * The status bar.
	 * 
	 * @author christopher
	 */
	private class StatusBar
	{
		private final PFont _font;
		private int _lastStatus;
		
		/**
		 * Ctor.
		 */
		public StatusBar()
		{
			_font = createFont("sans", 11);
		}
		
		/**
		 * Draws the status bar.
		 */
		public void draw()
		{
			int status = getStatus();
			
			if(! _forceRedraw && status == _lastStatus && !_sizeChanged()) {
				return;
			}
						
			rectMode(CORNER);
			noStroke();
			fill(50);
			rect(0, height - 20, width, 20);
			textAlign(LEFT);
			textFont(_font);
			fill(175);
			
			int x = 5, y = height - 6;
			
			switch(status) {
				case STATUS_PLAYING_PAV :
					text("Playing. Streaming to " + Config.pavHost + ":" + Config.pavPort + ".", x, y);
					break;
				case STATUS_PLAYING_NOPAV :
					text("Playing. PAV support inactive.", x, y);
					break;
				case STATUS_PAUSED :
					text("Paused.", x, y);
					break;
				case STATUS_STOPPED :
					text("Stopped.", x, y);
					break;
			}
			
			_lastStatus = status;
		}
	}
	
	/**
	 * The player menu.
	 * 
	 * @author christopher
	 */
	private class Menu
	{
		/**
		 * The open button.
		 */
		public static final int BUTTON_OPEN = 1;
		
		/**
		 * The previous button.
		 */
		public static final int BUTTON_PREV = 2;
		
		/**
		 * The next button.
		 */
		public static final int BUTTON_NEXT = 3;
		
		/**
		 * The stop button.
		 */
		public static final int BUTTON_STOP = 4;
		
		/**
		 * The play button.
		 */
		public static final int BUTTON_PLAY = 5;
		
		private int _lastMouseOverStatus;
		private final HashMap<Integer, Button> _buttons;
		private final int _height = 40;
		private final PFont _font;
		
		/**
		 * Ctor.
		 */
		public Menu()
		{
			_buttons = new HashMap<Integer, Button>(5);
			_buttons.put(BUTTON_OPEN, new Button());
			_buttons.put(BUTTON_PREV, new Button());
			_buttons.put(BUTTON_NEXT, new Button());
			_buttons.put(BUTTON_STOP, new Button());
			_buttons.put(BUTTON_PLAY, new Button());
			
			_buttons.get(BUTTON_OPEN).setLabel("OPEN");
			_buttons.get(BUTTON_PREV).setLabel("PREV");
			_buttons.get(BUTTON_NEXT).setLabel("NEXT");
			_buttons.get(BUTTON_STOP).setLabel("STOP");
			_buttons.get(BUTTON_PLAY).setLabel("PLAY");
			
			_font = createFont("sans", 12);
		}
		
		/**
		 * Draws the menu.
		 */
		public void draw()
		{
			int mouseOverStatus = onMousePressed();
			
			if(! _forceRedraw && mouseOverStatus == _lastMouseOverStatus && !_sizeChanged()) {
				return;
			}
			
			float xd = width / 5.0f;
			
			if(_sizeChanged()) {
				float x = 0;
				
				_buttons.get(BUTTON_OPEN).setArea(x, 0, x + xd, _height);
				x += xd;
				_buttons.get(BUTTON_PREV).setArea(x, 0, x + xd, _height);
				x += xd;
				_buttons.get(BUTTON_NEXT).setArea(x, 0, x + xd, _height);
				x += xd;
				_buttons.get(BUTTON_STOP).setArea(x, 0, x + xd, _height);
				x += xd;
				_buttons.get(BUTTON_PLAY).setArea(x, 0, x + xd, _height);
				x += xd;
			}
			
			for(Button b : _buttons.values()) {
				b.draw();
			}
			
			strokeWeight(1);
			stroke(50);
			
			for(int i = 1; i < 5; i++) {
				line(i * xd, 0, i * xd, _height - 1);
			}
			
			_lastMouseOverStatus = mouseOverStatus;
		}
		
		/**
		 * Mouse pressed event handler. 
		 * Returns 0 if the mouse was not pressed over a menu button,
		 * otherwise the id of that button is returned (see constants of this class).
		 * 
		 * @return
		 */
		public int onMousePressed()
		{
			if(mouseY > _height) {
				return 0;
			}
			
			if(_buttons.get(BUTTON_OPEN).isMouseOver()) {
				return BUTTON_OPEN;
			}
			
			if(_buttons.get(BUTTON_PREV).isMouseOver()) {
				return BUTTON_PREV;
			}
			
			if(_buttons.get(BUTTON_NEXT).isMouseOver()) {
				return BUTTON_NEXT;
			}
			
			if(_buttons.get(BUTTON_STOP).isMouseOver()) {
				return BUTTON_STOP;
			}
			
			if(_buttons.get(BUTTON_PLAY).isMouseOver()) {
				return BUTTON_PLAY;
			}
			
			return 0;
		}
		
		/**
		 * A menu button.
		 * 
		 * @author christopher
		 */
		private class Button
		{
			private String _label;
			private float _x1, _y1, _x2, _y2;
			
			/**
			 * Sets the label to use.
			 * 
			 * @param label The label
			 */
			public void setLabel(String label)
			{
				_label = label;
			}
			
			/**
			 * Sets the button area.
			 * 
			 * @param x1 The x1 position
			 * @param y1 The y1 position
			 * @param x2 The x2 position. Must be > x1
			 * @param y2 The y2 position. Must be > y2
			 */
			public void setArea(float x1, float y1, float x2, float y2)
			{
				_x1 = x1;
				_y1 = y1;
				_x2 = x2;
				_y2 = y2;
			}
			
			/**
			 * Draws the button.
			 */
			public void draw()
			{
				if(isMouseOver()) {
					fill(25);
				}
				else {
					fill(40);
				}
				
				rectMode(CORNER);
				noStroke();
				rect(_x1, _y1, _x2, _y2);
				
				if(_label != null) {
					fill(255);
					textFont(_font);
					textAlign(CENTER, CENTER);
					text(_label, _x1 + (_x2 - _x1) / 2, (_y2 - _y1) / 2);
				}
			}
			
			/**
			 * Whether the mouse is currently over the button.
			 * 
			 * @return True if the mouse is over the button, otherwise false
			 */
			public boolean isMouseOver()
			{
				return _x1 <= mouseX && mouseX <= _x2 && _y1 <= mouseY && mouseY <= _y2;
			}
		}	
	}
}
