import java.util.*;

/**
 * 
 * @author Greeshma Reddy
 * This is a java file to test the fileSystem methods- read, write, isEOF, close, open.
 * Accepts Filename as input. Subsequent read, write operations are shown as a choice to the user.
 * 
 */
public class testCl{

    public static void main(String [] args) throws java.lang.InterruptedException, java.io.IOException, ClassNotFoundException
    {
		String Filename=args[0]; 
//    	String Filename="pyrite.cs.iastate.edu:10608/data.txt";

    	fileSystemAPI fs = new fileSystem(Filename); 
    	filehandle fh;
    	long startTime, endTime, turnAround;

    	// Fetches filehandle for given file. If null is returned, exits the program.
    	fh=fs.open(Filename);	
		if(fh==null)
		{
			System.out.println("No such file on the server or invalid filename");	    	
			System.exit(1);
		}
		
    	while (true){
    		
    		System.out.println("Enter your choice.\n2. Read\n3. Write\n4. Exit");
    		Scanner sc= new Scanner(System.in);
        	fh=fs.open(Filename);	

    		switch(sc.nextInt())
    		{
    			case 2: 
    			{
    				fh= fs.open(Filename);
    				startTime=Calendar.getInstance().getTime().getTime();
    				// Reads the file, 1024 bytes at a time
    				while (!fs.isEOF(fh)){
    					byte[] data= new byte[1024];
    					fs.read(fh, data);
    				}
    				endTime=Calendar.getInstance().getTime().getTime();
    				turnAround=endTime-startTime;
    				System.out.println();
    				System.out.println("This round took "+turnAround+" ms.");
    				break;
    			}
    			case 3:
    			{
    				System.out.println("Enter the text you want to write");
    				sc.nextLine();
    				String str= sc.nextLine();
    				str=str+"\n";
    				// Writes the user's input to file on server
    				fs.write(fh, str.getBytes("UTF-8"));
    				// Closes file after writing
    				fs.close(fh);
    				break;
    			}
    			case 4:
    			{
    				fs.close(fh);
    				System.out.println("Exiting Program..");	    	
    				System.exit(1);
    			}
    			default: break;
	    
    		}
    		Thread.sleep(1000);
    	}
    }

}