package ca.mint.mintchip.blackberry.sample;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.microedition.global.Formatter;

import net.rim.device.api.i18n.SimpleDateFormat;

import ca.mint.mintchip.blackberry.api.MessageFactory;
import ca.mint.mintchip.blackberry.api.MintChipFactory;
import ca.mint.mintchip.blackberry.sample.components.FileUtility;
import ca.mint.mintchip.contract.*;

/**
 * 
 * This class includes most of the code that directly interacts with the MintChip API to 
 * demonstrate how the API can be used on the BlackBerry platform.
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
	public synchronized static IMintChip getMintChip() throws MintChipException {
		
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
			reverseArray(entries);
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
		request.setResponseAddress("http://www.somemerchant.com/mintchip"); // this is optional
		
		// The request object can be used right away, though we would like to demonstrate how
		// it can be serialized / deserialized in cases when it has to be sent over a network.
		String base64Request = request.toBase64String();
		IValueRequestMessage receivedRequest = 
				(IValueRequestMessage) MessageFactory.getInstance().toMessage(base64Request);
		
		// Create the value message
		IValueMessage valueMessage = getMintChip().createValueMessage(receivedRequest);
		
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
		
		IValueMessage valueMessage = 
				(IValueMessage) MessageFactory.getInstance().toMessage(fileContent);
		
		return valueMessage;
	}
	
	
	/**
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
		
		return Formatter.formatMessage("{0}-{1}-{2}-{3}", 
				new String[] { 
					id.substring(0, 4),
					id.substring(4, 8),
					id.substring(8, 12),
					id.substring(12, 16),
				});	
	}
	
	
	/**
	 * Formats an amount in cents as a string representing the dollar value.
	 * 
	 * @param cents the amount to format
	 * @return a formatted string representing the cents as a dollar value
	 */
	public static String formatCurrency(int cents) {
		
		double amount = (double)cents / 100;
		return "$" + new Formatter("en").formatNumber(amount, 2);
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

	
	private static void reverseArray(ILogEntry[] entries) {
		
		for (int i = 0; i < entries.length / 2; i++) {
			ILogEntry temp = entries[i];
			entries[i] = entries[entries.length - i - 1];
			entries[entries.length - i - 1] = temp;
		}
	}
}

