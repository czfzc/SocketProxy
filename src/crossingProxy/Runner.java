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
	 * 向源主机发送此次请求的状态 是否成功等
	 * 一定要在源主机向服务器发送完域名和端口信息的下一条发送
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
	 * 转发数据
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	
	public void transmit(String host,int port) {
		try {
			Socket socket=new Socket(host,port);
			
			OutputStream out=socket.getOutputStream();
			InputStream in=socket.getInputStream();
			
			byte[] request=new byte[this.in.available()];
			this.in.read(request);                                       //接收源主机请求
			
			System.out.println("\n\nrequest:\n"+new String(request,"UTF-8"));
			
			out.write(request);											//向目标主机发送请求
			
			socket.shutdownOutput();
			
			System.out.println("冲刷完成关闭");
			
		//	byte[] response=new byte[in.available()];					
		//	in.read(response);											//接收目标主机的回应
			
		//	System.out.println("\n\nresponse:\n"+new String(response,"UTF-8"));
			
	//		this.out.write(response);									//向源主机转发目标主机的回应
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
	 * 关闭与源主机的连接
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
	 * 获取认证方法并通过认证 默认为无身份认证
	 * 接受的三个参数分别是
	 * ver:socket版本(5)--1字节
	 * nmethods:在下一个参数的方法数 --1字节
	 * methods:方法 --1至255字节
	 *  X’00’ NO AUTHENTICATION REQUIRED				无身份验证
		X’01’ GSSAPI									未知
		X’02’ USERNAME/PASSWORD							用户名/密码
		X’03’ to X’7F’ IANA ASSIGNED					保留位
		X’80’ to X’FE’ RESERVED FOR PRIVATE METHODS		私有位
		X’FF’ NO ACCEPTABLE METHODS                 	没有可用方法
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
	 * 获取域名
	 * 域名前一位为域名字符串的长度 每个字符为一个字节
	 * 长度前四位分别是 
	 * ver:socket版本(5) 
	 * cmd:sock命令码(1 tcp,2 bind,3 udp) 
	 * rsv:保留字段
	 * atyp:地址类型(ipv4 1,域名 3,ipv6 4)
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
	 * 获取端口号
	 * 一般在域名或地址之后的两个字符 所以用short类型返回
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
	 * 工具方法 将两个byte转成一个short数字
	 * @param b
	 * @return
	 */
	
	private static short bytesToShort(byte[] b) {
		short z=(short)(((b[0]&0x00FF)<<8)|(0x00FF& b[1]));
		return z;
	}

}
