package ca.mint.mintchip.android.sample;

import java.io.*;

public final class FileUtility {

	public static String readTextFile(String filePath) throws IOException {

		BufferedReader reader = null;

		try {
			
			reader = new BufferedReader(new FileReader(filePath));
			StringBuilder builder = new StringBuilder();

			String line = reader.readLine();
			while (line != null) {
				
				builder.append(line);
				line = reader.readLine();
			}

			return builder.toString();
		} 
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	
	public static void writeTextFile(String filePath, String fileContent) throws IOException {

		FileWriter writer = null;

		try {

			writer = new FileWriter(filePath);
			writer.write(fileContent);
		} 
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	
	public static void createFile(String filePath) throws IOException {

		File file = new File(filePath);
		file.createNewFile();
	}
}
