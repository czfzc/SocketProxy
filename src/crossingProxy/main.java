package crossingProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class main {
	
	final static int PORT=1031;
	static int sum=0;
	
	public static void main(String[] args) throws IOException{
		
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5));
		ServerSocket ss=new ServerSocket(PORT);
		while(true) {
			Socket socket=ss.accept();
            Runner runner=new Runner(socket,sum++);
            executor.execute(runner);
            System.out.println("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+
            executor.getQueue().size()+"，已执行完别的任务数目："+executor.getCompletedTaskCount());
		}
	}
	
}
