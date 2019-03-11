package crossingProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class main {
	
	
	public static void main(String[] args) throws IOException{
		ServerSocket ss=new ServerSocket(1031);
//		for(int i=0;;i++){
			Socket socket=ss.accept();
			connect(socket);
//		}
	}
	
	public static void connect(Socket soc) throws IOException{
		InputStream is=soc.getInputStream();
		OutputStream os=soc.getOutputStream();
		byte[] b=new byte[3];
		is.read(b);
		if(b[0]==5&&b[1]==1&&b[2]==0) {
			os.write(new byte[] {5,0});
		}
		byte[] c=new byte[4];
		is.read(c);
		
		int domainLen=is.read();
		byte[] domainarr=new byte[domainLen];
		is.read(domainarr);
		String domain=new String(domainarr);
		System.out.println(domain);
		
		byte[] portarr=new byte[2];
		is.read(portarr);
		short port=main.bytesToShort(portarr);
		System.out.println(port);
	//	if(c[0]==5&&c[1]==1&&c[2]==0&&c[3]==3) {
	//		os.write(new byte[] {5,0});
			main.printBytes(is);
	//	}
		
	}
	
	public static short bytesToShort(byte[] b) {
		short z=(short)(((b[0]&0x00FF)<<8)|(0x00FF& b[1]));
		return z;
	}
	
	public static void printBytes(InputStream is) throws IOException {
		byte b=0;
		while((b=(byte)is.read())!=-1) {
			System.out.print((char)b);
		}
	}
	
}
