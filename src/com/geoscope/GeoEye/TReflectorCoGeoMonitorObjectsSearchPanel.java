package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TReflectorCoGeoMonitorObjectsSearchPanel extends Activity {

	private static class TItem {
		
		public int 		idTComponent;
		public long 	idComponent;
		public int 		idCoType;
		//.
		public String Name = "";
		public String Domains = "";
		//.
		public boolean flOnline;
		
		public boolean IsValid() {
			return ((idTComponent == SpaceDefines.idTCoComponent) && (idCoType == SpaceDefines.idTCoGeoMonitorObject));
		}
		
		public long ID() {
			return idComponent;
		}
		
		public String Text() {
			String S = Name;
			if (Domains.length() > 0)
				S += " "+"/"+Domains+"/";
			return S;
		}

		public String Text1() {
			String OS;
			if (flOnline)
				OS = "[+]";
			else
				OS = "[-]";
			String S = OS+" "+Name;
			if (Domains.length() > 0)
				S += " "+"/"+Domains+"/";
			return S;
		}
	}
	
	private static class TItems {
		
		public ArrayList<TItem> Items = new ArrayList<TItem>();

		public TItems(byte[] BA) throws Exception {
			FromByteArray(BA);
		}
		
		public void FromByteArray(byte[] BA) throws Exception {
	    	Document XmlDoc;
			ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
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
			FromXMLNode(RootNode);
		}

		public void FromXMLNode(Node ANode) throws Exception {
			int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
			switch (Version) {
			case 1:
				try {
	    			Items.clear();
					NodeList ItemsNode = TMyXML.SearchNode(ANode,"Items").getChildNodes();
					int Cnt = ItemsNode.getLength();
					for (int I = 0; I < Cnt; I++) {
						Node ItemNode = ItemsNode.item(I);
						//.
						if (ItemNode.getLocalName() != null) {
							TItem Item = new TItem();
							//.
							Item.idTComponent = Integer.parseInt(TMyXML.SearchNode(ItemNode,"idTComponent").getFirstChild().getNodeValue());
							Item.idComponent = Long.parseLong(TMyXML.SearchNode(ItemNode,"idComponent").getFirstChild().getNodeValue());
							//.
							if (Item.idTComponent == SpaceDefines.idTCoComponent) 
								Item.idCoType = Integer.parseInt(TMyXML.SearchNode(ItemNode,"idCoType").getFirstChild().getNodeValue());
							//.
							Item.Name = TMyXML.SearchNode(ItemNode,"Name").getFirstChild().getNodeValue();
							Item.Domains = TMyXML.SearchNode(ItemNode,"Domains").getFirstChild().getNodeValue();
							//.
							Node node = TMyXML.SearchNode(ItemNode,"Online");
							if (node != null)
								Item.flOnline = (Integer.parseInt(node.getFirstChild().getNodeValue()) != 0);
							//.
		    				Items.add(Item);
						}
					}
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing stream descriptor: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown stream descriptor version, version: "+Integer.toString(Version)); //. =>
			}
		}
	}
	
	private EditText	edContext;
	private Button 		btnSearch;
	private ListView 	lvObjects;
	private Button 		btnAddCheckedObjects;
	//.
	private TItems ResultItems;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.reflector_gmos_search_panel);
        //.
        edContext = (EditText)findViewById(R.id.edContext);
        edContext.setOnEditorActionListener(new OnEditorActionListener() {        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				btnSearch.callOnClick();
                if(arg1 == EditorInfo.IME_ACTION_DONE){
    				btnSearch.callOnClick();
                }
				return false;
			}
        });        
        //.
        btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	try {
					DoDomainSearch(edContext.getText().toString(),"V=1");
				} catch (Exception E) {
					Toast.makeText(TReflectorCoGeoMonitorObjectsSearchPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        lvObjects = (ListView)findViewById(R.id.lvObjects);
		lvObjects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvObjects.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	}              
        });         
        btnAddCheckedObjects = (Button)findViewById(R.id.btnAddCheckedObjects);
        btnAddCheckedObjects.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		AddCheckedObjects();
        		//.
            	setResult(RESULT_OK);
        		TReflectorCoGeoMonitorObjectsSearchPanel.this.finish();
            }
        });
        //.
        this.setResult(RESULT_CANCELED);
    }

    private void DoDomainSearch(final String Domains, final String Params) throws Exception {
		TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectsSearchPanel.this,getString(R.string.SWaitAMoment)) {
			
			private TItems ResultItems;
			
			@Override
			public void Process() throws Exception {
		    	TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    	byte[] Result = TReflectorCoGeoMonitorObjects.GetDataForDomains(UserAgent.Server, Domains, Params);
		    	//.
		    	ResultItems = new TItems(Result);
				//.
	    		Thread.sleep(100); 
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				TReflectorCoGeoMonitorObjectsSearchPanel.this.ResultItems = ResultItems;
				//.
				lvObjects_UpdateByResultItems();
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TReflectorCoGeoMonitorObjectsSearchPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
    
	private void lvObjects_UpdateByResultItems() {
		int Cnt = ResultItems.Items.size();
		String[] lvObjectsItems = new String[Cnt];
		for (int I = 0; I < Cnt; I++)
			lvObjectsItems[I] = ResultItems.Items.get(I).Text1();
		ArrayAdapter<String> lvObjectsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvObjectsItems);             
		lvObjects.setAdapter(lvObjectsAdapter);
		for (int I = 0; I < Cnt; I++)
			lvObjects.setItemChecked(I,false);
	}	
	
	private void AddCheckedObjects() {
		TReflector Reflector = TReflector.GetReflector();
		if (Reflector == null)
			return; //. ->
		int Cnt = ResultItems.Items.size();
		for (int I = 0; I < Cnt; I++)
			if (lvObjects.isItemChecked(I)) {
				TItem Item = ResultItems.Items.get(I);
				if (Item.IsValid()) 
					Reflector.CoGeoMonitorObjects.AddItem(Item.ID(),Item.Text(),true);
			}
	}
}