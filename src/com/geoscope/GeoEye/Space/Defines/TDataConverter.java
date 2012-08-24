package com.geoscope.GeoEye.Space.Defines;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TDataConverter {

    //. Big-Endian number's conversion
    public static byte[] ConvertInt16ToBEByteArray(short V) throws IOException
    {
        byte E;
        //.
        ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(8);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(V);
        //.
        byte[] R = bos.toByteArray();
        //.
        E = R[0]; R[0] = R[1]; R[1] = E;
        //.
        return R;
    }
    
    public static short ConvertBEByteArrayToInt16(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+1],V[Idx+0]};
        //.
        ByteArrayInputStream bis = new ByteArrayInputStream(BA,0,2);
        DataInputStream dis = new DataInputStream(bis);
        return dis.readShort();
    }
    
    public static byte[] ConvertInt32ToBEByteArray(int V) throws IOException
    {
        byte E;
        //.
        ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(8);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(V);
        //.
        byte[] R = bos.toByteArray();
        //.
        E = R[0]; R[0] = R[1]; R[1] = E;
        E = R[2]; R[2] = R[3]; R[3] = E;
        //.
        E = R[0]; R[0] = R[2]; R[2] = E; E = R[1]; R[1] = R[3]; R[3] = E;
        //.
        return R;
    }
    
    public static int ConvertBEByteArrayToInt32(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+3],V[Idx+2],V[Idx+1],V[Idx+0]};
        //.
        ByteArrayInputStream bis = new ByteArrayInputStream(BA,0,4);
        DataInputStream dis = new DataInputStream(bis);
        return dis.readInt();
    }
    
    public static byte[] ConvertDoubleToBEByteArray(double V) throws IOException
    {
        byte E;
        //.
        ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(8);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeDouble(V);
        //.
        byte[] R = bos.toByteArray();
        //.
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
        //.
        ByteArrayInputStream bis = new ByteArrayInputStream(BA,0,8);
        DataInputStream dis = new DataInputStream(bis);
        return dis.readDouble();
    }
    
}
