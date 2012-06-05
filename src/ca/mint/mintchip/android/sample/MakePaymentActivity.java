package ca.mint.mintchip.android.sample;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;

import ca.mint.mintchip.android.api.MessageFactory;
import ca.mint.mintchip.contract.IMessage;
import ca.mint.mintchip.contract.IValueMessage;
import ca.mint.mintchip.contract.IValueRequestMessage;
import ca.mint.mintchip.contract.MintChipException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MakePaymentActivity extends BaseActivity 
		implements View.OnClickListener, DialogInterface.OnDismissListener {

	private static final String FILE_FOLDER = "/sdcard/";
	private static final String KEY_VALUE_REQUEST_MESSAGE = "ValueRequestMessage";
	private static final String SCHEME_HTTP = "http";
    private static final int FILE_PROMPT_DIALOG  = 0;

	private Button mMakePaymentButton;
	private EditText mAmountEditText;
	private EditText mPayeeIdEditText;
	private EditText mAnotationEditText;
	
	private IValueRequestMessage mValueRequestMessage;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
    	this.setContentView(R.layout.makepayment);
    	
    	this.initBanner(getString(R.string.make_payment_title));
    	
    	this.mMakePaymentButton = (Button) this.findViewById(R.id.makePaymentButton);
        this.mMakePaymentButton.setOnClickListener(this);
        
        this.mAmountEditText = (EditText) this.findViewById(R.id.amountEditText);
        this.mPayeeIdEditText = (EditText) this.findViewById(R.id.payeeIdEditText);
        this.mAnotationEditText = (EditText) this.findViewById(R.id.annotationEditText);
        
        new StartupDataTask().execute(savedInstanceState);        
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		
		try {
			
			if (this.mValueRequestMessage != null) {
				outState.putString(KEY_VALUE_REQUEST_MESSAGE, this.mValueRequestMessage.toBase64String());
			}
		} 
		catch (IOException e) {
			// Ignore this exception.
		}
	}

	
	@Override
	public void onClick(View view) {
		
		if (view == this.mMakePaymentButton) {

			if (this.isInputValid()) {
				
				if(this.isWebRequest()) {
					this.makeWebPayment();
				}
				else {
					this.showDialog(FILE_PROMPT_DIALOG);
				}
			}		
		}		
	}

	
	@Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case FILE_PROMPT_DIALOG:
            	return new PromptDialog(
            			this, 
            			getString(R.string.save_payment_to_file), 
            			getString(R.string.file_name), 
            			generateFileName(), 
            			this, 
            			null);
        }

        return null;
    }

    
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        
		switch (id)
        {
            case FILE_PROMPT_DIALOG:
    			PromptDialog filePrompt = (PromptDialog) dialog;
    			filePrompt.setInputValue(generateFileName());
    			break;
        }
	}


	@Override
	public void onDismiss(DialogInterface dialog) {
		
		if (dialog instanceof PromptDialog) {
			
			PromptDialog filePrompt = (PromptDialog) dialog;
			String selectedFile = filePrompt.getInputValue();
			
			if (selectedFile != null && selectedFile.length() > 0) {
				this.saveValueMessageToFile(selectedFile);
			}
		}
	}

	
	private class StartupDataTask extends AsyncTask<Bundle, Void, Void> {

    	Exception mException;
    	
		@Override
		protected Void doInBackground(Bundle... params) {
    		try {
    			MakePaymentActivity.this.retrieveStartupData(params[0]);
			} 
			catch (Exception ex) {
				mException = ex;
			}
    		
			return null;
		}

    	@Override
		protected void onPostExecute(Void param) {
    		if (mException != null) {
    			MakePaymentActivity.this.reportError(mException);
    		}
    		else {
    			MakePaymentActivity.this.populateControls();
    		}    	
    	}
	}
	
	private void retrieveStartupData(Bundle savedInstanceState) throws ClientProtocolException, IOException {
		
		if (savedInstanceState != null) {
			
			String base64Message = savedInstanceState.getString(KEY_VALUE_REQUEST_MESSAGE);
			
			if (base64Message != null) {
				
				IMessage message = MessageFactory.getInstance().toMessage(base64Message);
				
				if (message instanceof IValueRequestMessage) {
					this.mValueRequestMessage = (IValueRequestMessage) message;
				}
				else {
					throw new IOException("Received message is not of the IValueRequestMessage type.");				
				}				
			}
		}
    	
		if (this.mValueRequestMessage == null) {
			
			Intent intent = this.getIntent();
	    	if (intent != null) {
	    		
	            Uri data = intent.getData();
	            if (data != null)
	            {
	            	if (SCHEME_HTTP.equalsIgnoreCase(data.getScheme())) {
	            		
	            		this.mValueRequestMessage = MintChipUtility.downloadValueRequestMessageFromWeb(data.toString());
	            	}
	            }
	    	}
		}
	}


	private void populateControls() {
		
		if (this.mValueRequestMessage == null) {
			
			this.mAmountEditText.setEnabled(true);
			this.mPayeeIdEditText.setEnabled(true);
			this.mAnotationEditText.setEnabled(true);
		}
		else {
				
			String amount = new DecimalFormat("#0.00").format((double)this.mValueRequestMessage.getAmount()/100);
			
			this.mAmountEditText.setText(amount);
			this.mPayeeIdEditText.setText(this.mValueRequestMessage.getPayeeId());
			this.mAnotationEditText.setText(this.mValueRequestMessage.getAnnotation());
			
			this.mAmountEditText.setFocusable(false);
			this.mPayeeIdEditText.setFocusable(false);
			this.mAnotationEditText.setFocusable(false);
			
			this.mMakePaymentButton.requestFocus();			
		}
	}


    private void makeWebPayment() {
			
    	new WebPaymentTask().execute(this.mValueRequestMessage);			
	}
    
    private class WebPaymentTask extends AsyncTask<IValueRequestMessage, Void, String> {     
    	
    	ProgressDialog mDialog = new ProgressDialog(MakePaymentActivity.this);
    	Exception mException;
    	
    	@Override
    	protected String doInBackground(IValueRequestMessage... params) {         
    		try {
				return MintChipUtility.postValueMessageToWeb(params[0]);
			} 
			catch (Exception ex) {
				mException = ex;
				return null;
			}     
    	}     
    	    	
    	@Override
		protected void onPreExecute() {
    		
    		mException = null;
    		
    		mDialog.setMessage("Processing..."); 
    		mDialog.setIndeterminate(true); 
    		mDialog.setCancelable(false); 
    		mDialog.show(); 
		}

    	@Override
		protected void onPostExecute(String nextUrl) {         
		
    		mDialog.hide();
    		
    		if (mException != null) {
    			MakePaymentActivity.this.reportError(mException);
    		}
    		else {
	    		if (nextUrl != null) {
	    			MakePaymentActivity.this.launchWebBrowser(nextUrl);
				}
				else {
					DialogUtility.displayMessage(MakePaymentActivity.this, "", String.format(getString(R.string.value_message_posted), 
							MakePaymentActivity.this.mValueRequestMessage.getResponseAddress()));
				}
    		}
    	}
    }
    
    
	private void saveValueMessageToFile(String fileName) {
		
		try {
			
			IValueMessage valueMessage = MintChipUtility.createValueMessageFile(
					fileName, 
					this.getAmountCents(), 
					this.mPayeeIdEditText.getText().toString(), 
					this.mAnotationEditText.getText().toString());

			this.updateBalance();

			DialogUtility.displayMessage(this, "", getString(R.string.value_message_saved) + 
					fileName + ":\n\n" + MintChipUtility.formatValueMessage(valueMessage));
		} 
		catch (Exception ex) {
			this.reportError(ex);
		}
	}

	
	private boolean isInputValid() {

		try {
			
			if (this.mAmountEditText.getText().length() == 0 || getAmountCents() == 0){
				
				DialogUtility.displayMessage(this, "", getString(R.string.specify_amount));
				this.mAmountEditText.requestFocus();
				return false;
			}
			
			if (this.mPayeeIdEditText.getText().length() == 0 
				|| !MintChipUtility.isValidMintChipID(this.mPayeeIdEditText.getText().toString())){
				
				DialogUtility.displayMessage(this, "", getString(R.string.specify_payee_id));
				this.mPayeeIdEditText.requestFocus();
				return false;
			}
			
			return true;
		}
		catch (MintChipException e) {
			this.reportError(e);
			return false;
		}
	}
	
	
	private boolean isWebRequest() {
		
		if (this.mValueRequestMessage == null) {
			return false;
		}
		
		if (this.mValueRequestMessage.getResponseAddress() == null) {
			return false;
		}
		
		Uri url = Uri.parse(this.mValueRequestMessage.getResponseAddress());
		
		return SCHEME_HTTP.equalsIgnoreCase(url.getScheme());
	}
	
	
	private void launchWebBrowser(String url) {
    	
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setData(Uri.parse(url)); 
	    
	    this.startActivity(intent); 
	}
    
	
	private int getAmountCents() {
		
		double amount = Double.parseDouble(this.mAmountEditText.getText().toString());
		int cents = (int)(amount * 100);
		return cents;
	}

	
	private String generateFileName(){
		
		Date now = Calendar.getInstance().getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");	 
		String fileName = formatter.format(now) + ".ecn";
		
		return FILE_FOLDER + fileName;
	}
}
