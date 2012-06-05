package ca.mint.mintchip.android.sample;

import java.io.IOException;

import ca.mint.mintchip.android.api.MessageFactory;
import ca.mint.mintchip.contract.IMessage;
import ca.mint.mintchip.contract.IValueMessage;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AcceptPaymentActivity extends BaseActivity 
		implements View.OnClickListener, DialogInterface.OnDismissListener {

	private static final String START_DIRECTORY = "/";
	private static final String KEY_VALUE_MESSAGE  = "ValueMessage";
	private static final String SCHEME_HTTP = "http";
	private static final String SCHEME_FILE = "file";
    private static final int DIALOG_FILE_BROWSER  = 0;

	private IValueMessage mValueMessage;
	private String mValueMessageFilePath;
	
    private Button mBrowseButton;
	private Button mAcceptButton;
	private TextView mFileNameTextView;
	private TextView mValueMessageTextView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
    	this.setContentView(R.layout.acceptpayment);
    	
    	this.initBanner(getString(R.string.accept_payment_title));
    	
    	this.mBrowseButton = (Button) this.findViewById(R.id.browseButton);
        this.mBrowseButton.setOnClickListener(this);                    	

    	this.mAcceptButton = (Button) this.findViewById(R.id.acceptButton);
        this.mAcceptButton.setOnClickListener(this);                    	

    	this.mFileNameTextView = (TextView) this.findViewById(R.id.fileNameTextView);
    	this.mValueMessageTextView = (TextView) this.findViewById(R.id.valueMessageTextView);
    	
    	try {
			this.retrieveStartupData(savedInstanceState);
        	this.populateControls();    		
		} 
		catch (Exception ex) {
			this.reportError(ex);
		}
    	
		if (this.mValueMessage != null) {
	    	
			this.mBrowseButton.setVisibility(View.GONE);
	    	
	    	RelativeLayout.LayoutParams layoutParams = 
	    			(RelativeLayout.LayoutParams)this.mAcceptButton.getLayoutParams(); 
	    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0); 
	    	layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, -1); 
	    	this.mAcceptButton.setLayoutParams(layoutParams); 
		}
    }


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		
		if (this.mValueMessage != null) {
			try {
				outState.putString(KEY_VALUE_MESSAGE, this.mValueMessage.toBase64String());
			} 
			catch (IOException e) {
				// Ignore this excpetion
			}
		}
	}


	@Override
	public void onClick(View view) {
		
		if (view == this.mBrowseButton) {

			this.showDialog(DIALOG_FILE_BROWSER);

		} else if (view == this.mAcceptButton) {
			
			this.acceptValueMessage();
		
		} 	
	}
	
	
    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case DIALOG_FILE_BROWSER:
            	return new FileBrowser(this, this, START_DIRECTORY);
        }

        return null;
    }
	

	@Override
	public void onDismiss(DialogInterface dialog) {

		if (dialog instanceof FileBrowser) {
			
			FileBrowser fileBrowser = (FileBrowser) dialog;
			String selectedFile = fileBrowser.getSelectedFile();
			
			if (selectedFile != null && selectedFile.length() > 0) {
				
				this.mValueMessageFilePath = selectedFile;
				
				try {
					this.mValueMessage = MintChipUtility.readValueMessageFromFile(this.mValueMessageFilePath);
					this.populateControls();
				} 
				catch (Exception ex) {
					this.reportError(ex);
				}    	
			}
		}
	}
	
	
	private void retrieveStartupData(Bundle savedInstanceState) throws IOException {
		
		if (savedInstanceState != null) {
			String base64Message = savedInstanceState.getString(KEY_VALUE_MESSAGE);

			if (base64Message != null) {
				
				IMessage message = MessageFactory.getInstance().toMessage(base64Message);
				
				if (message instanceof IValueMessage) {
					this.mValueMessage = (IValueMessage) message;
				}
				else {
					throw new IOException("Received message is not of the IValueMessage type.");				
				}				
			}
		}
    	
		if (this.mValueMessage == null) {
			
			Intent intent = this.getIntent();
	    	if (intent != null) {
	    		
	            Uri data = intent.getData();
	            if (data != null)
	            {
	            	if (SCHEME_HTTP.equalsIgnoreCase(data.getScheme())) {
	            		this.mValueMessage = MintChipUtility.downloadValueMessageFromWeb(data.toString());
	            	}
	            	else if (SCHEME_FILE.equalsIgnoreCase(data.getScheme())) {
	    				this.mValueMessageFilePath = data.toString().replace("file://", "");
	            		this.mValueMessage = MintChipUtility.readValueMessageFromFile(this.mValueMessageFilePath);
	            	}
	            }
	    	}
		}
	}

    
	private void populateControls() {

		if (this.mValueMessage == null) {
			return;
		}
		
		this.mValueMessageTextView.setText(MintChipUtility.formatValueMessage(this.mValueMessage));
    	this.mValueMessageTextView.setVisibility(View.VISIBLE);

    	if (this.mValueMessageFilePath != null && this.mValueMessageFilePath.length() != 0) {
    		
    		this.mFileNameTextView.setText(this.mValueMessageFilePath + ":");
        	this.mFileNameTextView.setVisibility(View.VISIBLE);    		
    	}
    	else {
        	this.mFileNameTextView.setVisibility(View.GONE);    		
    	}
	}

	
	private void acceptValueMessage() {
		
		if (this.mValueMessage == null) {
			DialogUtility.displayMessage(this, "", getString(R.string.please_load_value_message));
			return;
		}
		
		try {
			
			String amountLoaded = MintChipUtility.loadValueMessage(this.mValueMessage);

			this.updateBalance();
			
			DialogUtility.displayMessage(this, "", String.format(
					getString(R.string.successfully_accepted_value_message, amountLoaded)));   			
		}
		catch (Exception ex) {
			this.reportError(ex);
		}
	}	
}
