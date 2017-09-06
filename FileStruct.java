import java.io.File;


/**
 * 
 */

/**
 * @author Greeshma Reddy
 * FileStruct holds file properties such as filename, readPosition, writePosition, lastModifiedTime
 */
public class FileStruct {
	
	String filename;
	int readPosition;
	int writePosition;
	long lastModifiedTime;
	
	public FileStruct(String filename)
	{
		this.filename=filename;
		File f= new File(filename);
		lastModifiedTime= f.lastModified();
		readPosition=0;
		writePosition=(int) f.length();
	}

}
