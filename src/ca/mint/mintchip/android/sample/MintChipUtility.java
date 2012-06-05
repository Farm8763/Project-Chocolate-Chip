package ca.mint.mintchip.android.sample;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import ca.mint.mintchip.android.api.MessageFactory;
import ca.mint.mintchip.android.api.MintChipFactory;
import ca.mint.mintchip.contract.*;

/**
 * 
 * This class includes most of the code that directly interacts with the MintChip API to 
 * demonstrate how the API can be used on the Android platform.
 *
 */
public final class MintChipUtility {

	private static final int LOG_RECORDS_TO_SHOW = 20; // To display up to 20 records
	private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("MMM d yyyy, HH:mm aaa");

	private static IMintChip sMintChip = null;

	
	/**
	 * Demonstrates how to create an instance of the MintChip.
	 * 
	 * @return an instance of the class that implements the IMintChip interface.
	 * @throws MintChipException if the MintChip cannot be accessed.
	 */
	private synchronized static IMintChip getMintChip() throws MintChipException {
		
		if (sMintChip == null){
			sMintChip = MintChipFactory.getInstance().createMintChip();
		}
		
		return sMintChip;
	}
	
	
	/**
	 * Demonstrates how to check whether a string is a valid MintChip Id.
	 * 
	 * @param mintChipId the MintChip account Id to be validated 
	 * @return true if the supplied id is a valid MintChip account id; false otherwise
	 * @throws MintChipException if the MintChip cannot be accessed.
	 */
	public static boolean isValidMintChipID(String mintChipId) throws MintChipException {
		
		return getMintChip().isValidId(mintChipId);
	}

	
	/**
	 * Demonstrates how to read the ID of the Asset Store associated with MintChip.
	 * 
	 * @return formatted MintChip Id
	 * @throws MintChipException if the MintChip cannot be accessed.
	 */
	public static String getFormattedMintChipID() throws MintChipException {
		
		String id = getMintChip().getId();
		
		return formatId(id);
	}
	

	/**
	 * Demonstrates how to read the MintChip balance.
	 *  
	 * @return a currency formatted string representing the MintChip balance
	 * @throws MintChipException if the MintChip cannot be accessed.
	 */
	public static String getFormattedBalance() throws MintChipException  {
		
		IMintChipStatus status = getMintChip().getStatus();
		
		return formatCurrency(status.getBalance());
	}

	
	/**
	 * Demonstrates how to read the MintChip information.
	 *  
	 * @return a string that contains formatted information about the MintChip. It includes the 
	 * properties of the MintChip as well as its current status.
	 * 
	 * @throws MintChipException if the MintChip cannot be accessed.
	 */
	public static String getFormattedChipInfo() throws MintChipException {
		
		IMintChipStatus status = getMintChip().getStatus();

		StringBuffer text = new StringBuffer();
		
		text.append("MintChip Id: ").append(formatId(getMintChip().getId()))
			.append("\nCurrency: ").append(getMintChip().getCurrencyCode())
			.append("\nApplet Version: ").append(getMintChip().getVersion())			
			.append("\nBalance: ").append(formatCurrency(status.getBalance()))
			.append("\nCurrent Credit Count: ").append(status.getCreditLogCount())
			.append("\nCurrent Debit Count: ").append(status.getDebitLogCount())
			.append("\nRemaining Credit Count: ").append(status.getCreditLogCountRemaining())
			.append("\nRemaining Debit Count: ").append(status.getDebitLogCountRemaining())
			.append("\nMax Credit Allowed: ").append(formatCurrency(status.getMaxCreditAllowed()))
			.append("\nMax Debit Allowed: ").append(formatCurrency(status.getMaxDebitAllowed()));
		
		return text.toString();
	}
	
	
	/**
	 * Demonstrates how to read the MintChip transaction log.
	 *  
	 * @param logType the type of entries to return(CREDIT or DEBIT)
	 * @return ILogEntry[] array containing the predefined number of latest transaction records. 
	 * @throws MintChipException if the MintChip is missing/faulty or if communication errors 
	 * occurred with the MintChip
	 */
	public static ILogEntry[] readTransactionLog(LogType logType) throws MintChipException {
		
		IMintChipStatus status = getMintChip().getStatus();
		
		// Calculate the start index and the number of entries to read
		int totalCount = 0;
		if (logType == LogType.CREDIT){
			totalCount = status.getCreditLogCount();
		}
		else {
			totalCount = status.getDebitLogCount();
		}
		
		int startIndex = totalCount - LOG_RECORDS_TO_SHOW;
		int numOfEntries = LOG_RECORDS_TO_SHOW;		
		if (startIndex < 0) {
			numOfEntries = LOG_RECORDS_TO_SHOW + startIndex;
			startIndex = 0;
		}
		
		if (numOfEntries < 1) {
			numOfEntries = 1;
		}
		
		// Read the MintChip transaction log
		ILogEntry[] entries = getMintChip().readTransactionLog(logType, startIndex, numOfEntries);
		
		// List entries in the reverse order
		if (entries != null && entries.length > 0) {
			
			List<ILogEntry> list = Arrays.asList(entries);
			Collections.reverse(list);
			list.toArray(entries);
		}
		
		return entries;
	}
	
	
	/**
	 * Demonstrates how to create the value message and write it to a file.
	 * 
	 * @param fileName name of the file to write the value message to
	 * @param amount the value message amount in cents
	 * @param payee the receiver's MintChip Id
	 * @param annotation an optional text description for the value message 
	 * @return the created value message that was saved to the specified file
	 * @throws IOException if errors occurred while writing to the file
	 * @throws MintChipException if the MintChip is missing/faulty or if communication errors occurred 
	 * with the MintChip, if the requested value message exceeds one of the chips operational parameters
	 */
	public static IValueMessage createValueMessageFile(
									String fileName, 
									int amount, 
									String payee, 
									String annotation) 
									throws IOException, MintChipException {

		// Create a new file first to ensure the selected file can be accessed for writing
		FileUtility.createFile(fileName);
		
		// Create the Value Request
		IValueRequestMessage request = MessageFactory.getInstance().createValueRequestMessage(
				payee, amount, getMintChip().getCurrencyCode());
		
		request.setAnnotation(annotation); // this is optional
		request.setChallenge(new Date().hashCode()); // this is optional
		
		// Create the value message
		IValueMessage valueMessage = getMintChip().createValueMessage(request);
		
		// Write the value message to the file
		String message = valueMessage.toBase64String();
		FileUtility.writeTextFile(fileName, message);
		
		return valueMessage;
	}
	
	
	/**
	 * Demonstrates how to load the value message onto the MintChip.
	 *  
	 * @param valueMessage the value message to load
	 * @return a currency formatted string representing the amount loaded onto the MintChip
	 * @throws MintChipException if MintChip is missing/faulty or if communication errors 
	 * occurred with MintChip, if the value has already been loaded, if the value message 
	 * exceeds one of the chips operational parameters, if the value message has been tampered with
	 */
	public static String loadValueMessage(IValueMessage valueMessage) throws MintChipException {	

		getMintChip().loadValueMessage(valueMessage);
		
		return formatCurrency(valueMessage.getAmount());
	}
	

	/**
	 * Demonstrates how to read the Value Message from a file.
	 * 
	 * @param filePath name of the file to read the value message from
	 * @return the value message read from the specified file
	 * @throws IOException if errors occurred while reading the message from the file
	 */
	public static IValueMessage readValueMessageFromFile(String filePath) throws IOException {
		
		String fileContent = FileUtility.readTextFile(filePath);
		
		IMessage message = MessageFactory.getInstance().toMessage(fileContent);
		
		if (message instanceof IValueMessage) {
			
			return (IValueMessage) message;
		}
		else {
			throw new IOException("File contains a message that is not of the IValueMessage type.");
		}
	}
	
	
	/**
	 * Demonstrates how to download a value message from a Web site.
	 * 
	 * @param url URL to load the value message from
	 * @return the value message loaded from the Web site
	 * @throws ClientProtocolException if errors occurred while communicating with the Web site
	 * @throws IOException if the downloaded content cannot be parsed into a valid value message
	 */
	public static IValueMessage downloadValueMessageFromWeb(String url) throws ClientProtocolException, IOException {
		
		String base64Message = WebUtility.downloadContent(url);
		
		IMessage message = MessageFactory.getInstance().toMessage(base64Message);
		
		if (message instanceof IValueMessage) {
			
			return (IValueMessage) message;
		}
		else {
			throw new IOException("Received message is not of the IValueMessage type.");
		}
	}	

	
	/**
	 * Demonstrates how to download a value request message from a Web site.
	 * 
	 * @param url URL to load the value request message from
	 * @return the value request message loaded from the Web site
	 * @throws ClientProtocolException if errors occurred while communicating with the Web site
	 * @throws IOException if the downloaded content cannot be parsed into a valid value request message
	 */
	public static IValueRequestMessage downloadValueRequestMessageFromWeb(String url) throws ClientProtocolException, IOException {
	
		String base64Message = WebUtility.downloadContent(url);
	
		IMessage message = MessageFactory.getInstance().toMessage(base64Message);
		
		if (message instanceof IValueRequestMessage) {
			
			return (IValueRequestMessage) message;
		}
		else {
			throw new IOException("Received message is not of the IValueRequestMessage type.");
		}
	}
	
	
	/**
	 * Demonstrates how to post a value message to a Web site.
	 * 
	 * @param request the value request message that specifies what value message to create and where to post it
	 * @param context a parent context (an Activity) that makes this call
	 * @return the URL where to navigate next after the value message was posted
	 * @throws MintChipException is errors occurred while creating the value message
	 * @throws IOException if errors occurred while posting the value message to the Web site
	 */
	public static String postValueMessageToWeb(IValueRequestMessage request) throws MintChipException, IOException  {
		
		IValueMessage valueMessage = getMintChip().createValueMessage(request);
		
		String message = valueMessage.toBase64String();
		String newUrl = WebUtility.postContent(request.getResponseAddress(), message);
		
		return newUrl;
	}
	
	
	/**
	 * 
	 * Demonstrates how to get the last value message created by this MintChip.
	 * 
	 * @return the last value message created by this MintChip.
	 * @throws MintChipException if the MintChip is missing/faulty or if communication 
	 * errors occurred with MintChip
	 */
	public static IValueMessage getLastCreatedValueMessage() throws MintChipException {
		
		return getMintChip().getLastCreatedValueMessage("This is a duplicate.");
	}

	
	/**
	 * Demonstrates how to read the properties of the Value Message.
	 * 
	 * @param valueMessage the value message to inspect
	 * @return a string that contains formatted properties of the provided value message
	 */
	public static String formatValueMessage(IValueMessage valueMessage) {
		
		StringBuffer text = new StringBuffer();
		
		text.append("Payer: ").append(formatId(valueMessage.getPayerId()))
			.append("\nPayee: ").append(formatId(valueMessage.getPayeeId()))
			.append("\nAmount: ").append(formatCurrency(valueMessage.getAmount()))
			.append("\nCurrency: ").append(valueMessage.getCurrencyCode())
			.append("\nTime: ").append(formatDateTime(valueMessage.getCreatedTime()))
			.append("\nAnnotation: ").append(valueMessage.getAnnotation())
			.append("\nChallenge: ").append(valueMessage.getChallenge())
			.append("\nMintChip Version: ").append(valueMessage.getMintChipVersion());
		
		return text.toString();
	}
	
	
	/**
	 * Formats a MintChip ID as ####-####-####-####
	 * 
	 * @param id the MintChip Id to format
	 * @return the formatted MintChip Id
	 */
	public static String formatId(String id){
		
		return String.format("%s-%s-%s-%s", 
					id.substring(0, 4),
					id.substring(4, 8),
					id.substring(8, 12),
					id.substring(12, 16));	
	}
	
	
	/**
	 * Formats an amount in cents as a string representing the dollar value.
	 * 
	 * @param cents the amount to format
	 * @return a formatted string representing the cents as a dollar value
	 */
	public static String formatCurrency(int cents) {
		
		double amount = (double)cents / 100;
		return String.format("$%.2f", amount);
	}
	

	/**
	 * Formats the Calendar object to a string.
	 * 
	 * @param dateTime the Calendar object to format
	 * @return formatted string representing the date and time
	 */
	public static String formatDateTime(Calendar dateTime) {
		
		return sDateFormat.format(dateTime.getTime());
	}
}
