package crossingProxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class main {
	
	public static void main(String[] args) throws IOException{
		ServerSocket ss=new ServerSocket(1031);
		for(int i=0;;i++){
			Socket socket=ss.accept();
			connect(socket,i);
		}
	}
	
	public static void connect(Socket soc,int i) throws IOException{
		InputStream is=soc.getInputStream();
		int b=0;
		while(b!=-1){
			b=is.read();
			System.out.println(i+" "+b);
	//		System.out.print((char)b);
		}
	}
	
}
