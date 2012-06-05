package ca.mint.mintchip.android.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class PromptDialog extends AlertDialog {

	private EditText mInput;
	
	public PromptDialog(
			Context context, 
			String title, 
			String label, 
			String initialValue,
			DialogInterface.OnDismissListener dismisslistener, 
			DialogInterface.OnCancelListener cancelListener) {
		
		super(context);
		
		this.mInput = new EditText(context);
		this.mInput.setText(initialValue);

		this.setTitle(title);
		this.setMessage(label);
		this.setCancelable(true);
		this.setOnDismissListener(dismisslistener);
		this.setOnCancelListener(cancelListener);
		
		this.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok_button), 
				new DialogInterface.OnClickListener() { 
				    public void onClick(DialogInterface dialog, int whichButton) { 		    	
				        dialog.dismiss(); 
				    } 
		}); 
		
		this.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel_button), 
				new DialogInterface.OnClickListener() { 
				    public void onClick(DialogInterface dialog, int whichButton) {		    	
				    	PromptDialog.this.mInput.setText("");
				    	dialog.cancel(); 
				    } 
		}); 
		
		this.setView(mInput);
	}
	
	
	public String getInputValue() {
		
		return this.mInput.getText().toString();
	}

	
	public void setInputValue(String value) {
		
		this.mInput.setText(value);
	}
}
