import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 *  
 * @author Greeshma Reddy
 * This is an implementation of fileSystemAPI- includes operations read, write, open, close, isEOF. 
 */
public class fileSystem implements fileSystemAPI
{ 
	static Socket clientSocket = null;

	//handleToFilenameMap holds a mapping of filehandle and filename
    HashMap<filehandle, String> handleToFilenameMap;
	ObjectInputStream ois=null;
	ObjectOutputStream oos=null;
	DataInputStream dis = null;
	DataOutputStream dos = null;


	/**
	 * fileSystem's constructor
	 * @param filename
	 * @throws IOException
	 */
	public fileSystem(String filename) throws IOException
	{
		// Extract hostname and port name from the filename
		String hostName=filename.substring(0, filename.indexOf(':'));
		int port= Integer.parseInt(filename.substring(filename.indexOf(':')+1, filename.indexOf('/')));
				
		// handleToFilenameMap keepa track of filehandles and corresponding filenames
		handleToFilenameMap= new HashMap<filehandle, String>();
		try {
			// Connects to server socket
	    	clientSocket = new Socket(hostName, port);
	    	System.out.println("Client started on "+ clientSocket.getLocalPort());
	    	
	    	// Initialize Object input stream and output stream
	    	oos= new ObjectOutputStream(clientSocket.getOutputStream());
	    	ois= new ObjectInputStream(clientSocket.getInputStream());
    	
		} catch (Exception e) {
			System.out.println("Error occured"+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Accepts url as input and returns file handle of given url from the server
	 */
    public filehandle open(String url) throws IOException
    {
    	filehandle fh=null;
    	String filename=url.substring(url.indexOf('/')+1);
    	// If filename contains extra '/' , null is returned
    	if(filename.contains("/")) 
    	{
    		return null;
    	}
		try {
			// open-1
			oos.writeObject(1);
	    	oos.writeObject(url);
	    	fh= (filehandle) ois.readObject();
	    	if(fh.isValid)
	    	{
	    		handleToFilenameMap.put(fh,  filename);
	    	}
	    	else
	    	{
	    		fh=null;
	    	}	
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return fh;
    }
	
    /**
     * Sends the byte array data to be written to server. Accepts filehandle fh, byte array data as
     * input. Returns true/false
     */

    public boolean write(filehandle fh, byte[] data) throws java.io.IOException, ClassNotFoundException
    {
    	if(!handleToFilenameMap.keySet().contains(fh))
    	{
    		System.out.println("Not a valid filehandle");
    		return false;
    	}
    	// write- 3
    	oos.writeObject(3);
    	oos.writeObject(handleToFilenameMap.get(fh));
    	oos.writeObject(data);
    	oos.flush();
    	close(fh);
    	// retruns true on successful write operation at server
    	if((boolean)ois.readObject())
    	{
    		return true;
    	}
    	return false;
    }


    /**
     * Sends the byte array data to be read into from the file on server. Accepts
     * filehandle fh, byte array data as input and returns number of bytes read
     */
    public int read(filehandle fh, byte[] data) throws java.io.IOException, ClassNotFoundException
    {
    	/*if(!handleToFilenameMap.keySet().contains(fh))
    	{
    		System.out.println("Not a valid filehandle");
    		return -1;
    	}*/
    	dos = new DataOutputStream(clientSocket.getOutputStream());   
    	
    	//read- 2

    	oos.writeObject(2);
    	oos.writeObject(handleToFilenameMap.get(fh));
    	oos.writeObject(data);
    	dis = new DataInputStream(clientSocket.getInputStream());
    	int len= dis.readInt();
    	if(len==-1) return -1;
    	else
    	{
    		dis.read(data, 0, len);
        	System.out.print(new String(data).trim());
    	}
    	return data.length;
    }

    /**
     * Accepts filehandle fh as input. Closes the file handle, removes the file handle from handleToFilenameMap
     */
    public boolean close(filehandle fh) throws java.io.IOException 
    {
    	// close- 5
    	handleToFilenameMap.remove(fh);
    	fh.discard();
    	return true;
    }

    /**
     *Checks if it is the end-of-file. Returns true/false
     */
    public boolean isEOF(filehandle fh) throws java.io.IOException, ClassNotFoundException
    {
    	// isEOF- 4
    	oos.writeObject(4);
    	oos.writeObject(handleToFilenameMap.get(fh));
    	oos.flush();
    	return (boolean)ois.readObject();   	
    }
    
} 
    
	
