package ca.mint.mintchip.android.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtility {

	public static void displayMessage(Context context, String title, String message) {
		
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		
		dialog.setCancelable(true);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok_button), 
				new DialogInterface.OnClickListener() { 
				    public void onClick(DialogInterface dialog, int whichButton) { 
				        dialog.dismiss(); 
				    } 
		}); 
		dialog.show();
	}
}
