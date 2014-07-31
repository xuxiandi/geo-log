package com.geoscope.GeoEye.UserAgentService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoLog.Application.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.Application.Network.TServerConnection;

public class TUserAgent {

	private static TUserAgent UserAgent = null;
	
    public static synchronized TUserAgent CreateUserAgent(Context context) throws Exception {
    	if (UserAgent == null)
    		UserAgent = new TUserAgent(context);
    	return UserAgent;
	}    
    
    public static synchronized void FreeUserAgent() {
    	if (UserAgent != null)
    	{
    		UserAgent.Destroy();
    		UserAgent = null;
    	}
    }
    
    public static synchronized TUserAgent GetUserAgent() {
    	return UserAgent;
    }
    
    public static synchronized void RestartUserAgent(Context context) throws Exception {
    	FreeUserAgent();
    	CreateUserAgent(context);
    }
    
    public static class TConfiguration {
    	public String 	ServerAddress = "127.0.0.1";
    	public int		ServerPort = 80;
    	//.
    	public int 		UserID = 2;
    	public String 	UserPassword = "ra3tkq";
    	public boolean 	flUserSession = true;
    	public boolean 	flSecureConnections = false;
    }
    
    private TConfiguration GetConfigurationFromReflector() throws Exception {
    	TConfiguration Result = new TConfiguration();
		String FN = TReflector.ProfileFolder()+"/"+TReflector.TReflectorConfiguration.ConfigurationFileName;
		File F = new File(FN);
		if (!F.exists()) 
			return Result; //. ->
		// .
		byte[] XML;
		long FileSize = F.length();
		if (FileSize == 0)
			return Result; // . ->
		FileInputStream FIS = new FileInputStream(FN);
		try {
			XML = new byte[(int)FileSize];
			FIS.read(XML);
		} finally {
			FIS.close();
		}
		Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			XmlDoc = builder.parse(BIS);
		} finally {
			BIS.close();
		}
		int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
		NodeList NL;
		switch (Version) {
		
		case 1:
			NL = XmlDoc.getDocumentElement().getElementsByTagName("ServerAddress");
			Result.ServerAddress = NL.item(0).getFirstChild().getNodeValue();
			// .
			NL = XmlDoc.getDocumentElement().getElementsByTagName("ServerPort");
			Result.ServerPort = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
			// .
			NL = XmlDoc.getDocumentElement().getElementsByTagName("UserID");
			Result.UserID = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
			// .
			NL = XmlDoc.getDocumentElement().getElementsByTagName("UserPassword");
			Result.UserPassword = NL.item(0).getFirstChild().getNodeValue();
			//.
			try {
				NL = XmlDoc.getDocumentElement().getElementsByTagName("flUserSession");
				Result.flUserSession = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
			}
			catch (Exception E) {}
			//.
			try {
				NL = XmlDoc.getDocumentElement().getElementsByTagName("flSecureConnections");
				Result.flSecureConnections = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
			}
			catch (Exception E) {}
			//.
			return Result; //. ->
			
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); // . =>
		}
    }
    
    public Context context;
    //.
	public TGeoScopeServer 		Server;
	
	@SuppressLint("NewApi")
	private TUserAgent(Context pcontext) throws Exception {
		TFileSystem.TExternalStorage.WaitForMounted();
		//.
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); 		
		}
		//.
		context = pcontext;
        //.
        TGeoLogInstallator.CheckInstallation(context);
		//.
		TConfiguration Configuration = GetConfigurationFromReflector();
		//.
		TServerConnection.flSecureConnection = Configuration.flSecureConnections;
		//.
        Server = new TGeoScopeServer(context, Configuration.ServerAddress,Configuration.ServerPort); 
        Server.InitializeUser(Configuration.UserID,Configuration.UserPassword,Configuration.flUserSession); 
	}
	
	private void Destroy() {
		if (Server != null) {
			try {
				Server.Destroy();
			}
			catch (Exception E) {
                Toast.makeText(context, "Error of freeing user-agent, "+E.getMessage(), Toast.LENGTH_LONG).show();
			}
			Server = null;
		}
	}
	
	public synchronized void Check() throws Exception {
	}

	public TGeoScopeServerUser User() {
		return Server.User;
	}
}
