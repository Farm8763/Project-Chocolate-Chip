package mypackage;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.global.Formatter;

import ca.mint.mintchip.blackberry.sample.MintChipUtility;
import ca.mint.mintchip.blackberry.sample.components.FileUtility;
import ca.mint.mintchip.contract.*;
import net.rim.device.api.i18n.SimpleDateFormat;

public class MakeFile {

	private String _amount;
	private String _payee;
	private String _annotation;
	private IValueMessage _valueMessage;
	private String path;
	private static final String FILE_FOLDER = "SDCard/";
	
	public MakeFile (String payee) {
	
		_payee = payee;
		initControls();
    }
	
	public String getPath() {
		return path;
	}

	private void saveToFile() {
		
		try {
			if (!isInputValid()){
				return;
			}
			
			String fileName = getFileForValueMessage();
			if (fileName == null){
				return;
			}
			
			set_valueMessage(MintChipUtility.createValueMessageFile(
					fileName, getAmountCents(), _payee, _annotation));
		} 
		catch (Exception ex) {
		}
	}
	
	private String getFileForValueMessage() {
		String fileName = generateFileName();
		
		try {
			while (FileUtility.fileExists(fileName)){
				fileName += "1";
			}
			
			// This will create a new file or truncate the existing one
			// ensuring the selected file can be accessed for writing
			FileUtility.createFile(fileName);
		} 
		catch (Exception ex) {
			return null;
		}
		
		return fileName;
	}

	private boolean isInputValid() throws MintChipException {

		if (_amount.length() == 0 || getAmountCents() == 0){
			return false;
		}
		
		if (_payee.length() == 0 
			|| !MintChipUtility.isValidMintChipID(_payee)){
			return false;
		}
		
		return true;
	}
	
	private int getAmountCents() {
		
		double amount = Double.parseDouble(_amount);
		int cents = (int)(amount * 100);
		return cents;
	}

	private String generateFileName(){
		
		Date now = Calendar.getInstance().getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");	 
		String fileName = formatter.format(now) + ".ecn";
		path = fileName;
		return FILE_FOLDER + fileName;
	}
	
	private void initControls() {
	try{
		
//		_amount = new Formatter("en").formatNumber(((double)MintChipUtility.getMintChip().getStatus().getBalance()) / 100, 2);
		_amount = new Formatter("en").formatNumber(((double)0.01));
        saveToFile();
		_annotation = "Uploaded from a BlackBerry";
        }   
	catch (Exception e){}
	}

	public IValueMessage get_valueMessage() {
		return _valueMessage;
	}

	public void set_valueMessage(IValueMessage _valueMessage) {
		this._valueMessage = _valueMessage;
	}
}
