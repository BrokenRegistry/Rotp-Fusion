/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.util.sound;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static javax.sound.sampled.FloatControl.Type.MASTER_GAIN;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import rotp.util.Base;

public class OggClip implements SoundClip, Base {
    private static OggMap loadedClips = new OggMap();
    private static OggMap[] delayClip = newOggMaps(4);
    Clip clip;
    private boolean loaded = false;
    private String filename;
    private float gain;
    private int position = 0;
    private boolean continuous = false;
    private String style = "";
    private float decay;
    private int msDelay, msHullDelay, shipSize;

    public static void clearDelayClips() {
    	for (OggMap map : delayClip) {
    		for (Entry<String, OggClip> entry : map.entrySet()) {
    			OggClip dc = entry.getValue();
    			if (dc.clip !=null) {
    				dc.clip.close();
    			}
    		}
    		map.clear();
    	}
    }
    private static OggMap[] newOggMaps(int size) {
    	OggMap[] mapArray = new OggMap[size];
    	for (int i=0; i<size; i++)
    		mapArray[i] = new OggMap(); 
    	return mapArray;
    }
    public static OggClip play(String fn, float clipGain, float masterVolume) {
        if (!loadedClips.containsKey(fn))
            loadedClips.put(fn, new OggClip(fn, clipGain));

        OggClip wc = loadedClips.get(fn);
        wc.setVolume(masterVolume);
        wc.play();
        return wc;
    }
    public static OggClip play(String fn, float clipGain, float masterVolume, int hullSize) {
        if (!delayClip[hullSize].containsKey(fn)) 
        	delayClip[hullSize].put(fn, new OggClip(fn, clipGain, hullSize));
        OggClip wc = delayClip[hullSize].get(fn);
        wc.setVolume(masterVolume);
        wc.play();
        return wc;
    }
    public static OggClip playContinuously(String fn, float clipGain, String s, float masterVolume) {
        if (!loadedClips.containsKey(fn))
            loadedClips.put(fn, new OggClip(fn, clipGain));

        OggClip wc = loadedClips.get(fn);
        wc.setVolume(masterVolume);
        wc.style = s;
        wc.playContinuously();
        return wc;
    }
    public static void setVolume(String fn, float vol) {
        if (!loadedClips.containsKey(fn))
            return;

        OggClip wc = loadedClips.get(fn);
        wc.setVolume(vol);
    }
    public OggClip(String fn, float vol) {
        filename = fn;
        gain = vol;
        loaded = false;

        AudioInputStream ais = null;
        AudioInputStream decoded = null;
        try {
            if (!loaded) {
                BufferedInputStream is = new BufferedInputStream(WavClip.wavFileStream(fn));

                ais = AudioSystem.getAudioInputStream(is);
                final int ch = ais.getFormat().getChannels();
                final float rate = ais.getFormat().getSampleRate();
                AudioFormat format = new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
                decoded = getAudioInputStream(format, ais);

				if (clip != null && clip.isOpen())
					clip.close();
                clip = AudioSystem.getClip();
                clip.open(decoded);
                if (vol < 1 && clip.isControlSupported(MASTER_GAIN)) {
                    log("setting gain for sound: " + fn + "  to " + (int) (gain * 100));
					FloatControl masteGain = (FloatControl) clip.getControl(MASTER_GAIN);
					masteGain.setValue(20f * (float) Math.log10(vol));
                }
                loaded = true;
                is.close();
            }
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.err.println(e.toString());
            System.err.println(e.getStackTrace());
        } finally {
            if (ais != null)
                try {
                    ais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (decoded != null)
                try {
                    decoded.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    private OggClip(String fn, float vol, int hullSize) { // BR:
        filename	= fn;
        gain		= vol;
        shipSize	= hullSize;
        msHullDelay	= options().echoSoundHullDelay();
        msDelay		= options().echoSoundDelay() + shipSize*msHullDelay;
        decay		= options().echoSoundDecay();
        loaded		= false;

        AudioInputStream ais = null;
        AudioInputStream decoded = null;
        try {
            if (!loaded) {
            	EchoFilter filter = new EchoFilter(msDelay*48, decay);
                BufferedInputStream is = new BufferedInputStream(WavClip.wavFileStream(fn));

                ais = AudioSystem.getAudioInputStream(is);
                final int ch = ais.getFormat().getChannels();
                final float rate = ais.getFormat().getSampleRate();
                AudioFormat format = new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
                decoded = getAudioInputStream(format, ais);
                decoded = new FilteredSoundStream(decoded, filter);
                clip = AudioSystem.getClip();
                clip.open(decoded);
                if (vol < 1 && clip.isControlSupported(MASTER_GAIN)) {
                    log("setting gain for sound: " + fn + "  to " + (int) (gain * 100));
                    FloatControl gain = (FloatControl) clip.getControl(MASTER_GAIN);
                    gain.setValue(20f * (float) Math.log10(vol));
                }
                loaded = true;
            }
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.err.println(e.toString());
            System.err.println(e.getStackTrace());
        } finally {
            if (ais != null)
                try {
                    ais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (decoded != null)
                try {
                    decoded.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void setVolume(float masterVolume) {
        if (!clip.isControlSupported(MASTER_GAIN))
            return;

        float volume = min(1.0f, masterVolume * gain);
        log("setting volume*gain for sound: " + filename + "  to " + (int) (volume * 100));
        FloatControl gain = (FloatControl) clip.getControl(MASTER_GAIN);
        gain.setValue(20f * (float) Math.log10(volume));
    }

    public void play() {
        clip.setFramePosition(position);
        clip.start();
    }

    public void playContinuously() {
        continuous = true;
        if (style.equals("L"))
            clip.setFramePosition(0);
        else {
            try {
                clip.setFramePosition(position);
            } catch (IllegalArgumentException e) {
                // thrown if invalid frame position
                clip.setFramePosition(0);
            }
        }
        clip.start();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    @Override
    public void pausePlaying() {
        position = clip.getFramePosition();
        clip.stop();
    }

    @Override
    public void resumePlaying() {
        clip.setFramePosition(position);
        clip.start();
        if (continuous)
            clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    @Override
    public void endPlaying() {
        position = clip.getFramePosition();
        clip.stop();
    }

    public static String changeExtension(String wavFile) {
            int idx = wavFile.lastIndexOf('.');
            String filenameWithoutExtension;
            if (idx > 0) {
                filenameWithoutExtension = wavFile.substring(0, idx);
            } else {
                filenameWithoutExtension = wavFile;
            }
            String filenameWithExtension = filenameWithoutExtension + ".ogg";
            return filenameWithExtension;
    }
}
class OggMap extends HashMap<String, OggClip> {
	private static final long serialVersionUID = 1L;
}
