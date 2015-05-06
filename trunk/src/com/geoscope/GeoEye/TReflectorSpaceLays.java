package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.TSpaceLay;

public class TReflectorSpaceLays {

	public class TSuperLays {
		
		public static final String SuperLaysFileName = "ReflectionWindow_SuperLays.xml";
		
		
		public class TSuperLay {
			public String	ID;
			public String 	Name;
			public boolean 	Enabled;
			public int[] 	LayIDs;
		}
		
		private TReflectorSpaceLays SpaceLays;
		public TSuperLay[] Items = null;
		
		public TSuperLays(TReflectorSpaceLays pSpaceLays) throws Exception {
			SpaceLays = pSpaceLays;
			//.
			Load();
		}
		
		private synchronized void Load() throws Exception {
			String FN = TReflectorComponent.ProfileFolder()+"/"+SuperLaysFileName;
			File F = new File(FN);
			if (!F.exists()) {
				Items = new TSuperLay[0];
				return; //. ->
			}
			//.
			byte[] XML;
	    	long FileSize = F.length();
	    	FileInputStream FIS = new FileInputStream(FN);
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
			int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
			switch (Version) {
			case 0:
				NodeList NL = XmlDoc.getDocumentElement().getElementsByTagName("Items");
				if (NL != null) {
					NodeList ItemsNode = NL.item(0).getChildNodes();
					Items = new TSuperLay[ItemsNode.getLength()];
					for (int I = 0; I < Items.length; I++) {
						Node ItemNode = ItemsNode.item(I);
						NodeList ItemChildsNode = ItemNode.getChildNodes();
						//.
						TSuperLay Item = new TSuperLay();
						Item.ID = ItemChildsNode.item(0).getFirstChild().getNodeValue();
						Item.Name = ItemChildsNode.item(1).getFirstChild().getNodeValue();
						Item.Enabled = (Integer.parseInt(ItemChildsNode.item(2).getFirstChild().getNodeValue()) != 0);
						NodeList LaysNode = ItemChildsNode.item(3).getChildNodes();
						Item.LayIDs = new int[LaysNode.getLength()];
						for (int J = 0; J < Item.LayIDs.length; J++) {
							NodeList LayChilds = LaysNode.item(J).getChildNodes();
							Item.LayIDs[J] = Integer.parseInt(LayChilds.item(0).getFirstChild().getNodeValue());
						}
						Items[I] = Item;
					}
				}
				break; //. >
			default:
				throw new Exception("unknown data version, version: "+Integer.toString(Version)); //. =>
			}
			//.
			Validate();
		}
		
		public synchronized void Save() throws IllegalArgumentException, IllegalStateException, IOException {
	    	int Version = 0;
			String FN = TReflectorComponent.ProfileFolder()+"/"+SuperLaysFileName;
			File F = new File(FN);
			if (!F.exists()) {
				F.getParentFile().mkdirs();
				F.createNewFile();
			}
		    XmlSerializer serializer = Xml.newSerializer();
		    FileWriter writer = new FileWriter(FN);
		    try {
		        serializer.setOutput(writer);
		        serializer.startDocument("UTF-8",true);
		        serializer.startTag("", "ROOT");
		        //.
	            serializer.startTag("", "Version");
	            serializer.text(Integer.toString(Version));
	            serializer.endTag("", "Version");
		        //. Items
	            serializer.startTag("", "Items");
	            	for (int I = 0; I < Items.length; I++) {
		            	serializer.startTag("", "Item"+Integer.toString(I));
		            		//. ID
		            		serializer.startTag("", "ID");
		            		serializer.text(Items[I].ID);
		            		serializer.endTag("", "ID");
		            		//. Name
		            		serializer.startTag("", "Name");
		            		serializer.text(Items[I].Name);
		            		serializer.endTag("", "Name");
		            		//. Enabled
		            		serializer.startTag("", "Enabled");
		            		if (Items[I].Enabled)
		            			serializer.text("-1");
		            		else
		            			serializer.text("0");
		            		serializer.endTag("", "Enabled");
		            		//. Lays
		    	            serializer.startTag("", "Lays");
		    	            	for (int J = 0; J < Items[I].LayIDs.length; J++) {
		    		            	serializer.startTag("", "Lay"+Integer.toString(J));
		    		            		//. ID
				            			serializer.startTag("", "ID");
				            			serializer.text(Integer.toString(Items[I].LayIDs[J]));
				            			serializer.endTag("", "ID");
		    		            	serializer.endTag("", "Lay"+Integer.toString(J));
		    	            	}
		    	            serializer.endTag("", "Lays");
		            	serializer.endTag("", "Item"+Integer.toString(I));
	            	}
	            serializer.endTag("", "Items");
	            //.
		        serializer.endTag("", "ROOT");
		        serializer.endDocument();
		    }
		    finally {
		    	writer.close();
		    }
		}
		
		public synchronized void Validate() {
			if (Items == null)
				return; //. ->
			for (int I = 0; I < Items.length; I++) 
				SpaceLays.DisableEnableLaysByIDs(Items[I].LayIDs,Items[I].Enabled);
		}
		
		public synchronized void EnableDisableItem(int Index, boolean flEnable) {
			Items[Index].Enabled = flEnable;
			SpaceLays.DisableEnableLaysByIDs(Items[Index].LayIDs,Items[Index].Enabled);
			//.
			try {
				Save();
				//.
				switch (SpaceLays.Reflector.ViewMode) {
				case TReflectorComponent.VIEWMODE_REFLECTIONS: 
					SpaceLays.Reflector.ClearReflections(true);
					SpaceLays.Reflector.ClearHints(true);
					break; //. >
					
				case TReflectorComponent.VIEWMODE_TILES: 
					SpaceLays.Reflector.ClearHints(true);
					break; //. >
				}
				if (SpaceLays.Reflector.ViewMode == TReflectorComponent.VIEWMODE_REFLECTIONS)
				SpaceLays.Reflector.StartUpdatingSpaceImage();
			} catch (Exception E) {
	            Toast.makeText(SpaceLays.Reflector.context, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
		
		public synchronized AlertDialog CreateSelectorPanel(Activity ParentActivity, String pTitle) {
	    	final CharSequence[] _items = new CharSequence[Items.length];
	    	final boolean[] Mask = new boolean[Items.length];
	    	for (int I = 0; I < Items.length; I++) {
	    		_items[I] = Items[I].Name;
	    		Mask[I] = Items[I].Enabled;
	    	}
	    	AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
	    	builder.setTitle(pTitle);
	    	builder.setNegativeButton(R.string.SClose,null);
	    	builder.setMultiChoiceItems(_items, Mask, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
					EnableDisableItem(arg1, arg2);
				}
				
	    	});
	    	AlertDialog alert = builder.create();
	    	return alert;
		}
	}
	
	private TReflectorComponent Reflector;
	public TSpaceLay[] Items;
	private int DisabledItemsCount = 0;
	//.
	public TSuperLays SuperLays;
	
	public TReflectorSpaceLays(TReflectorComponent pReflector) throws Exception {
		Reflector = pReflector;
        //.
		LoadStructure();
		//.
        if (Reflector.Configuration.ReflectionWindow_DisabledLaysIDs != null)
        	DisableLaysByIDs(Reflector.Configuration.ReflectionWindow_DisabledLaysIDs);
        //.
        SuperLays = new TSuperLays(this);
	}
	
	private String PrepareSpaceStructureURL() {

		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(Reflector.User.UserID);
		String URL2 = "SpaceStructure.txt";
		//. add command parameters
		URL2 = URL2+"?"+"1";
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".txt";
		return URL;
	}

	private String[] GetSpaceStructure() throws Exception {
		String[] Result;
		String CommandURL = PrepareSpaceStructureURL();
		//.
		try {
			HttpURLConnection Connection = Reflector.Server.OpenConnection(CommandURL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					String S,S1;
					byte[] Data = new byte[1024];
					int Size;
					StringBuilder sb = new StringBuilder();
					while (true) {
						Size = in.read(Data);
						if (Size <= 0) 
							break; //. >
						S1 = new String(Data, 0,Size, "windows-1251");
						sb.append(S1);
					}	
					S = sb.toString();
					//.
					Result = S.split("\r\n");
				}
				finally {
					in.close();
				}                
			}
			finally {
				Connection.disconnect();
			}
		} 
		catch (Exception E) {
			throw E; //. =>
		}
		return Result;
	}
	
	private void CalculateDisabledItemsCount() {
		DisabledItemsCount = 0;
		for (int I = 0; I < Items.length; I++)
			if (!Items[I].flEnabled)
				DisabledItemsCount++;		
	}
	
	public synchronized void LoadStructure() throws Exception {
		String[] LayStrings = GetSpaceStructure();
		Items = new TSpaceLay[LayStrings.length];
		for (int I = 0; I < LayStrings.length; I++) {
			String S = LayStrings[I];
			int DI = S.indexOf(" "); 
			String LayIDStr = S.substring(0,DI); 
			String LayName = S.substring(DI+1);
			int LayID = Integer.parseInt(LayIDStr);
			Items[I] = new TSpaceLay(LayID,LayName);
		}
		DisabledItemsCount = 0;
	}
	
	public synchronized void DisableEnableLaysByIDs(int[] IDs, boolean flEnabled) {
		if (IDs == null)
			return; //. ->
		for (int I = 0; I < IDs.length; I++)
			for (int J = 0; J < Items.length; J++)
				if (Items[J].ID == IDs[I])
					Items[J].flEnabled = flEnabled;
		CalculateDisabledItemsCount();
	}
	
	public synchronized void DisableLaysByIDs(int[] IDs) {
		if (IDs == null)
			return; //. ->
		for (int I = 0; I < IDs.length; I++)
			for (int J = 0; J < Items.length; J++)
				if (Items[J].ID == IDs[I])
					Items[J].flEnabled = false;
		CalculateDisabledItemsCount();
	}
	
	public synchronized void DisableLaysByIndexes(short[] Indexes) {
		if (Indexes == null)
			return; //. ->
		for (int I = 0; I < Indexes.length; I++)
			Items[Indexes[I]-1].flEnabled = false;
		CalculateDisabledItemsCount();
	}
	
	public synchronized void DisableEnableItem(int ItemIndex, boolean flDisable) {
		if (flDisable)
		{
			if (Items[ItemIndex].flEnabled) {
				Items[ItemIndex].flEnabled = false;
				DisabledItemsCount++;
			}
		}
		else {
			if (!Items[ItemIndex].flEnabled) {
				Items[ItemIndex].flEnabled = true;
				DisabledItemsCount--;
			}
		}
	}
	
	public synchronized int[] GetDisabledLaysIDs() {
		if (DisabledItemsCount == 0)
			return null; //. ->
		int[] Result = new int[DisabledItemsCount];
		int Idx = 0;
		for (int I = 0; I < Items.length; I++)
			if (!Items[I].flEnabled) {
				Result[Idx] = Items[I].ID;
				Idx++;
			}
		return Result;
	}
	
	public synchronized short[] GetDisabledLaysIndexes() {
		if (DisabledItemsCount == 0)
			return null; //. ->
		short[] Result = new short[DisabledItemsCount];
		int Idx = 0;
		for (int I = 0; I < Items.length; I++)
			if (!Items[I].flEnabled) {
				Result[Idx] = (short)(I+1);
				Idx++;
			}
		return Result;
	}
	
	public synchronized AlertDialog CreateLaySelectorPanel(Activity ParentActivity) {
    	final CharSequence[] _items = new CharSequence[Items.length];
    	final boolean[] Mask = new boolean[Items.length];
    	for (int I = 0; I < Items.length; I++) {
    		_items[I] = Items[I].Name;
    		Mask[I] = Items[I].flEnabled;
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
    	builder.setTitle(R.string.SLays);
    	builder.setNegativeButton(R.string.SClose,null);
    	builder.setMultiChoiceItems(_items, Mask, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				DisableEnableItem(arg1, !arg2);
			}
			
    	});
    	AlertDialog alert = builder.create();
    	return alert;
	}
}
