package com.geoscope.Classes.Data.Types.String;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.geoscope.GeoEye.R;

public class TEnterStringDialog {

    public static class TOnStringEnteredHandler {
    	
    	public void DoOnStringEntered(String Str) throws Exception {
    	}
    }
    
    public static void Dialog(final Context context, String Title, String Message, String Str, final int StringMaxSize, final TOnStringEnteredHandler OnStringEnteredHandler) {
		final EditText input = new EditText(context);
		input.setText(Str);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		//.
		final AlertDialog dlg = new AlertDialog.Builder(context)
		//.
		.setTitle(Title)
		.setMessage(Message)
		//.
		.setView(input)
		.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				//. hide keyboard
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
				//.
				try {
					String Str = input.getText().toString();
    				if (Str.length() > StringMaxSize)
    					Str = Str.substring(0,StringMaxSize);
    				//.
					OnStringEnteredHandler.DoOnStringEntered(Str);
				} catch (Exception E) {
					Toast.makeText(context, E.getMessage(),	Toast.LENGTH_LONG).show();
				}
			}
		})
		//.
		.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// . hide keyboard
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		}).create();
		//.
		input.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick(); 
				return false;
			}
        });        
		// .
		dlg.show();
    }
}
