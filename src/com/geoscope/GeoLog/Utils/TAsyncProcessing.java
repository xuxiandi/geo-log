package com.geoscope.GeoLog.Utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Message;

import com.geoscope.GeoEye.R;

@SuppressLint("HandlerLeak")
public class TAsyncProcessing extends TCancelableThread {

	private static final int MESSAGE_EXCEPTION = 0;
	private static final int MESSAGE_COMPLETED = 1;
	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
		
	private Context context;
	
    private ProgressDialog 	progressDialog = null; 
    private String 			progressDialog_Name = null; 
	
	public TAsyncProcessing(Context pcontext, String Name) {
		context = pcontext;
		progressDialog_Name = Name;
		//.
		_Thread = new Thread(this);
	}

	public TAsyncProcessing(Context pcontext) {
		this(pcontext,null);
	}
	
	public void Destroy() {
		Stop();
	}

	public void Start() {
		try {
			DoOnStart();
		}
		catch (Exception E) {
			DoOnException(E);
		}
		_Thread.start();
	}
	
	public void Stop() {
		CancelAndWait();
	}
	
	@Override
	public void run() {
		try {
			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
			try {
				Process();
				//.
				if (Canceller.flCancel)
					throw new CancelException(); //. =>
			}
			finally {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
			}
			//.
			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
    	}
    	catch (InterruptedException E) {
    		try {
    			DoOnCancel();
    		}
        	catch (Exception Ex) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
    	}
    	catch (CancelException CE) {
    		try {
    			DoOnCancel();
    		}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
    	}
    	catch (Exception E) {
			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    	}
    	catch (Throwable E) {
			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
    	}
	}

    private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
            case MESSAGE_EXCEPTION:
            	Exception E = (Exception)msg.obj;
            	DoOnException(E);
            	break; //. >
            	
            case MESSAGE_COMPLETED:
            	DoOnCompleted();
            	break; //. >
            	
            case MESSAGE_PROGRESSBAR_SHOW:
            	if (context != null) {
                	progressDialog = new ProgressDialog(context);
                	if (progressDialog_Name != null)
                		progressDialog.setMessage(context.getString(R.string.SWaitAMoment));    
                	else
                		progressDialog.setMessage(progressDialog_Name);
                	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
                	progressDialog.setIndeterminate(ProcessIsIndeterminate()); 
                	progressDialog.setCancelable(true);
                	progressDialog.setOnCancelListener( new OnCancelListener() {
    					@Override
    					public void onCancel(DialogInterface arg0) {
    						Cancel();
    					}
    				});
                	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
                		@Override 
                		public void onClick(DialogInterface dialog, int which) { 
    						Cancel();
                		} 
                	}); 
                	//.
                	progressDialog.show(); 	            	
            	}
            	else
            		progressDialog = null;
            	//.
            	break; //. >

            case MESSAGE_PROGRESSBAR_HIDE:
            	if (progressDialog != null)
            		progressDialog.dismiss(); 
            	//.
            	break; //. >
            
            case MESSAGE_PROGRESSBAR_PROGRESS:
            	if (progressDialog != null)
            		progressDialog.setProgress((Integer)msg.obj);
            	//.
            	break; //. >
            }
        }
    };
    
    public boolean ProcessIsIndeterminate() {
    	return true;
    }
    
    public void DoOnStart() throws Exception {
    }

    public void Process() throws Exception {
    }
    
    public void DoOnProgress(int Percentage) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,Percentage).sendToTarget();
    }
    
    public void DoOnCancel() throws Exception {
    }

    public void DoOnCompleted() {
    }

    public void DoOnException(Exception E) {
    }
}
