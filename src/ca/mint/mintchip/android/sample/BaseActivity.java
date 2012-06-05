package ca.mint.mintchip.android.sample;

import ca.mint.mintchip.contract.MintChipException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

/**
 * 
 * The superclass of all activity classes in the application.
 *
 */
abstract class BaseActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
    	//Remove title bar 
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);    	
    }
    
    
	@Override
	protected void onStart() {
		
		super.onStart();

    	try {
    		
        	this.updateBalance();
		} 
		catch (MintChipException e) {
			this.reportError(e);
		}
	}


	protected void initBanner(String title) {
		
    	TextView  titleTextView = (TextView) findViewById(R.id.titleTextView);
    	titleTextView.setText(title);
    	    	
    	try {
    		
        	TextView  accountTextView = (TextView) findViewById(R.id.accountTextView);
			accountTextView.setText(MintChipUtility.getFormattedMintChipID());
		} 
		catch (MintChipException e) {
			this.reportError(e);
		}
	}


	protected void updateBalance() throws MintChipException {

    	TextView  balanceTextView = (TextView) findViewById(R.id.balanceTextView);

    	if (balanceTextView != null) {
    		balanceTextView.setText(MintChipUtility.getFormattedBalance());
    	}
    }

	
	protected void reportError(Exception exception) {
		
		Log.e("MintChip Sample", "Error", exception);
		
		DialogUtility.displayMessage(this, "Error", exception.toString());		
	}
}
