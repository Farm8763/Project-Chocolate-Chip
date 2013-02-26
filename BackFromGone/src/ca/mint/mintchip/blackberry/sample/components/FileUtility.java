package ca.mint.mintchip.blackberry.sample.components;

import java.io.*;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;

public final class FileUtility {

	public static String readTextFile(String fileName) throws IOException {
		
		String fileContent = null; 
		FileConnection file = null; 
		DataInputStream dataStream = null; 
		
		try { 
			file = (FileConnection) Connector.open(makeFilePath(fileName), Connector.READ); 
			dataStream = file.openDataInputStream(); 
			byte[] data = IOUtilities.streamToBytes(dataStream); 
			fileContent = new String(data); 
		} 
		finally { 
		    if (dataStream != null){ 
		    	dataStream.close(); 
		    }
		    
		    if (file != null ){ 
		    	file.close(); 
		    }
		} 
		
		return fileContent;
	}

	public static void writeTextFile(String fileName, String fileContent) throws IOException {
		writeTextFile(fileName, fileContent.getBytes());
	}

	public static void writeTextFile(String fileName, byte[] fileContent) throws IOException {
		
		FileConnection file = null; 
		DataOutputStream dataStream = null; 
		
		try { 
			file = (FileConnection) Connector.open(makeFilePath(fileName), Connector.READ_WRITE); 
			if (!file.exists()){
				file.create();
			}
			else {
				file.truncate(0);
			}
			
			dataStream = file.openDataOutputStream();
			dataStream.write(fileContent);
		} 
		finally { 
		    if (dataStream != null){ 
		    	dataStream.close(); 
		    }
		    
		    if (file != null ){ 
		    	file.close(); 
		    }
		} 
	}

	public static boolean fileExists(String fileName) throws IOException {
		
		FileConnection file = null; 
		
		try { 
			file = (FileConnection) Connector.open(makeFilePath(fileName), Connector.READ); 
			return file.exists();
		} 
		finally { 
		    if (file != null ){ 
		    	file.close(); 
		    }
		} 
	}

	public static void createFile(String fileName) throws IOException {
		
		FileConnection file = null; 
		
		try { 
			file = (FileConnection) Connector.open(makeFilePath(fileName), Connector.READ_WRITE); 
			if (!file.exists()){
				file.create();
			}
			else {
				file.truncate(0);
			}
		} 
		finally { 
		    if (file != null ){ 
		    	file.close(); 
		    }
		} 
	}
	
	private static String makeFilePath(String fileName) {
		
		if (fileName == null || fileName.length() == 0){
			throw new IllegalArgumentException("fileName parameter is missing.");
		}
		
		if (!fileName.startsWith("file:///")) {
			return "file:///" + fileName;
		}
		else {
			return fileName;
		}
	}
}
