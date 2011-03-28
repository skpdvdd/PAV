
## Processing Audio Visualization (PAV) ##

PAV is a music visualization project based on Processing/Java.

 - libpav is the visualization library
 - pav is an application that visualizes music from the network or a FIFO using libpav
 - pav-player is a simple audio player with tag support that can stream audio data to PAV over the network

For more information refer to http://skpdvdd.github.com/PAV/

PAV is released under the GPL v3 license. If you use PAV for non-private purposes please contact me first.

PAV is developed on Linux. If you find any bugs or experience problems (regardless of the OS you use) please contact me on github.

# Setup #

You need Java SDK and Ant installed on your system (it works with only Java RE if you manage to set all paths correctly).
The build.xml file specifies run configurations. There are predefined default configurations, "run" starts PAV in software/network mode while "run-gl" enables hardware/network mode. If you plan to use PAV with PAV-player or stream data to it via UDP you can use one of these configurations directly. Navigate to /PAV and enter "ant run" or "ant run-gl" (recommended) to start PAV. If you want to use a FIFO audio source or change other settings you need to edit build.xml. The following settings are available:

-renderer		The renderer to use. See predefined configurations. You can use any Processing renderer.
-width			The width of the window.
-height			The height of the window.
-resizable		Make the window resizable. Can cause problems with some renderers.
-audiosource	The audio source (udp (default) or fifo). fifo works only on *nix.
-samplesize		Number of audio samples per frame (512, 1024 or 2048).
-samplerate		Sample rate of the audio data.
-byteorder		Byte order of the samples (le (default) or be).
-path			Path to the fifo
-port			Port to listen to

PAV expects the audio data to be in short/uint8 (2 byte) mono format. You can specify the byte order and sample size on startup (see above), default is 44100:1024:16:1.

If you want to use PAV with a fifo audio source use -audiosource=fifo and specify the path to the fifo file (see run-fifo run configuration in build.xml). For example, to use PAV with MPD (http://mpd.wikia.com/) (like me) simply enable fifo output in your MPD config:

audio_output {
	type		"fifo"
	name		"MPD FIFO"
	path		"/tmp/mpd.fifo"
	format		"44100:16:1"
}

