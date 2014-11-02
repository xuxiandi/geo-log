package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO;

import java.io.IOException;
import java.util.Random;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.Protocol;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.TModel;

public class PIO extends Protocol {

	public static byte[] ID() {
		return (new byte[] {0x30,0x31});
	}
	
	public static class TCommand {
		
		public static TCommand GetCommandByName(String Command, TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			if (TMODELCommand.CheckCommandName(Command))
				return new TMODELCommand(pPluginModule,pCommandSender,pResponseHandler); //. ->
			else
				if (TGPOCommand.CheckCommandName(Command))
					return new TGPOCommand(pPluginModule,pCommandSender,pResponseHandler); //. ->
				else
					if (TADCCommand.CheckCommandName(Command))
						return new TADCCommand(pPluginModule,pCommandSender,pResponseHandler); //. ->
					else
						if (TDACCommand.CheckCommandName(Command))
							return new TDACCommand(pPluginModule,pCommandSender,pResponseHandler); //. ->
						else
							return null; //. ->
		}
		
		public static final String CommandPrefix = "#";
		public static final String CommandResponsePrefix = "@";
		
		public static final int ProcessTimeout = 10000; //. ms
		
		public static boolean IsCommand(String Message) {
			return Message.startsWith(CommandPrefix);
		}
		
		public static boolean IsCommandResponse(String Message) {
			return Message.startsWith(CommandResponsePrefix);
		}
		
		public static class TCommandSender {
			
			public void SendCommand(String Command) throws IOException {
			}
		}
		
		public static class TCommandData {
			
			public static final int UnknownSession = 0;
			
			public static short 	NewCommandSessionRange = Short.MAX_VALUE-1;
			private static Random	NewCommandSessionRandom = new Random();
			
			public static short GetNewCommandSession() {
				synchronized (NewCommandSessionRandom) {
					return (short)NewCommandSessionRandom.nextInt(1+NewCommandSessionRange);
				}
			}
			
			public String 	Command = null;
			//.
			public String[]	Data = null;
			public int		Version = 0;
			public int		Session = UnknownSession;
			//.
			public TCommandResponseData Response = null;
			
			public TCommandData() {
			}

			public TCommandData(String pCommand, int pSession) {
				Command = pCommand;
				Session = pSession;
			}

			public String ConstructCommandMessage() {
				StringBuilder SB = new StringBuilder();
				SB.append(CommandPrefix);
				SB.append(Command);
				SB.append(" ");
				SB.append(Integer.toString(Version));
				SB.append(",");
				if (Data != null) {
					for (int I = 0; I < Data.length; I++) {
						SB.append(Data[I]);
						SB.append(",");
					}
				}
				SB.append(Integer.toString(Session));
				SB.append(",");
				SB.append("0"); //. CRC
				SB.append("!"); //. Terminator
				return SB.toString();			
			}
			
			public void Set(String pData) {
				Data = pData.split(",");
				//.
				Version = GetVersion();
				Session = GetSessionID();
				//.
				ResetResponse();
			}
			
			public int GetSessionID() {
				return Integer.decode(Data[Data.length-2]);
			}

			public int GetCRC() {
				return Integer.decode(Data[Data.length-1]);
			}

			public int GetVersion() {
				return Integer.decode(Data[0]);
			}

			public void SetResponse(TCommandResponseData pResponse) {
				Response = pResponse;
			}
			
			public void ResetResponse() {
				Response = null;
			}			
		}
		
		public static class TResponseHandler {
			
			public void DoOnResponse(TCommand Command) {
			}
		}
		
		@SuppressWarnings("serial")
		public static class ResponseException extends IOException {
			
			public int 		Code;
			public String 	Message;
			
			public ResponseException(int pCode, String pMessage) {
				Code = pCode;
				Message = pMessage;
			}
		}
		
		@SuppressWarnings("serial")
		public static class ResponseTimeoutException extends ResponseException {
			
			public ResponseTimeoutException() {
				super(Integer.MIN_VALUE,null);
			}
		}
		
		@SuppressWarnings("serial")
		public static class ResponseProcessException extends ResponseException {
			
			public ResponseProcessException(String pMessage) {
				super(Integer.MIN_VALUE,pMessage);
			}
		}

		public static class TCommandResponseData {
			
			public String 	Command = null;
			//.
			public String[]	Data = null;
			public int 		Version = 0;
			public int 		Session = 0;
			public int 		ExceptionCode = 0;
			public String	ExceptionMessage = null;

			public TCommandResponseData() {
			}
			
			public TCommandResponseData(String pResponse) {
				Set(pResponse);
			}
			
			public void Set(String pResponse) {
				String[] Response = pResponse.split(" ");
				//.
				Command = Response[0].substring(1);
				Data = Response[1].split(",");
				//.
				Version = GetVersion();
				Session = GetSessionID();
				if (Version < 0) {
					ExceptionCode 	= GetExceptionCode();
					ExceptionMessage = GetExceptionMessage();
				}
				else {
					ExceptionCode 	= 0;
					ExceptionMessage = null;
				}
			}
			
			public int GetSessionID() {
				return Integer.decode(Data[Data.length-2]);
			}

			public int GetCRC() {
				return Integer.decode(Data[Data.length-1]);
			}

			public int GetVersion() {
				return Integer.decode(Data[0]);
			}
			
			public int GetExceptionCode() {
				return Integer.decode(Data[1]);
			}
			
			public String GetExceptionMessage() {
				return Data[2];
			}
		}
		

		protected TPluginModule PluginModule;
		//.
		public TCommandSender CommandSender;
		//.
		public TResponseHandler ResponseHandler = null;
		//.
		public TCommandData CommandData;
		//.
		public Object 	ReceivedSignal = new Object();
		public boolean 	Received = false;
		
		public TCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, boolean flNewSession) {
			PluginModule = pPluginModule;
			//.
			CommandSender = pCommandSender;
			//.
			ResponseHandler = pResponseHandler;
			//.
			if (flNewSession)
				CommandData = new TCommandData(MyCommandName(),TCommandData.GetNewCommandSession());
			else
				CommandData = new TCommandData(MyCommandName(),TCommandData.UnknownSession);
		}

		public TCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			this(pPluginModule, pCommandSender,pResponseHandler, false);
		}
		
		
		public String MyCommandName() {
			return null;
		}
		
		public boolean CheckCommand(String pCommand) {
			return false;
		}
				
		public void Prepare() {
		}
				
		public void Process() throws Exception {
			try {
				Reset();
				//.
				Prepare();
				//.
				SendCommand(CommandData.ConstructCommandMessage());
				//. wait for completion and process response for result
				WaitAndProcessResponse();
			}
			catch (Exception E) {
				String EM = E.getMessage();
				if (EM == null)
					EM = E.getClass().getName();
				PluginModule.Device.Log.WriteError("PIOCodec","command process error: "+EM);
				//.
				throw E; //. =>
			}
		}
		
		protected void SendCommand(String Command) throws IOException {
			CommandSender.SendCommand(Command);
		}
		
		public void WaitAndProcessResponse() throws Exception {
			synchronized (ReceivedSignal) {
				ReceivedSignal.wait(ProcessTimeout);
				if (!Received)
					throw new ResponseTimeoutException(); //. =>
				//.
				ProcessResponse();
			}
		}
		
		public void Reset() {
			synchronized (ReceivedSignal) {
				CommandData.ResetResponse();
				Received = false;
			}
		}
		
		public boolean DoOnCommandResponse(TCommandResponseData Response) {
			if (CommandData.Session != TCommandData.UnknownSession)  
				if (Response.Session == CommandData.Session) {
					CommandData.SetResponse(Response);
					//.
					synchronized (ReceivedSignal) {
						Received = true;
						ReceivedSignal.notify();
					}
					//.
					return true; //. ->
				}
				else
					return false; //. ->
			else {
				CommandData.SetResponse(Response);
				//.
				return true; //. ->
			}				
		}
		
		public void CheckResponse() throws ResponseException {
			if (CommandData.Response.Version < 0)
				throw new ResponseException(CommandData.Response.ExceptionCode,CommandData.Response.ExceptionMessage); //. =>
		}

		public void ParseResponse() throws Exception {
		}

		public void ProcessResponse() throws Exception {
			CheckResponse(); 
			//.
			ParseResponse();
			//.
			DoOnResponseIsProcessed();
		}
		
		public void DoOnResponseIsProcessed() throws Exception {
			if (ResponseHandler != null) 
				ResponseHandler.DoOnResponse(this);
		}
	}
	
	public static class TMODELCommand extends TCommand {
		
		public static String CommandName() {
			return "MODEL";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public TModel Value;
		
		public TMODELCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress) {
			super(pPluginModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
		}
		
		public TMODELCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginModule, pCommandSender,pResponseHandler, true);
		}

		public TMODELCommand(TPluginModule pPluginModule) {
			super(pPluginModule, null,null, true);
		}

		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = null;
		}
		
		@Override
		public void ParseResponse() throws Exception {
			switch (CommandData.Response.Version) {
			
			case 1: 
				String _Base64Value = CommandData.Response.Data[1];
				if ((_Base64Value != null) && (_Base64Value.length() > 0))
					Value = new TModel(PluginModule,_Base64Value);
				else
					Value = null;
				break; //. >
				
			default:
				throw new ResponseProcessException("unknown response version, version: "+Integer.toString(CommandData.Response.Version)); //. =>
			}
		}
	}
	
	public static class TGPICommand extends TCommand {
		
		public static String CommandName() {
			return "GPI";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TGPICommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress) {
			super(pPluginModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
		}
		
		public TGPICommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginModule, pCommandSender,pResponseHandler);
		}

		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[1];
			CommandData.Data[0] = Integer.toString(Address);
		}
		
		@Override
		public void ParseResponse() throws Exception {
			switch (CommandData.Response.Version) {
			
			case 1: 
				int _Address = Integer.parseInt(CommandData.Response.Data[1]);
				if (Address == 0) 
					Address = _Address;
				else
					if (_Address != Address)
						throw new ResponseProcessException("wrong returned address, address: "+Integer.toString(_Address)); //. =>
				Value = Integer.parseInt(CommandData.Response.Data[2]);
				break; //. >
				
			default:
				throw new ResponseProcessException("unknown response version, version: "+Integer.toString(CommandData.Response.Version)); //. =>
			}
		}
	}
	
	public static class TGPOCommand extends TCommand {
		
		public static String CommandName() {
			return "GPO";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TGPOCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress, int pValue) {
			super(pPluginModule,pCommandSender,pResponseHandler,true);
			//.
			Address = pAddress;
			Value = pValue;
		}
		
		public TGPOCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginModule,pCommandSender,pResponseHandler);
		}
		
		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[2];
			CommandData.Data[0] = Integer.toString(Address);
			CommandData.Data[1] = Integer.toString(Value);
		}				
	}
	
	public static class TADCCommand extends TCommand {
		
		public static String CommandName() {
			return "ADC";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TADCCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress) {
			super(pPluginModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
		}
		
		public TADCCommand(TPluginModule pPluginModule, int pAddress) {
			this(pPluginModule ,null,null, pAddress);
		}
		
		public TADCCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginModule, pCommandSender,pResponseHandler);
		}
		
		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[1];
			CommandData.Data[0] = Integer.toString(Address);
		}
		
		@Override
		public void ParseResponse() throws Exception {
			switch (CommandData.Response.Version) {
			
			case 1: 
				int _Address = Integer.parseInt(CommandData.Response.Data[1]);
				if (Address == 0) 
					Address = _Address;
				else
					if (_Address != Address)
						throw new ResponseProcessException("wrong returned address, address: "+Integer.toString(_Address)); //. =>
				Value = Integer.parseInt(CommandData.Response.Data[2]);
				break; //. >
				
			default:
				throw new ResponseProcessException("unknown response version, version: "+Integer.toString(CommandData.Response.Version)); //. =>
			}
		}
	}
	
	public static class TDACCommand extends TCommand {
		
		public static String CommandName() {
			return "DAC";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TDACCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress, int pValue) {
			super(pPluginModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
			Value = pValue;
		}
		
		public TDACCommand(TPluginModule pPluginModule, int pAddress, int pValue) {
			this(pPluginModule, null,null, pAddress,pValue);
		}
		
		public TDACCommand(TPluginModule pPluginModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginModule, pCommandSender,pResponseHandler);
		}
		
		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[2];
			CommandData.Data[0] = Integer.toString(Address);
			CommandData.Data[1] = Integer.toString(Value);
		}				
	}	
}
