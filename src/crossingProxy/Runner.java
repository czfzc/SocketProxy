package crossingProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Runner implements Runnable{

	private int index;
	private Socket socket;
	InputStream in;
	OutputStream out;
	public Runner(Socket socket,int index) throws IOException {
		this.index=index;
		this.socket=socket;
		in=socket.getInputStream();
		out=socket.getOutputStream();
	}
	@Override
	public void run() {
		System.out.println("running:"+this.index);
		this.acceptAuth();
		String domain=this.getDomain();
		short port=this.getPort();
		
		System.out.println("address: "+domain+":"+port);
		
		this.responseStatus();
		
		this.transmit(domain, port);
		this.closeConnection();
		
	}
	
	/**
	 * ��Դ�������ʹ˴������״̬ �Ƿ�ɹ���
	 * һ��Ҫ��Դ����������������������Ͷ˿���Ϣ����һ������
	 */
	
	public void responseStatus() {
		byte[] status= {0x05,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00};
		try {
			this.out.write(status);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ת������
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	
	public void transmit(String host,int port) {
		try {
			Socket socket=new Socket(host,port);
			
			OutputStream out=socket.getOutputStream();
			InputStream in=socket.getInputStream();
			
			byte[] request=new byte[this.in.available()];
			this.in.read(request);                                       //����Դ��������
			
			System.out.println("\n\nrequest:\n"+new String(request,"UTF-8"));
			
			out.write(request);											//��Ŀ��������������
			
			socket.shutdownOutput();
			
			System.out.println("��ˢ��ɹر�");
			
		//	byte[] response=new byte[in.available()];					
		//	in.read(response);											//����Ŀ�������Ļ�Ӧ
			
		//	System.out.println("\n\nresponse:\n"+new String(response,"UTF-8"));
			
	//		this.out.write(response);									//��Դ����ת��Ŀ�������Ļ�Ӧ
			byte b=0;
			while((b=(byte)in.read())!=-1){
				System.out.print((char)b);
				this.out.write(b);
			}
			
			this.out.flush();
			
			this.socket.shutdownOutput();
			
			in.close();
			out.close();
			socket.close();  
			System.out.println("---------------------------closed");

		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * �ر���Դ����������
	 */
	
	public void closeConnection() {
		try {
	//		this.socket.shutdownOutput();
			this.out.close();
			this.in.close();
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ��֤������ͨ����֤ Ĭ��Ϊ�������֤
	 * ���ܵ����������ֱ���
	 * ver:socket�汾(5)--1�ֽ�
	 * nmethods:����һ�������ķ����� --1�ֽ�
	 * methods:���� --1��255�ֽ�
	 *  X��00�� NO AUTHENTICATION REQUIRED				�������֤
		X��01�� GSSAPI									δ֪
		X��02�� USERNAME/PASSWORD							�û���/����
		X��03�� to X��7F�� IANA ASSIGNED					����λ
		X��80�� to X��FE�� RESERVED FOR PRIVATE METHODS		˽��λ
		X��FF�� NO ACCEPTABLE METHODS                 	û�п��÷���
	 */
	
	private void acceptAuth(){
		try {
			byte[] b=new byte[3];
			in.read(b);
			if(b[0]==5&&b[1]==1&&b[2]==0) {
				out.write(new byte[] {5,0});
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ����
	 * ����ǰһλΪ�����ַ����ĳ��� ÿ���ַ�Ϊһ���ֽ�
	 * ����ǰ��λ�ֱ��� 
	 * ver:socket�汾(5) 
	 * cmd:sock������(1 tcp,2 bind,3 udp) 
	 * rsv:�����ֶ�
	 * atyp:��ַ����(ipv4 1,���� 3,ipv6 4)
	 * @return
	 */
	
	private String getDomain(){
		try {
			byte[] c=new byte[4];
			in.read(c);
			int domainLen=in.read();
			byte[] domainarr=new byte[domainLen];
			in.read(domainarr);
			String domain=new String(domainarr);
			return domain;
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * ��ȡ�˿ں�
	 * һ�����������ַ֮��������ַ� ������short���ͷ���
	 * @return
	 */
	
	private short getPort(){  
		try {
			byte[] portarr=new byte[2];
			in.read(portarr);
			short port=Runner.bytesToShort(portarr);
			return port;
		}catch(IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * ���߷��� ������byteת��һ��short����
	 * @param b
	 * @return
	 */
	
	private static short bytesToShort(byte[] b) {
		short z=(short)(((b[0]&0x00FF)<<8)|(0x00FF& b[1]));
		return z;
	}

}
