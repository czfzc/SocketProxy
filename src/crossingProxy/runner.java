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
			short port=runner.bytesToShort(portarr);
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
