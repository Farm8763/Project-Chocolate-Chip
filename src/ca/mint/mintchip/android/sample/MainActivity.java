package ca.mint.mintchip.android.sample;

import ca.mint.mintchip.contract.IValueMessage;
import ca.mint.mintchip.contract.LogType;
import ca.mint.mintchip.contract.MintChipException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends BaseActivity implements View.OnClickListener {

	private Button mCreatePaymentButton;
	private Button mAcceptPaymentButton;
	private Button mCreditsButton;
	private Button mDebitsButton;
	private Button mLastDebitButton;
	private Button mChipInfoButton;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
    	this.setContentView(R.layout.main);
    	
    	this.initBanner(this.getString(R.string.app_name));

    	this.mCreatePaymentButton = (Button) this.findViewById(R.id.makePaymentButton);
        this.mCreatePaymentButton.setOnClickListener(this);                

    	this.mAcceptPaymentButton = (Button) this.findViewById(R.id.acceptPaymentButton);
        this.mAcceptPaymentButton.setOnClickListener(this);                

    	this.mCreditsButton = (Button) this.findViewById(R.id.creditsButton);
        this.mCreditsButton.setOnClickListener(this);                

        this.mDebitsButton = (Button) this.findViewById(R.id.debitsButton);
        this.mDebitsButton.setOnClickListener(this);                
    	
    	this.mLastDebitButton = (Button) this.findViewById(R.id.lastDebitButton);
        this.mLastDebitButton.setOnClickListener(this);                
    	
        this.mChipInfoButton = (Button) this.findViewById(R.id.chipInfoButton);
        this.mChipInfoButton.setOnClickListener(this);        
    }

    
	@Override
	public void onClick(View view) {

		if (view == this.mCreatePaymentButton) {
			
			this.startCreatePaymentActivity();

		} else if (view == this.mAcceptPaymentButton) {
			
			this.startAcceptPaymentActivity();
		
		} else if (view == this.mCreditsButton) {
			
	        this.showTransactionLog(LogType.CREDIT);
		
		} else if (view == this.mDebitsButton) {
			
	        this.showTransactionLog(LogType.DEBIT);
		
		} else if (view == this.mLastDebitButton) {
			
    		this.showLastDebit();			
    		
		} else if (view == this.mChipInfoButton) {
			
    		this.showChipInfo();			
    		
		}
	}


	private void startCreatePaymentActivity() {
		
		Intent intent = new Intent(this, MakePaymentActivity.class);
		
		this.startActivity(intent);
	}


	private void startAcceptPaymentActivity() {
		
		Intent intent = new Intent(this, AcceptPaymentActivity.class);
		
		this.startActivity(intent);
	}
	

	private void showTransactionLog(LogType logType) {
		
		Intent intent = new Intent(this, TransactionLogActivity.class);
		intent.putExtra(TransactionLogActivity.PARAMETER_LOG_TYPE, logType.getValue());
		
		this.startActivity(intent);
	}
	
	
	protected void showLastDebit() {
		
		try {
			
			IValueMessage valueMessage = MintChipUtility.getLastCreatedValueMessage();

			if (valueMessage != null) {
				DialogUtility.displayMessage(this, getString(R.string.last_debit_title), 
						MintChipUtility.formatValueMessage(valueMessage));
			}
			else {
				DialogUtility.displayMessage(this, getString(R.string.last_debit_title), 
						getString(R.string.no_debit_transaction));
			}
		} 
		catch (MintChipException e) {
			this.reportError(e);
		}
	}

	
	private void showChipInfo() {
		
		try {
			
			String chipInfo = MintChipUtility.getFormattedChipInfo();
			
			DialogUtility.displayMessage(this, getString(R.string.mintchip_info), chipInfo);			
		}
		catch (MintChipException e) {
			this.reportError(e);
		}
	}	
}
