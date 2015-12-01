// simpletalk CLIENT with numbered messages, UDP version
// client sends message with newline stripped
/* data format: 
   +--------+-----------------------+
   | 16 bit |    string             |
   +--------+-----------------------+
 */

import java.net.*;      //pld
import java.io.*;
//import java.io.Externalizable;

public class nstalkc {

    static public BufferedReader bin;
    static public int destport = 5433;
    static public int bufsize = 512;
    static public short msgnum = 1;
    static public final int HSIZE = 2;

    //============================================================

    // usage: stalkc [desthost [port]]

    static public void main(String args[]) {
	String desthost = "localhost";
	if (args.length >= 1) desthost = args[0];
	if (args.length >= 2) {
		destport = (new Integer(args[1])).intValue();
	}

        bin = new BufferedReader(new InputStreamReader(System.in));

        InetAddress dest;
        System.err.print("Looking up address of " + desthost + "...");
        try {
            dest = InetAddress.getByName(desthost);	// DNS query
        }
        catch (UnknownHostException uhe) {
            System.err.println("unknown host: " + desthost);
            return;
        }
        System.err.println(" got it!");

        DatagramSocket s;
        try {
            s = new DatagramSocket();
        }
        catch(IOException ioe) {
            System.err.println("no socket available");
            return;
        }

	System.err.println("port=" + s.getLocalPort());

        DatagramPacket msg = new DatagramPacket(
                    new byte[0], 0, dest, destport);

        //============================================================

        while (true) {
            String buf;
            int slen;
            try { buf = bin.readLine();}
            catch (IOException ioe) {
                System.err.println("readLine() failed");
                return;
            }

            if (buf == null) break;	// user typed EOF character

            slen = buf.length();
            byte[] bbuf = new byte[slen+HSIZE]; // buf.getBytes();
		
	    for (int i=0; i<slen; i++) bbuf[i+HSIZE] = (byte) buf.charAt(i);
	    setmsgnum(bbuf, msgnum);
	    msgnum++;
            msg.setData(bbuf);
            msg.setLength(slen+HSIZE);

            try {
                s.send(msg);
				byte[] ack = new byte[128];
				ack = ("ACK[" + msgnum + "]:" + msg.getData()).getBytes();
				DatagramPacket dp = new DatagramPacket(ack, ack.length);
				s.receive(dp); //receive ACK packet from server
				String str = new String(dp.getData());
				System.err.println(str);
				
				str = null;
				ack = null;
				//msg = null;
            }
            catch (IOException ioe) {
                System.err.println("send() failed");
                return;
            }
        } // while
        s.close();
    }

    public static short getmsgnum(byte[] buf) throws IOException {
   	if (buf.length < HSIZE) throw new IOException("buffer too short");
	return (short) (((buf[0]) << 8) | ((buf[1]) & 0xff) );
    }

    static void setmsgnum(byte[] buf, short count) {
        buf[0] = (byte) (count >> 8);
        buf[1] = (byte) count;
    }
}