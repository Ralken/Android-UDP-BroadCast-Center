package cn.ralken.android.server.udp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import android.util.Log;

import com.google.gson.Gson;
import cn.ralken.android.server.ServerActivity;
import cn.ralken.android.server.util.ConfigType;
import cn.ralken.android.server.util.DisplayResponse;

public class TCPServer extends Thread {
	private int REQUEST_TYPE_TIME = 0;
	private int REQUEST_TYPE_CONFIG = 1;
	
	private ServerSocket serverSocket;
	public static final int TCP_SERVERPORT = 6000;
	
	private Gson gson = new Gson();
	private ServerActivity activity;
	
	public interface OnClientRequestListener{
		void onClientRequestAccepted(String dspAddress);
	}
	
	private OnClientRequestListener onClientRequestListener;
	
	public TCPServer(ServerActivity activity){
		this.activity = activity;
	}
	
	@Override
	public void run() {
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(TCP_SERVERPORT);
		} catch (IOException e) {
			e.printStackTrace();
			activity.postLog("Server socket exception: " + e.getMessage());
		}

		Log.e("miya_discovery", "serverSocket = " + serverSocket);
		while (!Thread.currentThread().isInterrupted() && serverSocket != null) {
			try {
				activity.postLog("Waiting for TCP connection...");
				socket = serverSocket.accept();
				activity.postLog("Server socket accepted!");

	            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
	            String requestJson = (String) ois.readObject();
	            activity.postLog("Recieved request json from client: " + requestJson);
				DTOClientRequest clientRequest = gson.fromJson(requestJson, DTOClientRequest.class);
				activity.postLog("Recieved request code from client: " + clientRequest.requestCode);
				
				if(clientRequest.requestCode == REQUEST_TYPE_CONFIG){
					sendDisplayConfig(socket);
				}else if (clientRequest.requestCode == REQUEST_TYPE_TIME) {
					sendSystemTime(socket);
				}
				
				ois.close();
				
				if(null != onClientRequestListener){
					InetAddress inetAddress = socket.getInetAddress();
					String clientAddress = inetAddress != null? inetAddress.getHostAddress(): null;
					onClientRequestListener.onClientRequestAccepted(clientAddress);	//通知前端停止UDP广播
				}
				socket.close();	//关闭TCP连接
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		try {
			if(serverSocket != null){
				serverSocket.close();
			}
			activity.postLog("TCP Server closed!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendDisplayConfig(Socket socket) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			DTOServerResponse response = new DTOServerResponse();

			response.displayConfigBase16 = "ADGLKJADLGJADLKJ";
			String resp = gson.toJson(response);
			
			DisplayResponse resps = new DisplayResponse(15, DisplayResponse.Command.CONFIG);

			HashMap<String, String> configMap = new HashMap<String, String>();
		    resps.putShort((short)4);
		    resps.putShort(getShortIntFromConfig(ConfigType.INDOOR_FREQ, configMap));
		    resps.putShort(getShortIntFromConfig(ConfigType.CITY, configMap));
		    resps.putByte((byte) 7); //同步时间频率，天
		    resps.putInt(15);
		    resps.putByte((byte) 3);

		    String base16Result = resps.encodeToString();
			
			activity.postLog("Sending Display Config response to client: " + resp);
			activity.postLog("Sending Display Config base16Result response to client: " + base16Result);
			
			//oos.writeObject(resp);
			oos.writeObject(base16Result);
			oos.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private short getShortIntFromConfig(ConfigType outdoorFreq, HashMap<String, String> configMap) {
		return 0;
	}

	private void sendSystemTime(Socket socket) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

			DTOServerResponse response = new DTOServerResponse();
			response.timeStamp = System.currentTimeMillis();
			String resp = gson.toJson(response);
			
			DisplayResponse resps = new DisplayResponse(4, DisplayResponse.Command.SET_TIME);
			resps.putInt((int)(System.currentTimeMillis()/1000));
		    String base16Result = resps.encodeToString();
		    
		    activity.postLog("Sending System Time response to client: " + resp);
			activity.postLog("Sending System Time base16Result response to client: " + base16Result);
			
			oos.writeObject(base16Result);
			oos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setOnClientRequestListener(OnClientRequestListener onClientRequestListener) {
		this.onClientRequestListener = onClientRequestListener;
	}
	
}
