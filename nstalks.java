/* simpletalk server with numbered messages, UDP version */
/* data format: 
   +--------+-----------------------+
   | 16 bit |    string             |
   +--------+-----------------------+

/* newline is to be included at SERVER side */

import java.lang.*;
import java.net.*;
import java.io.*;

public class nstalks {

    static public int destport = 5433;
    static public int bufsize = 512;
    static public final int timeout = 15000;
    static public final int HSIZE = 2;

    static public void main(String args[]) {
        DatagramSocket s;
	InetAddress dest;

 	if (args.length >= 1) {
		destport = (new Integer(args[0])).intValue();
	}

        try {
                //dest = InetAddress.getByName("10.0.0.5");	// DNS query
                s = new DatagramSocket(destport); //, dest);
        }
        catch (SocketException se) {
	        System.err.println("cannot create socket with port " + destport);
                return;
        } /* catch (UnknownHostException uhe) {
		return;
	} */
        try {
                s.setSoTimeout(timeout);       // set timeout in milliseconds
        } catch (SocketException se) {
                System.err.println("socket exception: timeout not set!");
        }

        DatagramPacket msg = new DatagramPacket(new byte[bufsize], bufsize);

	short msgnum = 0;

        while(true) { // read loop
            try {
					msg.setLength(bufsize);
					s.receive(msg); //Receives message
					String str = new String(msg.getData(), HSIZE, msg.getLength()-HSIZE);
					msgnum = getmsgnum(msg.getData());
					System.err.println("message from <" +
							msg.getAddress().getHostAddress() +
							"," + msg.getPort() + ">");
					byte[] ack = new byte[msg.getData().length];
					ack = ("ACK[" + msgnum + "]").getBytes();					
					s.send(new DatagramPacket(ack,ack.length,msg.getAddress(),msg.getPort())); //Sends ACK
					System.err.println("Acknowledgement sent\n");
           } catch (SocketTimeoutException ste) {
                    System.err.println("Response timed out!");
                    continue;
            } catch (IOException ioe) {           // should never happen!
                    System.err.println("Bad receive");
                    break;
            }
        } // end of read loop
        s.close();
    } // end of main

    
    public static short getmsgnum(byte[] buf) throws IOException {
   	if (buf.length < HSIZE) throw new IOException("buffer too short");
	return (short) (((buf[0]) << 8) | ((buf[1]) & 0xff) );
    }

    static void setmsgnum(byte[] buf, short count) {
        buf[0] = (byte) (count >> 8);
        buf[1] = (byte) count;
    }
}