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
package rotp.model.game;

import static rotp.util.ObjectCloner.deepCopy;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import rotp.util.Base;
import rotp.util.ObjectCloner;

public class DynOptions implements DynamicOptions, Serializable {
	private static final long serialVersionUID = 1L;
	private LinkedHashMap<String, Boolean>		booleanList	= new LinkedHashMap<>();
	private LinkedHashMap<String, Float>		floatList	= new LinkedHashMap<>();
	private LinkedHashMap<String, Integer>		integerList	= new LinkedHashMap<>();
    private LinkedHashMap<String, Serializable>	objectList	= new LinkedHashMap<>();
    private LinkedHashMap<String, String>		stringList	= new LinkedHashMap<>();
	private transient LinkedHashMap<String, String>	stringListBackup;

    // -------------------- Basic Getters --------------------
    //
    public LinkedHashMap<String, Boolean>	   booleanList() { return booleanList; }
    public LinkedHashMap<String, Float>		   floatList()	 { return floatList; }
    public LinkedHashMap<String, Integer>	   integerList() { return integerList; }
    public LinkedHashMap<String, Serializable> objectList()	 { return objectList; }
    public LinkedHashMap<String, String>	   stringList()	 { return stringList; }
    public int size()	{
    	return booleanList.size()
    			+ floatList.size()
    			+ integerList.size()
    			+ objectList.size()
    			+ stringList.size();
    }
    public boolean unfilled()	{ return size() == 0; }
    public boolean filled()		{ return size() > 0; }
	public LinkedHashMap<String, String> stringListBackup()	{
		if (stringListBackup == null)
			stringListBackup = new LinkedHashMap<>();
		return stringListBackup;
	}
	// #===-------------------- Overriders --------------------
    //
	@Override public Boolean setBoolean(String id, Boolean value) {
		return booleanList.put(id, value);
	}
	@Override public Boolean getBoolean(String id) {
		return booleanList.get(id);
	}
	@Override public Boolean getBoolean(String id, Boolean defaultValue) {
		return booleanList.getOrDefault(id, defaultValue);
	}
	@Override public Float setFloat(String id, Float value) {
		return floatList.put(id, value);
	}
	@Override public Float getFloat(String id) {
		return floatList.get(id);
	}
	@Override public Float getFloat(String id, Float defaultValue) {
		return floatList.getOrDefault(id, defaultValue);
	}
	@Override public Integer setInteger(String id, Integer value) {
		return integerList.put(id, value);
	}
	@Override public Integer getInteger(String id) {
		return integerList.get(id);
	}
	@Override public Integer getInteger(String id, Integer defaultValue) {
		return integerList.getOrDefault(id, defaultValue);
	}
	@Override public Serializable setObject(String id, Serializable value) {
		return objectList.put(id, (Serializable) deepCopy(value));
	}
	@Override public Serializable getObject(String id) {
		return objectList.get(id);
	}
	@Override public Serializable getObject(String id, Serializable defaultValue) {
		return objectList.getOrDefault(id, defaultValue);
	}
	@Override public String setString(String id, String value) {
		return stringList.put(id, value);
	}
	@Override public String getString(String id) {
		return stringList.get(id);
	}
	@Override public String getString(String id, String defaultValue) {
		return stringList.getOrDefault(id, defaultValue);
	}
	// -#-
	// #===-------------------- Tools --------------------
	//
	public static DynOptions copy(DynOptions options) {
		return (DynOptions) ObjectCloner.deepCopy(options);
	}
	public DynOptions copy()		{
		return (DynOptions) ObjectCloner.deepCopy(this);
	}
	public void backupStringMap()	{
		stringListBackup().clear();
		stringListBackup.putAll(stringList);
	}
	public void restoreStringMap()	{
		stringList.clear();
		stringList.putAll(stringListBackup());
	}
	// -#-
	// #===-------------------- File Management --------------------
	//
	public void save(String path, String fileName) {
		saveOptions(this, path, fileName);
	}
    // Save options to zip file
    public static void saveOptions(DynOptions options, File saveFile) {
		try {
			saveOptionsTE(options, saveFile);
		} catch (IOException ex) {
			ex.printStackTrace();
           	System.err.println("Options.save -- IOException: "+ ex.toString());
		}
    }
    // Save options to zip file
    public static void saveOptions(DynOptions options, String path, String fileName) {
		File saveFile = Base.newFile(path, fileName);
		try {
			saveOptionsTE(options, saveFile);
		} catch (IOException ex) {
			ex.printStackTrace();
           	System.err.println("Options.save -- IOException: "+ ex.toString());
		}
    }
    // Load options from file
    public static DynOptions loadOptions(File saveFile) {
    	DynOptions newOptions;
		if (saveFile.exists()) {
			newOptions = loadOptionsTE(saveFile);
            if (newOptions == null) {
            	System.err.println("Bad option version: " + saveFile.getAbsolutePath());
            	newOptions = initMissingOptionFile(saveFile);
            }
    	} else {
			System.err.println("File not found: " + saveFile.getAbsolutePath());
			newOptions = new DynOptions();
			// newOptions = initMissingOptionFile(saveFile);
		}
		return newOptions;
    }
    // Load options from file
    public static DynOptions loadOptions(String path, String fileName) {
    	DynOptions newOptions;
		File loadFile = Base.newFile(path, fileName);
		if (loadFile.exists()) {
			newOptions = loadOptionsTE(loadFile);
            if (newOptions == null) {
            	System.err.println("Bad option version: " + loadFile.getAbsolutePath());
            	newOptions = initMissingOptionFile(path, fileName);
            }
    	} else {
			System.err.println("File not found: " + loadFile.getAbsolutePath());
			newOptions = new DynOptions();
			// newOptions = initMissingOptionFile(path, fileName);
		}
		return newOptions;
    }
    private static void saveOptionsTE(DynOptions options, File saveFile) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(saveFile));
        ZipEntry e = new ZipEntry("DynamicOptions.dat");
        out.putNextEntry(e);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(bos);
            objOut.writeObject(options);
            objOut.flush();
            byte[] data = bos.toByteArray();
            out.write(data, 0, data.length);
        }
        finally {
            try {
            bos.close();
            out.close();
            }
            catch(IOException ex) {
    			ex.printStackTrace();
            	System.err.println("Options.save -- IOException: "+ ex.toString());
            }
        }
    }
    // Options files initialization
    private static DynOptions initMissingOptionFile(File saveFile) {
    	// Toolkit.getDefaultToolkit().beep();
    	DynOptions newOptions = new DynOptions();
    	saveOptions(new DynOptions(), saveFile);			
		return newOptions;    	
    }
    // Options files initialization
    private static DynOptions initMissingOptionFile(String path, String fileName) {
    	// Toolkit.getDefaultToolkit().beep();
    	DynOptions newOptions = new DynOptions();
    	saveOptions(new DynOptions(), path, fileName);			
		return newOptions;    	
    }
    private static DynOptions loadOptionsTE(File saveFile) {
    	DynOptions newOptions;
    	try (ZipFile zipFile = new ZipFile(saveFile)) {
            ZipEntry ze = zipFile.entries().nextElement();
            InputStream zis = zipFile.getInputStream(ze);
            newOptions = loadObjectData(zis);
        }
        catch(IOException e) {
        	System.err.println("Bad option version " + saveFile.getAbsolutePath());
        	newOptions = null;
        }
		return newOptions;
    }
    private static DynOptions loadObjectData(InputStream is) {
        try {
        	DynOptions newOptions;
            try (InputStream buffer = new BufferedInputStream(is)) {
                ObjectInput input = new ObjectInputStream(buffer);
                newOptions = (DynOptions) input.readObject();
            }
            return newOptions;
        }
        catch (IOException | ClassNotFoundException e) {
        	System.err.println("Error: in DynOptions loadObjectData(InputStream is)");
            return null;
        }
    }	
	// -#-
}
