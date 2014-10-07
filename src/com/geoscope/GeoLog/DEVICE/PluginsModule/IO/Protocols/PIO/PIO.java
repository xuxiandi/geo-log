package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO;

import java.io.IOException;
import java.util.Random;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.Protocol;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.TModel;

public class PIO extends Protocol {

	public static short ID() {
		return 1;
	}
	
	public static class TCommand {
		
		public static final String CommandPrefix = "#";
		public static final String CommandResponsePrefix = "@";
		
		public static final int ProcessTimeout = 10000; //. ms
		
		public static boolean IsCommand(String Message) {
			return Message.startsWith(CommandPrefix);
		}
		
		public static boolean IsCommandResponse(String Message) {
			return Message.startsWith(CommandResponsePrefix);
		}
		
		public static TCommand GetCommandByName(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, String Command) {
			if (TMODELCommand.CheckCommandName(Command))
				return new TMODELCommand(pPluginsModule,pCommandSender,pResponseHandler); //. ->
			else
				if (TGPOCommand.CheckCommandName(Command))
					return new TGPOCommand(pPluginsModule,pCommandSender,pResponseHandler); //. ->
				else
					if (TADCCommand.CheckCommandName(Command))
						return new TADCCommand(pPluginsModule,pCommandSender,pResponseHandler); //. ->
					else
						if (TDACCommand.CheckCommandName(Command))
							return new TDACCommand(pPluginsModule,pCommandSender,pResponseHandler); //. ->
						else
							return null; //. ->
		}
		
		public static class TCommandSender {
			
			public void SendCommand(String Command) throws IOException {
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
		
		public static class TCommandData {
			
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
			public int		Session = 0;
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
		
		protected TPluginsModule PluginsModule;
		//.
		public TCommandSender CommandSender;
		//.
		public TResponseHandler ResponseHandler = null;
		//.
		public TCommandData CommandData;
		//.
		public Object 	ReceivedSignal = new Object();
		public boolean 	Received = false;
		
		public TCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, boolean flNewSession) {
			PluginsModule = pPluginsModule;
			//.
			CommandSender = pCommandSender;
			//.
			ResponseHandler = pResponseHandler;
			//.
			if (flNewSession)
				CommandData = new TCommandData(MyCommandName(),TCommandData.GetNewCommandSession());
			else
				CommandData = new TCommandData(MyCommandName(),0);
		}

		public TCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			this(pPluginsModule, pCommandSender,pResponseHandler, false);
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
				PluginsModule.Device.Log.WriteError("PIOCodec","command process error: "+EM);
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
			if (CommandData.Session != 0)  
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
		
		public TMODELCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress) {
			super(pPluginsModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
		}
		
		public TMODELCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginsModule, pCommandSender,pResponseHandler);
		}

		public TMODELCommand(TPluginsModule pPluginsModule) {
			super(pPluginsModule, null,null);
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
				String _Value = CommandData.Response.Data[1];
				if ((_Value != null) && (_Value.length() > 0))
					Value = new TModel(PluginsModule,_Value);
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
		
		public TGPICommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress) {
			super(pPluginsModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
		}
		
		public TGPICommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginsModule, pCommandSender,pResponseHandler);
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
		
		public TGPOCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress, int pValue) {
			super(pPluginsModule,pCommandSender,pResponseHandler,true);
			//.
			Address = pAddress;
			Value = pValue;
		}
		
		public TGPOCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginsModule,pCommandSender,pResponseHandler);
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
		
		public TADCCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress) {
			super(pPluginsModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
		}
		
		public TADCCommand(TPluginsModule pPluginsModule, int pAddress) {
			this(pPluginsModule ,null,null, pAddress);
		}
		
		public TADCCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginsModule, pCommandSender,pResponseHandler);
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
		
		public TDACCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler, int pAddress, int pValue) {
			super(pPluginsModule, pCommandSender,pResponseHandler, true);
			//.
			Address = pAddress;
			Value = pValue;
		}
		
		public TDACCommand(TPluginsModule pPluginsModule, int pAddress, int pValue) {
			this(pPluginsModule, null,null, pAddress,pValue);
		}
		
		public TDACCommand(TPluginsModule pPluginsModule, TCommandSender pCommandSender, TResponseHandler pResponseHandler) {
			super(pPluginsModule, pCommandSender,pResponseHandler);
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
