package com.geoscope.Classes.Data.Containers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TDataConverter {

    //. Big-Endian number's conversion
    public static byte[] ConvertInt16ToLEByteArray(short V) throws IOException
    {
    	byte[] Result = new byte[2];
    	Result[0] = (byte)(V & 0xff);
    	Result[1] = (byte)(V >>> 8 & 0xff);
    	return Result;
    }
    
    public static void ConvertInt16ToLEByteArray(short V, byte[] R, int Idx) throws IOException
    {
    	R[Idx+0] = (byte)(V & 0xff);
    	R[Idx+1] = (byte)(V >>> 8 & 0xff);
    }
    
    public static short ConvertLEByteArrayToInt16(byte[] V, int Idx) throws IOException
    {
		return (short)(((V[Idx+1] & 0xFF) << 8)+(V[Idx] & 0xFF));
    }
    
    public static void ConvertInt32ToLEByteArray(int V, byte[] R) throws IOException
    {
    	R[0] = (byte)(V & 0xff);
    	R[1] = (byte)(V >> 8 & 0xff);
    	R[2] = (byte)(V >> 16 & 0xff);
    	R[3] = (byte)(V >>> 24);
    }
    
    public static byte[] ConvertInt32ToLEByteArray(int V) throws IOException
    {
    	byte[] Result = new byte[4];
    	Result[0] = (byte)(V & 0xff);
    	Result[1] = (byte)(V >> 8 & 0xff);
    	Result[2] = (byte)(V >> 16 & 0xff);
    	Result[3] = (byte)(V >>> 24);
        return Result;
    }
    
    public static int ConvertLEByteArrayToInt32(byte[] V, int Idx) throws IOException
    {
		return ((V[Idx+3] << 24)+((V[Idx+2] & 0xFF) << 16)+((V[Idx+1] & 0xFF) << 8)+(V[Idx] & 0xFF));
    }
    
    public static void ConvertInt64ToLEByteArray(long V, byte[] R) throws IOException
    {
    	ByteBuffer BB = ByteBuffer.wrap(R);
    	BB.order(ByteOrder.LITTLE_ENDIAN);
    	BB.putLong(V);
    }
    
    public static byte[] ConvertInt64ToLEByteArray(long V) throws IOException
    {
    	byte[] R = new byte[8];
    	ConvertInt64ToLEByteArray(V, R);
    	return R;
    }
    
    public static long ConvertLEByteArrayToInt64(byte[] V, int Idx) throws IOException
    {
    	ByteBuffer BB = ByteBuffer.wrap(V, Idx,8);
    	BB.order(ByteOrder.LITTLE_ENDIAN);
    	return BB.getLong();
    }
    
    public static void ConvertDoubleToLEByteArray(double V, byte[] R) throws IOException
    {
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
    }
    
    public static byte[] ConvertDoubleToLEByteArray(double V) throws IOException
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
    
    public static double ConvertLEByteArrayToDouble(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+7],V[Idx+6],V[Idx+5],V[Idx+4],V[Idx+3],V[Idx+2],V[Idx+1],V[Idx+0]};
        return ByteBuffer.wrap(BA).getDouble();
    }
    
    public static byte[] ConvertLongToLEByteArray(long V) throws IOException
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
    
    public static long ConvertLEByteArrayToLong(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+7],V[Idx+6],V[Idx+5],V[Idx+4],V[Idx+3],V[Idx+2],V[Idx+1],V[Idx+0]};
        return ByteBuffer.wrap(BA).getLong();
    }
}
