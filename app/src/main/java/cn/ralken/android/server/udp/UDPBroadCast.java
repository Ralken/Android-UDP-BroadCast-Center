package cn.ralken.android.server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.Context;
import android.os.SystemClock;

import com.google.gson.Gson;
import cn.ralken.android.server.ServerActivity;
import cn.ralken.android.server.util.WifiUtil;

public class UDPBroadCast extends Thread {
	private static final long BROADCAST_ALIVE_DURATION = 5 * 60 * 1000;
	
	public static final String TAG = "miya_discovery";
	private static final int DISCOVERY_PORT = 25621;
	private static final int TIMEOUT_MS = 5000;

	private ServerActivity context;
	private boolean mBroadCastEnabled = true;

	private long mAliveTimer;
	private int times = 1;
	
	public UDPBroadCast(Context context) {
		this.context = (ServerActivity) context;
	}

	public void run() {
		try {
			mAliveTimer = System.currentTimeMillis();
			
			DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
			socket.setBroadcast(true);
			socket.setSoTimeout(TIMEOUT_MS);
			
			boolean isTimeOut = false;
			while (!Thread.currentThread().isInterrupted() && mBroadCastEnabled && !isTimeOut) {
				sendDiscoveryRequest(socket);
				SystemClock.sleep(2000);
				isTimeOut = System.currentTimeMillis() - mAliveTimer > BROADCAST_ALIVE_DURATION;
			}
			
			socket.close();
			context.postLog("UDP broadcast has been stopped!");
			times = 1;
			
		} catch (IOException e) {
			context.postLog("UDP Could not send discovery request: " + e.getMessage());
		}
	}

	public void stopBroadCasting(){
		mBroadCastEnabled = false;
		interrupt();
	}
	
	public boolean ismBroadCastEnabled() {
		return mBroadCastEnabled;
	}
	
	/**
	 * Send a broadcast UDP packet containing a request for boxee services to
	 * announce themselves.
	 * 
	 * @throws IOException
	 */
	private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
		
		DTOBroadCast broadCast = new DTOBroadCast();
		broadCast.ip = WifiUtil.getIpAdrs(context);
		broadCast.port = 6000;
		broadCast.ssid = WifiUtil.readWifiConnectionName(context);

		String data = new Gson().toJson(broadCast);
		
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), WifiUtil.getBroadcastAddress(context), DISCOVERY_PORT);
		socket.send(packet);

		InetAddress inetAddress = WifiUtil.getBroadcastAddress(context);
		String hostAddress = "";
		if (inetAddress != null) {
			hostAddress = " to " + inetAddress.getHostAddress() + ":" + DISCOVERY_PORT;
		}

		context.postLog("==>Sending UDP data " + data + " with length " + data.length() + hostAddress + " -- " +times);
		times ++;
	}

	public static String byteArrayToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(String.format("%02X", bytes[i]));
		}
		return sb.toString();
	}
	
}
