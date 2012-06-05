package ca.mint.mintchip.android.sample;

import ca.mint.mintchip.contract.ILogEntry;
import ca.mint.mintchip.contract.LogType;
import ca.mint.mintchip.contract.MintChipException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class TransactionLogActivity extends BaseActivity {

	public static final String PARAMETER_LOG_TYPE = "ParameterLogType";
	
	private LogType mLogType = LogType.CREDIT;
	private ILogEntry[] mLogEntries;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
    	this.initLogType();   	
    	this.setContentView(R.layout.log);
    	this.initBanner(this.mLogType == LogType.CREDIT ? 
    			getString(R.string.credit_transactions_title) : 
    			getString(R.string.debit_transactions_title));

		try {
			mLogEntries = MintChipUtility.readTransactionLog(this.mLogType);			
		} 
		catch (MintChipException e) {
			this.reportError(e);
			return;
		}

		ListView logView = (ListView) this.findViewById(R.id.logView);
		LogEntryAdapter adapter = new LogEntryAdapter(this, this.mLogEntries, this.mLogType);
		logView.setAdapter(adapter);
    }


	private void initLogType() {
		
		Intent intent = this.getIntent();
    	if (intent != null) {
    		Bundle extras = intent.getExtras();
    		if (extras != null) {
    			int logType = extras.getInt(PARAMETER_LOG_TYPE);
    			mLogType = LogType.CREDIT.getValue() == logType ? LogType.CREDIT : LogType.DEBIT;
    		}
    	}
	}

    
	private static class LogEntryAdapter extends ArrayAdapter<ILogEntry> {

		private LogType mLogType;
		
		public LogEntryAdapter(Context context, ILogEntry[] entries, LogType logType) {
			
			super(context, R.layout.logentry, entries);
			
			this.mLogType = logType;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
			View rowView = inflater.inflate(R.layout.logentry, parent, false);
			
			ILogEntry entry = this.getItem(position);
			
			if (entry != null) {
	            
				TextView entryTextView = (TextView) rowView.findViewById(R.id.entryTextView);
				
				String accountMessage = "";
				if (this.mLogType == LogType.CREDIT) {
					accountMessage = getContext().getString(R.string.received_from) + 
							MintChipUtility.formatId(entry.getPayerId());
				}
				else {
					accountMessage = getContext().getString(R.string.paid_to) + 
							MintChipUtility.formatId(entry.getPayeeId());
				}
				
	    		String text = String.format(getContext().getString(R.string.log_entry_format), 
	    				(position + 1),
	    				MintChipUtility.formatCurrency(entry.getAmount()),
	    				accountMessage,
	    				MintChipUtility.formatDateTime(entry.getTransactionTime()));

	            entryTextView.setText(text);
			}
			
			return rowView;
		}
	}
}
