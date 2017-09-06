import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;



/**
 * @author Greeshma Reddy
 * Server program is responsible for doing actual file operations on it's files based 
 * on client's input
 */
public class Server {	
	
	static HashMap<String, filehandle> filenameToHandleMap= new HashMap<String, filehandle>();
	static HashMap<filehandle, FileStruct> hm= new HashMap<filehandle, FileStruct>();
	static ServerSocket serverSocket=null;
	
	public static void main (String args[]) throws ClassNotFoundException 
	{
		// Checks for valid number of arguments. Port number must be entered as a command-line argument
		if(args.length!=1)
		{
			System.out.println("Port number missing or extra arguments present..exiting the program");
			System.exit(1);
		}
		try
		{
			Socket clientSocket=null;
			int serverPort = Integer.parseInt(args[0]);
			// Creates a server socket on given port
	        serverSocket = new ServerSocket(serverPort);
	        System.out.println("Starting server on "+serverSocket.getLocalPort());

			while(true) 
			{
				// Accept a new client socket connection
				clientSocket = serverSocket.accept();
				
				// Initialize objectinputstream, objectoutputstream and datainputstream, dataoutputstream
				ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
				ObjectOutputStream oos= new ObjectOutputStream(clientSocket.getOutputStream());
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
				while(true)
				{
					int operation= (int) ois.readObject();
					/*
					 * Request for filehandle- 1, read- 2, write-3, isEOF- 4, close-5
					 */
					switch(operation)
					{
					case 1: 
					{
						String url= (String) ois.readObject();
						String filename=url.substring(url.indexOf('/')+1, url.length());
						// Checks if the file is in current directory
						if(lookup(url))
						{
							if(filenameToHandleMap.containsKey(filename))
							{
								oos.writeObject(filenameToHandleMap.get(filename));
								oos.flush();
							}
							else
							{
								// Creates a new file handle and sends it to the client
								filehandle fh= new filehandle();
								filenameToHandleMap.put(filename, fh);
								hm.put(fh, new FileStruct(filename));	
								oos.writeObject(fh);
								oos.flush();
							}
						}
						else
						{
							// If given file is not present in the directory, sends a filehandle object with a 
							// flag that says, no such file is present
							filehandle fh= new filehandle();
							fh.isValid=false;
							oos.writeObject(fh);
							oos.flush();
						}
						
						break;
						
					}
					case 2:
					{
						String filename=(String) ois.readObject();
						byte[] data= (byte[]) ois.readObject();
						if(filenameToHandleMap.containsKey(filename))
						{
							// Offset as current read positon
							int offset=hm.get(filenameToHandleMap.get(filename)).readPosition;
							dos.writeInt(data.length);
							dos.write(read(filename, offset, data));
						}
						else
						{
							dos.writeInt(-1);
						}						
						dos.flush();
						break;
					}
					case 3:
					{
						String filename=(String) ois.readObject();
						byte[] data= (byte[]) ois.readObject();
						boolean res= false;
						if(filenameToHandleMap.containsKey(filename))
						{
							// get current write postion and use it for next write
							long offset= hm.get(filenameToHandleMap.get(filename)).writePosition;
							res= write(filename, offset, data);
						}						
						oos.writeObject(res);
						oos.flush();
						break;
					}
					case 4:
					{
						String filename=(String) ois.readObject();
						boolean endOfFile= EOF(filename);
						// On EOF, reset the read position
						if(endOfFile)
						{
							hm.get(filenameToHandleMap.get(filename)).readPosition= 0;
						}
						oos.writeObject(endOfFile);
						oos.flush();
						break;
					}
					default: break;
					}

				}
			}
		} 	
		catch(IOException e) 
		{	
			System.out.println("Server :"+e.getMessage());
			System.exit(1);
		}
		}


	/**
	 * Checks the end of file
	 * @param filename
	 * @return true/false
	 */
	private static boolean EOF(String filename) {
		int readPos=hm.get(filenameToHandleMap.get(filename)).readPosition;
		File f= new File(filename);
		if(readPos>=f.length())
			return true;
		return false;
	}


	/**
	 * @param filename
	 * @param offset
	 * @param data
	 * @return
	 * @throws IOException 
	 */
	private static boolean write(String filename, long offset, byte[] data) throws IOException {
		File f= new File(filename);	
		// Writes to a file
		FileWriter fWriter= new FileWriter(f, true);
		String str= new String(data);
		fWriter.write(str);
		fWriter.close();
		long lastModTime= f.lastModified();
		// Set last modied time and next writeposition
		hm.get(filenameToHandleMap.get(filename)).lastModifiedTime= lastModTime;
		int writePosition= hm.get(filenameToHandleMap.get(filename)).writePosition;
		hm.get(filenameToHandleMap.get(filename)).writePosition= writePosition+data.length;
		return true;
	}


	/**
	 * @param filename
	 * @param offset
	 * @param data
	 * @return data; number of bytes read
	 * @throws IOException 
	 */
	private static byte[] read(String filename, int offset, byte[] data) throws IOException {
		FileInputStream fIn = new FileInputStream(new File(filename));
		fIn.getChannel().position(offset);
		// Read if the fileinputstream is available
		if(fIn.available()>=0)
		{
			fIn.read(data);	
		}
		// Set next readPosition
		int readPosition= hm.get(filenameToHandleMap.get(filename)).readPosition;
		hm.get(filenameToHandleMap.get(filename)).readPosition= readPosition+data.length;
		fIn.close();
		return data;
	}
	
	/**
	 * For finding last modified time of the fiel
	 * @param filename
	 * @return lastModifiedTime
	 */
	private static long getAttribute(String filename)
	{
		return hm.get(filenameToHandleMap.get(filename)).lastModifiedTime;
	}
	
	/**
	 * Checks if given file is in the directory.
	 * @param url
	 * @returns true if file exists, else returns false
	 */
	private static boolean lookup(String url)
	{
		File dir = new File(".");
		String filename=url.substring(url.indexOf('/')+1, url.length());
		File[] list = dir.listFiles();
        if(list!=null)
        for (File fil : list)
        {
             if (filename.equalsIgnoreCase(fil.getName()))
            {
               return true;
            }
        }
        
        return false;
	}

}
