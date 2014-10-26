package com.geoscope.GeoLog.DEVICE.AudioModule.VoiceCommandModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.TRecognitionListener;
import edu.cmu.pocketsphinx.TSpeechRecognizer;
import edu.cmu.pocketsphinx.TSpeechRecognizerSetup;

public class TVoiceCommandModule extends TModule {

	public static class TCommands extends File {
		
		private static final long serialVersionUID = 1L;
		
		public String Name;
		public String CultureName;
		public String[] Items;
		public boolean flAsGrammar;
		
		public TCommands(String pName, String pCultureName, String[] pItems, boolean pflAsGrammar) throws IOException {
			super(TDEVICEModule.GetTempFolder(),pName+(pflAsGrammar ? ".grm" : ".txt"));
			//.
			Name = pName;
			CultureName = pCultureName;
			Items = pItems;
			flAsGrammar = pflAsGrammar;
			//.
			Initialize();
		}
		
		private void Initialize() throws IOException {
			if (flAsGrammar) {
				FileOutputStream FOS = new FileOutputStream(this);
				try {
					String Header = "#JSGF V1.0;\n";
					String GrammarHeader = "grammar geolog;\n";
					String GrammarName = "public <"+Name+"> = \n";
					//.
					FOS.write(Header.getBytes("utf-8"));
					FOS.write(GrammarHeader.getBytes("utf-8"));
					FOS.write(GrammarName.getBytes("utf-8"));
					int Cnt = Items.length;
					for (int I = 0; I < Cnt; I++) {
						String S;
						if (I < (Cnt-1)) 
							S = Items[I]+"|\n";
						else
							S = Items[I]+";\n";
						FOS.write(S.getBytes("utf-8"));
					}
				}
				finally {
					FOS.close();        
				}
			}
			else {
				FileOutputStream FOS = new FileOutputStream(this);
				try {
					int Cnt = Items.length;
					for (int I = 0; I < Cnt; I++) {
						String S;
						if (I < (Cnt-1)) 
							S = Items[I]+"\n";
						else
							S = Items[I];
						FOS.write(S.getBytes("utf-8"));
					}
				}
				finally {
					FOS.close();        
				}
			}
		}

		public boolean ItemExists(String Item) {
			int Cnt = Items.length;
			for (int I = 0; I < Cnt; I++)
				if (Items[I].equals(Item))
					return true; //. ->
			return false;
		}
	}
	
	public static class TRecognizerParameters {
		
		public double KeywordThreshold = 1e-20;
	}
	
	public static class TRecognizer {
		
		public static boolean Available() {
			return (TGeoLogApplication.GetVoiceRecognizerFolder() != null);
		}
		
		public static final double 	InactivityTimeout = (1.0/(24.0*3600.0))*60000; //. seconds
		public static final int 	InactivityFinalizationTimerInterval = 1000*600; //. seconds
		
		@SuppressWarnings("unused")
		private TVoiceCommandModule VoiceCommandModule;
		//.
		private int 	_RefCount = 0;
		private double	_RefCount_0_Timestamp = 0.0;
		//.
		public String CultureName = "en-us";
		//.
		public TRecognizerParameters Parameters;
		//.
		private TSpeechRecognizer SphinxRecognizer = null;
		//.
		private ArrayList<TCommands> CommandsRepository = new ArrayList<TCommands>();
		//.
		private boolean flInitialized = false;
		private Timer InactivityFinalizationTimer;
		//.
		public boolean flHasListener = false;
		
		public TRecognizer(TVoiceCommandModule pVoiceCommandModule) {
			VoiceCommandModule = pVoiceCommandModule;
			//.
			Parameters = new TRecognizerParameters();
			//.
	        InactivityFinalizationTimer = new Timer();
		}
		
		public synchronized int AddRef() {
			_RefCount++;
			return _RefCount;
		}
		
		public synchronized int Release() {
			_RefCount--;
			if (_RefCount == 0)
				_RefCount_0_Timestamp = OleDate.UTCCurrentTimestamp();
			return _RefCount;
		}
		
		public synchronized int RefCount() {
			return _RefCount;
		}
		
		private synchronized void Initialize(String pCultureName) throws IOException {
			Finalize();
			//.
			String CultureName = pCultureName;
			//.
            String CMUSphinxFolder = TGeoLogApplication.GetVoiceRecognizerFolder();
            if (CMUSphinxFolder == null)
            	throw new IOException("there is no CMU.Sphinx folder"); //. =>
            File VoiceRecognizerFolder = new File(CMUSphinxFolder+"/"+"sync");
            //.
            File ModelsDir = new File(VoiceRecognizerFolder, "models");
            TSpeechRecognizerSetup Setup = TSpeechRecognizerSetup.defaultSetup();
            Setup.setDictionary(new File(ModelsDir, "dict/current."+CultureName));
            Setup.setAcousticModel(new File(ModelsDir, "hmm/current."+CultureName));
            Setup.setKeywordThreshold((float)Parameters.KeywordThreshold);
        	//. Setup.setRawLogDir(VoiceRecognizerFolder)
        	SphinxRecognizer = Setup.getRecognizer();
            //.
	        InactivityFinalizationTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					FinalizeOnInactivity(InactivityTimeout);
				}
			},InactivityFinalizationTimerInterval,InactivityFinalizationTimerInterval);
            //.
            flInitialized = true;
		}
		
		public synchronized void Finalize() {
			if (!flInitialized)
				return; //. ->
			//.
			flInitialized = false;
			//.
			InactivityFinalizationTimer.cancel();
			//.
			CommandsRepository_Clear();
			//.
			if (SphinxRecognizer != null) {
				SphinxRecognizer.cancel();
				SphinxRecognizer = null;
				//.
				System.gc();
			}
		}
		
		public synchronized void CheckInitialization(String pCultureName) throws IOException {
			if (!flInitialized || !CultureName.equals(pCultureName))
				Initialize(pCultureName);
		}
		
		public synchronized void FinalizeOnInactivity(double Timeout) {
			if (flInitialized)
				if ((_RefCount == 0) && (_RefCount_0_Timestamp > 0.0)) {
					if ((OleDate.UTCCurrentTimestamp()-_RefCount_0_Timestamp) > Timeout) 
						Finalize();
				}
		}
		
		public synchronized void CommandsRepository_Add(TCommands Commands) {
        	if (Commands.flAsGrammar)
        		SphinxRecognizer.addGrammarSearch(Commands.Name, Commands);
        	else 
        		SphinxRecognizer.addKeywordSearch(Commands.Name, Commands.getAbsoluteFile());
			//.
			CommandsRepository.add(Commands);
		}
		
		public synchronized void CommandsRepository_Clear() {
			CommandsRepository.clear();
		}
		
		public synchronized boolean CommandsRepository_ItemExists(TCommands Commands) {
			int Cnt = CommandsRepository.size();
			for (int I = 0; I < Cnt; I++)
				if (CommandsRepository.get(I).Name.equals(Commands.Name))
					return true; //. ->
			return false;
		}
		
		public synchronized void AddListener(TRecognitionListener Listener) throws IOException {
			if (flHasListener)
				throw new IOException("Listener has already was set"); //. =>
			SphinxRecognizer.addListener(Listener);
			flHasListener = true;
		}

		public synchronized void RemoveListener(TRecognitionListener Listener) {
			SphinxRecognizer.removeListener(Listener);
			flHasListener = false;
		}
		
	    public synchronized void StartListening(String CommandsName) {
	    	SphinxRecognizer.startListening(CommandsName);
	    }

	    public synchronized void StopListening() {
	    	SphinxRecognizer.stop();
	    }

		public synchronized void CancelListening() {
			SphinxRecognizer.cancel();
		}		

	    public synchronized void StopAndStartListening(String CommandsName) {
	    	SphinxRecognizer.stop();
	    	SphinxRecognizer.startListening(CommandsName);
	    }

	    public synchronized void CancelAndStartListening(String CommandsName) {
			SphinxRecognizer.cancel();
	    	SphinxRecognizer.startListening(CommandsName);
	    }
	}
	
	public static class TCommandHandler implements TRecognitionListener {
		
		public static class TDoOnCommandHandler {
		
			public void DoOnCommand(String Command) {
			}
		}
		
		public static class TDoOnExceptionHandler {
			
			public void DoOnException(Exception E) {
			}
		}
		
		private TVoiceCommandModule VoiceCommandModule;
		//.
		private TCommands Commands;
		//.
		private TDoOnCommandHandler 	OnCommandHandler;
		private TDoOnExceptionHandler 	OnExceptionHandler = null;
	    //.
	    public boolean flInitialized = false;
		
		
		public TCommandHandler(TVoiceCommandModule pVoiceCommandModule, TCommands pCommands, TDoOnCommandHandler pDoOnCommandHandler) {
			VoiceCommandModule = pVoiceCommandModule;
			Commands = pCommands;
			OnCommandHandler = pDoOnCommandHandler;
		}
		
		public void Initialize() throws IOException {
			VoiceCommandModule.Recognizer.AddRef();
			try {
				VoiceCommandModule.Recognizer.CheckInitialization(Commands.CultureName);
				//.
				if (!VoiceCommandModule.Recognizer.CommandsRepository_ItemExists(Commands))
					VoiceCommandModule.Recognizer.CommandsRepository_Add(Commands);
	            //.
				VoiceCommandModule.Recognizer.AddListener(this);
				VoiceCommandModule.Recognizer.StartListening(Commands.Name);
			}
			catch (IOException IOE) {
				VoiceCommandModule.Recognizer.Release();
				//.
				throw IOE; //. =>
			}
		}
		
		public void Finalize() {
			try {
				VoiceCommandModule.Recognizer.CancelListening();
				VoiceCommandModule.Recognizer.RemoveListener(this);
			}
			finally {
				VoiceCommandModule.Recognizer.Release();
			}
		}

		@Override
		public void onBeginningOfSpeech() {
		}

		@Override
		public void onEndOfSpeech() {
			VoiceCommandModule.Recognizer.StopAndStartListening(Commands.Name);
		}

		@Override
		public void onPartialResult(Hypothesis arg0) {
		}

		@Override
		public void onResult(Hypothesis arg0) {
			if (arg0 == null)
				return; //. ->
			//.
			final String Command = arg0.getHypstr();
			if (!Commands.ItemExists(Command))
				return; //. ->
			//.
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						OnCommandHandler.DoOnCommand(Command);
		        	}
		        	catch (Throwable E) {
		        		TGeoLogApplication.Log_WriteError(E);
		        	}
				}
			});
			thread.start();
		}

		public void onError(Exception exception) {
			if (OnExceptionHandler != null)
				OnExceptionHandler.DoOnException(exception);
		}
	}

	
	@SuppressWarnings("unused")
	private TAudioModule AudioModule;
	//.
	public TRecognizer Recognizer = null;
	
    public TVoiceCommandModule(TAudioModule pAudioModule) {
    	super(pAudioModule);
    	//.
    	AudioModule = pAudioModule;
    	//.
    	flEnabled = false;
    	//.
    	Recognizer =new TRecognizer(this);
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, "VoiceCommandModule profile loading error: "+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() {
    	if (Recognizer != null) {
    		Recognizer.Finalize();
    		Recognizer = null;
    	}
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    }
    
    @Override
    public void Stop() throws Exception {
    	//.
    	super.Stop();
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		String CFN = ModuleFile();
		File F = new File(CFN);
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(CFN);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		Element RootNode = XmlDoc.getDocumentElement();
		Node ParentModuleNode = TMyXML.SearchNode(RootNode,"AudioModule");
		if (ParentModuleNode == null) 
			return; //. ->
		Node ModuleNode = TMyXML.SearchNode(ParentModuleNode,"VoiceCommandModule");
		if (ModuleNode == null) 
			return; //. ->
		int Version = Integer.parseInt(TMyXML.SearchNode(ModuleNode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		
		case 1:
			try {
				flEnabled = (Integer.parseInt(TMyXML.SearchNode(ModuleNode,"flEnabled").getFirstChild().getNodeValue()) != 0);
				//. Recognizer				
				Node RecognizerNode = TMyXML.SearchNode(ModuleNode,"Recognizer");
				if (RecognizerNode != null) {
					Node ANode = TMyXML.SearchNode(RecognizerNode,"KeywordThreshold");
					if (ANode != null)
						Recognizer.Parameters.KeywordThreshold = Double.parseDouble(ANode.getFirstChild().getNodeValue());
				}
			}
			catch (Exception E) {
    			throw new Exception("error of profile: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown profile version, version: "+Integer.toString(Version)); //. =>
		}
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        Serializer.startTag("", "VoiceCommandModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        int V = 0;
        if (flEnabled)
        	V = 1;
        Serializer.startTag("", "flEnabled");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "flEnabled");
		//. Recognizer				
        Serializer.startTag("", "Recognizer");
        //.
        Serializer.startTag("", "KeywordThreshold");
        Serializer.text(Double.toString(Recognizer.Parameters.KeywordThreshold));
        Serializer.endTag("", "KeywordThreshold");
        //.
        Serializer.endTag("", "Recognizer");
        //. 
        Serializer.endTag("", "VoiceCommandModule");
    }
    
    public TCommandHandler CommandHandler_Create(TCommands pCommands, TCommandHandler.TDoOnCommandHandler pDoOnCommandHandler) {
    	return (new TCommandHandler(this,pCommands,pDoOnCommandHandler));
    }
}
