/*
 * Taken from:
 * https://www.infoworld.com/article/2077578/java-tip-76--an-alternative-to-the-deep-copy-technique.html
 * 
 * JAVA TIPS
 * By Dave Miller, JavaWorld | AUG 6, 1999 12:00 AM PST
 * 
 * For serializable objects
 */
package rotp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectCloner {
	// so that nobody can accidentally create an ObjectCloner object
	private ObjectCloner(){}

	static public Object deepCopy(Serializable oldObj) {
		try {
			return deepCopyTE(oldObj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	static public Object deepCopyTE(Object oldObj) throws Exception {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos =  new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			// serialize and pass the object
			oos.writeObject(oldObj);
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);
			// return the new object
			return ois.readObject();
		}
		catch(Exception e) {
			System.out.println("Exception in ObjectCloner = " + e);
			throw(e);
		}
		finally {
			try { if (oos != null) oos.close(); } catch(Exception e) {}
			try { if (ois != null) ois.close(); } catch(Exception e) {}
		}
	}
}
