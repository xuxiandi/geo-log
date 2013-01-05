package com.geoscope.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TDataConverter {

    //. Big-Endian number's conversion
    public static byte[] ConvertInt16ToBEByteArray(short V) throws IOException
    {
    	byte[] Result = new byte[2];
    	Result[0] = (byte)(V & 0xff);
    	Result[1] = (byte)(V >>> 8 & 0xff);
    	return Result;
    }
    
    public static short ConvertBEByteArrayToInt16(byte[] V, int Idx) throws IOException
    {
		return (short)(((V[Idx+1] & 0xFF) << 8)+(V[Idx] & 0xFF));
    }
    
    public static byte[] ConvertInt32ToBEByteArray(int V) throws IOException
    {
    	byte[] Result = new byte[4];
    	Result[0] = (byte)(V & 0xff);
    	Result[1] = (byte)(V >> 8 & 0xff);
    	Result[2] = (byte)(V >> 16 & 0xff);
    	Result[3] = (byte)(V >>> 24);
        return Result;
    }
    
    public static int ConvertBEByteArrayToInt32(byte[] V, int Idx) throws IOException
    {
		return ((V[Idx+3] << 24)+((V[Idx+2] & 0xFF) << 16)+((V[Idx+1] & 0xFF) << 8)+(V[Idx] & 0xFF));
    }
    
    public static byte[] ConvertDoubleToBEByteArray(double V) throws IOException
    {
        byte[] R = new byte[8];
        ByteBuffer.wrap(R).putDouble(V);
        //.
        byte E;
        E = R[0]; R[0] = R[1]; R[1] = E;
        E = R[2]; R[2] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[5]; R[5] = E;
        E = R[6]; R[6] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[2]; R[2] = E; E = R[1]; R[1] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[6]; R[6] = E; E = R[5]; R[5] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[4]; R[4] = E; E = R[1]; R[1] = R[5]; R[5] = E; E = R[2]; R[2] = R[6]; R[6] = E; E = R[3]; R[3] = R[7]; R[7] = E;
        //.
        return R;
    }
    
    public static double ConvertBEByteArrayToDouble(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+7],V[Idx+6],V[Idx+5],V[Idx+4],V[Idx+3],V[Idx+2],V[Idx+1],V[Idx+0]};
        return ByteBuffer.wrap(BA).getDouble();
    }
    
    public static byte[] ConvertLongToBEByteArray(long V) throws IOException
    {
        byte[] R = new byte[8];
        ByteBuffer.wrap(R).putLong(V);
        //.
        byte E;
        E = R[0]; R[0] = R[1]; R[1] = E;
        E = R[2]; R[2] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[5]; R[5] = E;
        E = R[6]; R[6] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[2]; R[2] = E; E = R[1]; R[1] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[6]; R[6] = E; E = R[5]; R[5] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[4]; R[4] = E; E = R[1]; R[1] = R[5]; R[5] = E; E = R[2]; R[2] = R[6]; R[6] = E; E = R[3]; R[3] = R[7]; R[7] = E;
        //.
        return R;
    }
    
    public static long ConvertBEByteArrayToLong(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+7],V[Idx+6],V[Idx+5],V[Idx+4],V[Idx+3],V[Idx+2],V[Idx+1],V[Idx+0]};
        return ByteBuffer.wrap(BA).getLong();
    }
}
