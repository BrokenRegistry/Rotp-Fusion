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
package rotp.util;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import rotp.Rotp;

public class LabelManager implements Base {
    static LabelManager instance = new LabelManager();
    public static LabelManager current()  { return instance; }
	public static boolean validate = false; // BR: for debug purpose
	private static String lastDir = "";

    private String labelFile = "labels.txt";
    private String dialogueFile = "dialogue.txt";
    private final String techsFile = "techs.txt";
    private String introFile = "intro.txt";
    private final HashMap<String,byte[]> labelMap = new HashMap<>();
    private final HashMap<String,List<String>> dialogueMap = new HashMap<>();
    private final List<String> introLines = new ArrayList<>();

    public boolean hasLabel(String key)    { return labelMap.containsKey(key); }
    public boolean hasDialogue(String key) { return dialogueMap.containsKey(key); }
    public boolean hasIntroduction()       { return !introLines.isEmpty(); }
    public List<String> introduction()     { return introLines; }
    
    public void dialogueFile(String s)    { dialogueFile = s; }
    public void labelFile(String s)       { labelFile = s; }
    public void introFile(String s)       { introFile = s; }
    public void copy(LabelManager src, LabelManager dest)	{
    	dest.labelFile	  = src.labelFile;
    	dest.dialogueFile = src.dialogueFile;
    	dest.introFile	  = src.introFile;
    	dest.introLines.addAll(src.introLines);
    	for (Entry<String, byte[]> entry : src.labelMap.entrySet())
    		dest.labelMap.put(entry.getKey(), entry.getValue().clone());
    	for (Entry<String, List<String>> entry : src.dialogueMap.entrySet())
    		dest.dialogueMap.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
    }

    public String label(String key) {
        byte[] value = labelMap.get(key);
        if (value == null)
        	return key;
        try {
        	String result;
        	result = new String(value, "UTF-8");
        	return replaceCrossReferences(result);
        }
        catch(UnsupportedEncodingException e) { return key; }
    }
    public String realLabel(String key) {
        byte[] value = labelMap.get(key);
        if (value == null)
        	return null;
        try {
        	String result;
        	result = new String(value, "UTF-8");
        	return replaceCrossReferences(result);
        }
        catch(UnsupportedEncodingException e) { return null; }
    }
    public String dialogue(String key) {
        List<String> value = dialogueMap.get(key);
        //if (value == null) 
        //    value = enDialogueMap.get(key);
        return (value == null) || value.isEmpty() ? key : random(value);
    }
    public void load(String dir) {
    	lastDir = dir;
    	loadLabelFile(dir);
    	loadTechsFile(dir);
    	loadDialogueFile(dir);
    }
    public void loadIntroFile(String dir) {
    	lastDir = dir;
        log("loading Intro: ", dir, introFile);
        String filename = dir+introFile;
        BufferedReader in = reader(filename);
        if (in == null) {
            err("can't find intro file! ", dir, introFile);
            return;
        }

        // intro file found... reset list of intro lines
        introLines.clear();
        int wc = 0;
        try {
            String input;
            while ((input = in.readLine()) != null) {
            	if (!isComment(input)) {
                    introLines.add(input);
                    if (Rotp.countWords)
                        wc += substrings(input, ' ').size();
                }
            }
        }
        catch (IOException e) { 
        	err("LabelManager.loadIntroFile -- IOException: " + e); 
        }
        finally {
        	try {
                        in.close();
			} catch (IOException e) {
	        	err("LabelManager.loadIntroFile2 -- IOException: " + e); 
			}
        }
        if (Rotp.countWords)
            log("WORDS - "+filename+": "+wc);
            
    }
    public void loadLabelFile(String dir) {
    	lastDir = dir;
        log("loading Labels: ", dir, labelFile);
        String filename = dir+labelFile;
        BufferedReader in = reader(filename);
        if (in == null) {
            err("can't find label file! ", dir, labelFile);
            return;
        }

        int wc = 0;
        try {
            String input;
            while ((input = in.readLine()) != null)
                wc += loadLabelLine(input);
        }
        catch (IOException e) { 
        	err("LabelManager.loadLabelFile -- IOException: ", e.toString()); 
        }
        finally {
        	try {
                        in.close();
                    } catch (IOException e) {
                        err("LabelManager.loadLabelFile2 -- IOException: " + e); 
                    }
        }
        if (Rotp.countWords)
            log("WORDS - "+filename+": "+wc);
    }
    public void resetDialogue() {
        dialogueMap.clear();
    }
    public void loadDialogueFile(String dir) {
    	lastDir = dir;
        log("loading Dialogue: ", dir, dialogueFile);
        
        String filename = dir+dialogueFile;
        BufferedReader in = reader(filename);
        if (in == null) {
            err("can't find dialogue file! ", dir, dialogueFile);
            return;
        }

        int wc = 0;
        try {
            String input;
            while ((input = in.readLine()) != null)
                wc += loadDialogueLine(input, dialogueMap);
        }
        catch (IOException e) { 
        	err("LabelManager.loadDialogueFile -- IOException: ", e.toString()); 
        }
        finally {
            try {
                    in.close();
                } catch (IOException e) {
                    err("LabelManager.loadDialogueFile2 -- IOException: " + e); 
                }
        }
        if (Rotp.countWords)
            log("WORDS - "+filename+": "+wc);
    }
    public void loadTechsFile(String dir) {
        log("loading Techs: ", dir, techsFile);
        
        String filename = dir+techsFile;
        BufferedReader in = reader(filename);
        if (in == null) {
            err("can't find techs file! ", dir, techsFile);
            return;
        }

        int wc = 0;
        try {
            String input;
            while ((input = in.readLine()) != null)
                wc += loadLabelLine(input);
        }
        catch (IOException e) { 
            err("LabelManager.loadTechsFile -- IOException: ", e.toString()); 
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                err("LabelManager.loadTechsFile2 -- IOException: " + e); 
            }
        }
        if (Rotp.countWords)
            log("WORDS - "+filename+": "+wc);
    }
    private int loadLabelLine(String input) {
    	if (isComment(input))
            return 0;

		//List<String> vals = substrings(input, '|');
		List<String> vals = Arrays.asList(input.split("\\|", 2));
        if (vals.size() < 2) {
        	if (validate) {
            	if (input.contains("|")) {
            		validateError("Orphan label keyword: " + input + " / " + labelFile);
            	}
            	else if (!input.trim().isEmpty()) {
            		validateError(labelFile + " / Orphan label text: " + input);
            	}
        	}
            return 0;
       }
        
        int wc = 0;
        String val = vals.get(1).trim();
        if (validate) {
            if (val.trim().isEmpty()) {
            	validateError("Orphan label keyword: " + input + " / " + labelFile);
            }
            else {
            	testForDialogueFont(val);
            }
        }
        try {
            labelMap.put(vals.get(0).trim(), val.getBytes("UTF-8"));
            if (Rotp.countWords)
                wc = substrings(val, ' ').size();
        }
        catch(UnsupportedEncodingException e) { }
        return wc;
    }
    public int addLabel(String key, String val) {
        int wc = 0;
        try {
            labelMap.put(key, val.getBytes("UTF-8"));
            if (Rotp.countWords)
                wc = substrings(val, ' ').size();
        }
        catch(UnsupportedEncodingException e) { }
        return wc;
    }
    private int loadDialogueLine(String input, HashMap<String,List<String>> map) {
    	if (isComment(input))
            return 0;
 
        List<String> vals = substrings(input, '|');
        if (vals.size() < 2) {
        	if (validate) {
            	if (input.contains("|")) {
            		validateError("Orphan dialogue keyword: " + input + " / " + dialogueFile);
            	}
            	else if (!input.trim().isEmpty()) {
            		validateError(dialogueFile + " / Orphan dialogue text: " + input);
            	}
        	}
            return 0;
        }
         
        String key = vals.get(0);
        if (!map.containsKey(key))
            map.put(key, new ArrayList<>());
        
        String val = vals.get(1);
        if (validate) {
            if (val.trim().isEmpty()) {
            	validateError("Orphan dialogue keyword: " + input + " / " + dialogueFile);
            }
            else {
            	testForDialogueFont(val);
            }
        }
        map.get(key).add(val);
        
        if (Rotp.countWords)
            return substrings(val, ' ').size();
        else
            return 0;
    }

    private String replaceCrossReferences(String source) {
    	// Split the requests
    	String leftId  = "→";
    	String rightId = "←";
    	String[] crArray = StringUtils.substringsBetween(source, leftId, rightId);
    	if (crArray == null || crArray.length == 0)
    		return source;

    	String result = source;
    	for (int k=0; k<crArray.length; k++) {
    		// identify the request
    		String key = crArray[k];
    		if (key == null || key.isEmpty())
    			continue;
    		// Extract the replacement string
    		String replacement = label(key);
    		// Replace
    		result = result.replace(leftId+key+rightId, replacement);
    	}
		return result;
    }
    public void replaceFirstVal(String key, String newVal) {
    	String oldList = label(key);
    	String[] valArray = oldList.split(",");
    	valArray[0] = newVal;
    	String newList = String.join(",", valArray);
        try {
            labelMap.put(key, newList.getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e) { }
    }

    public Collection<List<String>> dialogueMapValues() { return dialogueMap.values(); } // BR: For Debug
    public Set<Entry<String, List<String>>> dialogueMapEntrySet() { return dialogueMap.entrySet(); } // BR: For Debug

    private void testForDialogueFont(String str) {
    	if (FontManager.current() == null)
    		return;
    	Font dlg = FontManager.current().dlgFont(16);
//    	Font dlg = FontManager.current().plainFont(16);
    	int loc = dlg.canDisplayUpTo(str);
    	if (loc == -1)
    		return;
    	char badChar = str.charAt(loc);
    	int badCode = badChar;
    	switch(badCode) {
    	case 123:	// {
    	case 125:	// }
    	case 160:	// non-breaking space
    	case 167:	// § The section sign
    	case 183:	// ·
    	case 8203:	// Zero-Width Space
    	case 8226:	// • bullet point 
    	case 8582:	// ←
    	case 8594:	// →
    	case 9198:	// Reset ⏮
    	case 9208:	// Pause ⏸
    	case 9654:	// Play ▶
    	case 9655:	// Forward ▷
    	case 9665:	// ◁ Back
    	case 11199:	// Exit (circled bold x)
    		return;
    	case 55358:	// ߡ
    		return;
    	default:
    		System.err.println("("+ lastDir + ") dlgFont err: " + badChar + " / " + badCode + " / " + str);
    	}
    }
    private void validateError(String msg) {
    	if (LanguageManager.selectedLanguage() != 0
//    			&& !lastDir.contains("lang/en/")
    			) {
    		System.err.println("("+ lastDir + ") " + msg);
    	}
    }
}
