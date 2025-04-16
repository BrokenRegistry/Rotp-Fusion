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

import static javax.sound.sampled.FloatControl.Type.MASTER_GAIN;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import rotp.Rotp;
import rotp.model.game.IGameOptions;
import rotp.util.Base;

public class WavClip  implements SoundClip, Base {
    private static WavMap loadedClips = new WavMap();
    private static WavMap[] delayClip = newWavMaps(4);
    private Clip clip;
    private boolean loaded = false;
    private String filename;
    private float gain;
    private int position = 0;
    private boolean continuous = false;
    private String style = "";
    private float decay;
    private int msDelay, msHullDelay, shipSize;
    
    public static void clearDelayClips() {
    	for (WavMap map : delayClip) {
    		for (Entry<String, WavClip> entry : map.entrySet()) {
    			WavClip dc = entry.getValue();
    			if (dc.clip !=null) {
    				dc.clip.close();
    			}
    		}
    		map.clear();
    	}
    }
    private static WavMap[] newWavMaps(int size) {
    	WavMap[] mapArray = new WavMap[size];
    	for (int i=0; i<size; i++)
    		mapArray[i] = new WavMap(); 
    	return mapArray;
    }
    public static WavClip play(String fn, float clipGain, float masterVolume) {
        if (!loadedClips.containsKey(fn)) 
            loadedClips.put(fn, new WavClip(fn, clipGain));
        WavClip wc = loadedClips.get(fn);
        wc.setVolume(masterVolume);
        wc.play();
        return wc;    		
    }
    static WavClip play(String fn, float clipGain, float masterVolume, int hullSize) { // BR:
        if (!delayClip[hullSize].containsKey(fn)) 
        	delayClip[hullSize].put(fn, new WavClip(fn, clipGain, hullSize));
        WavClip wc = delayClip[hullSize].get(fn);
        //System.out.println("msDelay: " + wc.msDelay + "  decay: " + wc.decay );
        wc.setVolume(masterVolume);
        wc.play();
        return wc;    		
    }
    public static WavClip playContinuously(String fn, float clipGain, String s, float masterVolume) {
         if (!loadedClips.containsKey(fn)) 
            loadedClips.put(fn, new WavClip(fn, clipGain));
        
        WavClip wc = loadedClips.get(fn);
        wc.setVolume(masterVolume);
        wc.style = s;
        wc.playContinuously();
        return wc;
    }          
    public static void setVolume(String fn, float vol) {
         if (!loadedClips.containsKey(fn))
             return;
        
        WavClip wc = loadedClips.get(fn);
        wc.setVolume(vol);
    }          
    public WavClip(String fn, float vol, int hullSize) { // BR:
        filename	= fn;
        gain		= vol;
        shipSize	= hullSize;
        IGameOptions opts = guiOptions();
        msHullDelay	= opts.echoSoundHullDelay();
        msDelay		= opts.echoSoundDelay() + shipSize*msHullDelay;
        decay		= opts.echoSoundDecay();
        loaded		= false;
        
        AudioInputStream ais = null;
        DataLine.Info info = null;
        try {
            if (!loaded) {
        		EchoFilter filter = new EchoFilter(msDelay*48, decay);
                BufferedInputStream is = new BufferedInputStream(wavFileStream(fn));
//                is = new FilteredSoundStream(is, filter);
                ais = AudioSystem.getAudioInputStream(is);
                info = new DataLine.Info(Clip.class, ais.getFormat());
                ais = new FilteredSoundStream(ais, filter);
                clip = (Clip)AudioSystem.getLine(info);
                clip.open(ais);
                if (vol < 1 && clip.isControlSupported(MASTER_GAIN)) {
                    log("setting gain for sound: "+filename+"  to "+(int)(gain*100));
                    FloatControl gain = (FloatControl) clip.getControl(MASTER_GAIN);
                    gain.setValue(20f * (float) Math.log10(vol));
                }
                loaded = true;
            }
        }
        catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.err.println("Error looking for DataLine.Info "+info);
            if (info != null && info.getFormats() != null && info.getFormats().length > 0) {
                for (AudioFormat f : info.getFormats()) {
                    System.err.println("Format channels="+f.getChannels()+" samplerate="+f.getSampleRate()
                            +" framerate="+f.getFrameRate()+" framesize="+f.getFrameSize()
                            +" encoding="+f.getEncoding()+" "+f);
                }
            } else {
                System.err.println("Error happened before determining input stream DataLine.Info details");
            }
            System.err.println(e.toString());
            System.err.println(e.getStackTrace());
        }
        finally {
            if (ais != null)
                try { ais.close(); } catch (IOException e) {}
        }
    }
    public WavClip(String fn, float vol) {
        filename = fn;
        gain = vol;
        loaded = false;
        
        AudioInputStream ais = null;
        DataLine.Info info = null;
        try {
            if (!loaded) {
                BufferedInputStream is = new BufferedInputStream(wavFileStream(fn));
                ais = AudioSystem.getAudioInputStream(is);
                info = new DataLine.Info(Clip.class, ais.getFormat());

                clip = (Clip)AudioSystem.getLine(info);
                clip.open(ais);
                if (vol < 1 && clip.isControlSupported(MASTER_GAIN)) {
                    log("setting gain for sound: "+filename+"  to "+(int)(gain*100));
                    FloatControl gain = (FloatControl) clip.getControl(MASTER_GAIN);
                    gain.setValue(20f * (float) Math.log10(vol));
                }
                loaded = true;
            }
        }
        catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.err.println("Error looking for DataLine.Info "+info);
            if (info != null && info.getFormats() != null && info.getFormats().length > 0) {
                for (AudioFormat f : info.getFormats()) {
                    System.err.println("Format channels="+f.getChannels()+" samplerate="+f.getSampleRate()
                            +" framerate="+f.getFrameRate()+" framesize="+f.getFrameSize()
                            +" encoding="+f.getEncoding()+" "+f);
                }
            } else {
                System.err.println("Error happened before determining input stream DataLine.Info details");
            }
            System.err.println(e.toString());
            System.err.println(e.getStackTrace());
        }
        finally {
            if (ais != null)
                try { ais.close(); } catch (IOException e) {}
        }
    }
    public void setVolume(float masterVolume) {
        if (!clip.isControlSupported(MASTER_GAIN))
            return;
        
        float volume = min(1.0f, masterVolume*gain);
        log("setting volume*gain for sound: "+filename+"  to "+(int)(volume*100));
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
            try { clip.setFramePosition(position); }
            catch(IllegalArgumentException e) {
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
    public static InputStream wavFileStream(String n) {
        String fullString = "../rotp/" +n;

        try { return new FileInputStream(new File(Rotp.jarPath(), n)); } 
        catch (FileNotFoundException e) {
                try { return new FileInputStream(fullString); } 
                catch (FileNotFoundException ex) {
                    return Rotp.class.getResourceAsStream(n);
                }
        }
    }
}
class WavMap extends HashMap<String, WavClip> {
	private static final long serialVersionUID = 1L;
}
