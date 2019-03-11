package crossingProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class runner implements Runnable{

	private int index;
	private Socket socket;
	InputStream in;
	OutputStream out;
	public runner(Socket socket,int index) throws IOException {
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
			short port=runner.bytesToShort(portarr);
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
