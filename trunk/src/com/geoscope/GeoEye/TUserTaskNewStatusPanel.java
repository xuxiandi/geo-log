package com.geoscope.GeoEye;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue;

@SuppressLint("HandlerLeak")
public class TUserTaskNewStatusPanel extends Activity {

	public boolean flExists = false;
	//.
	private boolean flOriginator = false;
	//.
	private int[] StatusCodes = null;
	//. 
	@SuppressWarnings("unused")
	private TextView 	lbTaskStatus;
	private Spinner 	spTaskStatus;
	private EditText 	edTaskStatusReason;
	private EditText 	edTaskStatusComment;
	//.
	private LinearLayout	llTaskResult;
	private EditText 		edTaskResultCode;
	private EditText 		edTaskResultComment;
	//.
	private Button btnChangeStatus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	flOriginator = extras.getBoolean("flOriginator");
        }
        if (flOriginator)
        	StatusCodes = TTaskStatusValue.OriginatorStatuses;
        else
        	StatusCodes = TTaskStatusValue.ExpertStatuses;
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_task_newstatus_panel);
        //.
        lbTaskStatus = (TextView)findViewById(R.id.lbTaskStatus);
        spTaskStatus = (Spinner)findViewById(R.id.spTaskStatus);
        String[] TaskStatuses = new String[StatusCodes.length+1];
        TaskStatuses[0] = getString(R.string.SNotSelected);
        for (int I = 0; I < StatusCodes.length; I++)
        	TaskStatuses[I+1] = TTaskStatusValue.Status_String(StatusCodes[I], this);
        ArrayAdapter<String> saTaskStatus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, TaskStatuses);
        saTaskStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTaskStatus.setAdapter(saTaskStatus);
        spTaskStatus.setSelection(0);
        spTaskStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	int Idx = (position-1);
            	llTaskResult.setVisibility(((Idx >= 0) && (StatusCodes[Idx] == TTaskStatusValue.MODELUSER_TASK_STATUS_Processed)) ? View.VISIBLE : View.GONE); 
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            	llTaskResult.setVisibility(View.GONE);
            }
        });        
        edTaskStatusReason = (EditText)findViewById(R.id.edTaskStatusReason);
        edTaskStatusReason.setText("0");
        edTaskStatusComment = (EditText)findViewById(R.id.edTaskStatusComment);
    	llTaskResult = (LinearLayout)findViewById(R.id.llTaskResult);
    	edTaskResultCode = (EditText)findViewById(R.id.edTaskResultCode);
    	edTaskResultCode.setText("0");
    	edTaskResultComment = (EditText)findViewById(R.id.edTaskResultComment);
        btnChangeStatus = (Button)findViewById(R.id.btnChangeStatus);
        btnChangeStatus.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
    		    /* new AlertDialog.Builder(TUserTaskNewStatusPanel.this)
    	        .setIcon(android.R.drawable.ic_dialog_alert)
    	        .setTitle(R.string.SConfirmation)
    	        .setMessage(R.string.SChangeTheStatus)
    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
    		    	@Override
    		    	public void onClick(DialogInterface dialog, int id) {
    	        		try {
    						SetResult();
    		        		TUserTaskNewStatusPanel.this.finish();
    					} catch (Exception E) {
    		                Toast.makeText(TUserTaskNewStatusPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    					}
    		    	}
    		    })
    		    .setNegativeButton(R.string.SNo, null)
    		    .show();*/
        		try {
					SetResult();
	        		TUserTaskNewStatusPanel.this.finish();
				} catch (Exception E) {
	                Toast.makeText(TUserTaskNewStatusPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void SetResult() throws Exception {
    	int Idx = (spTaskStatus.getSelectedItemPosition()-1);
    	if (Idx < 0)
    		throw new Exception(getString(R.string.SStatusIsNotSelected)); //. =>
		int Status = StatusCodes[Idx];
		int StatusReason = Integer.parseInt(edTaskStatusReason.getText().toString());
		String StatusComment = edTaskStatusComment.getText().toString();
		//.
    	Intent intent = getIntent();
    	//.
    	intent.putExtra("Status",Status);
    	intent.putExtra("StatusReason",StatusReason);
    	intent.putExtra("StatusComment",StatusComment);
    	//.
    	if (Status == TTaskStatusValue.MODELUSER_TASK_STATUS_Processed) {
    		int ResultCode = Integer.parseInt(edTaskResultCode.getText().toString());
    		String ResultComment = edTaskResultComment.getText().toString();
    		//.
        	intent.putExtra("ResultCode",ResultCode);
        	intent.putExtra("ResultComment",ResultComment);
    	}
        //.
    	setResult(Activity.RESULT_OK,intent);
	}
}
