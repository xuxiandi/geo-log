package com.geoscope.GeoLog.DEVICE.AudioModule.VoiceCommandModule;

import static edu.cmu.pocketsphinx.TSpeechRecognizerSetup.defaultSetup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class TVoiceCommandModule extends TModule {

	public static class TGrammar extends File {
		
		private static final long serialVersionUID = 1L;
		
		public String Name;
		public String[] Items;
		
		public TGrammar(String pName, String[] pItems) throws IOException {
			super(TDEVICEModule.GetTempFolder(),pName+".grm");
			//.
			Name = pName;
			Items = pItems;
			//.
			Initialize();
		}
		
		private void Initialize() throws IOException {
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
		
		public boolean ItemExists(String Item) {
			int Cnt = Items.length;
			for (int I = 0; I < Cnt; I++)
				if (Items[I].equals(Item))
					return true; //. ->
			return false;
		}
	}
	
	public static class TCommandHandler implements RecognitionListener {
		
		public static boolean Available() {
			return (TGeoLogApplication.GetVoiceRecognizerFolder() != null);
		}
		
		public static class TDoOnCommandHandler {
		
			public void DoOnCommand(String Command) {
			}
		}
		
		public static class TDoOnExceptionHandler {
			
			public void DoOnException(Exception E) {
			}
		}
		
		@SuppressWarnings("unused")
		private TVoiceCommandModule VoiceCommandModule;
		private String CultureName;
		private TGrammar CommandGrammar;
		//.
		private TDoOnCommandHandler 	OnCommandHandler;
		private TDoOnExceptionHandler 	OnExceptionHandler = null;
		//.
	    private SpeechRecognizer VoiceRecognizer = null;
	    //.
	    public boolean flInitialized = false;
		
		
		public TCommandHandler(TVoiceCommandModule pVoiceCommandModule, String pCultureName, TGrammar pCommandGrammar, TDoOnCommandHandler pDoOnCommandHandler) {
			VoiceCommandModule = pVoiceCommandModule;
			CultureName = pCultureName;
			CommandGrammar = pCommandGrammar;
			OnCommandHandler = pDoOnCommandHandler;
		}
		
		public void Initialize() throws IOException {
			synchronized (this) {
	            String CMUSphinxFolder = TGeoLogApplication.GetVoiceRecognizerFolder();
	            if (CMUSphinxFolder == null)
	            	throw new IOException("there is no CMU.Sphinx folder"); //. =>
	            File VoiceRecognizerFolder = new File(CMUSphinxFolder+"/"+"sync");
	            //.
	            File ModelsDir = new File(VoiceRecognizerFolder, "models");
	            VoiceRecognizer = defaultSetup()
	            	.setDictionary(new File(ModelsDir, "dict/current."+CultureName))
	            	.setAcousticModel(new File(ModelsDir, "hmm/current."+CultureName))
	            	//. .setRawLogDir(VoiceRecognizerFolder)
	            	.getRecognizer();
	            //. set recognizing type (by language model or grammar)
	            File LanguageModel = new File(ModelsDir, "lm/current."+CultureName);
	            if (LanguageModel.exists())
	            	VoiceRecognizer.addNgramSearch(CommandGrammar.Name, LanguageModel);
	            else
	            	VoiceRecognizer.addGrammarSearch(CommandGrammar.Name, CommandGrammar);
	            //.
	            VoiceRecognizer.addListener(this);
	            //.
	            flInitialized = true;
			}
            //.
            StartListening();
		}
		
		public void Finalize() {
			synchronized (this) {
				if (flInitialized) {
					flInitialized = false; 
					//.
					VoiceRecognizer.cancel();
		            VoiceRecognizer.removeListener(this);
					VoiceRecognizer = null;
				}
			}
		}

	    public void StartListening() {
	    	VoiceRecognizer.stop();
	    	VoiceRecognizer.startListening(CommandGrammar.Name);
	    }

		@Override
		public void onBeginningOfSpeech() {
		}

		@Override
		public void onEndOfSpeech() {
			StartListening();
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
			if (!CommandGrammar.ItemExists(Command))
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
	
    public TVoiceCommandModule(TAudioModule pAudioModule) {
    	super(pAudioModule);
    	//.
    	AudioModule = pAudioModule;
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, "VoiceCommandModule profile loading error: "+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() {
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
        //. 
        Serializer.endTag("", "VoiceCommandModule");
    }
    
    public TCommandHandler CommandHandler_Create(String pCultureName, TGrammar pCommandGrammar, TCommandHandler.TDoOnCommandHandler pDoOnCommandHandler) {
    	return (new TCommandHandler(this,pCultureName,pCommandGrammar,pDoOnCommandHandler));
    }
}
