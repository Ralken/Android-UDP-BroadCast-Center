package cn.ralken.android.server.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

import cn.ralken.android.server.udp.UDPBroadCast;

public class WifiUtil {

	/**
	 * Calculate the broadcast IP we need to send the packet along. If we send
	 * it to 255.255.255.255, it never gets sent. I guess this has something to
	 * do with the mobile network not wanting to do broadcast.
	 */
	public static InetAddress getBroadcastAddress(Context context) throws IOException {
		WifiManager wm = (WifiManager) context.getSystemService(Activity.WIFI_SERVICE);
		DhcpInfo dhcp = wm.getDhcpInfo();
		if (dhcp == null) {
			Log.d(UDPBroadCast.TAG, "Could not get dhcp info");
			return null;
		}

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

	public static String getIpAdrs(Context context) {
		WifiManager wm = (WifiManager) context.getSystemService(Activity.WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		return ip;
	}

	/**
	 * Returns the service set identifier (SSID) of the current 802.11 network.
	 * If the SSID is an ASCII string, it will be returned surrounded by double
	 * quotation marks.Otherwise, it is returned as a string of hex digits. The
	 * SSID may be null if there is no network currently connected.
	 * 
	 * @param context
	 * @return the SSID
	 */
	public static String readWifiConnectionName(Context context) {
		if (isWifiConnected(context)) {
			final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
				// if (connectionInfo != null &&
				// !StringUtil.isBlank(connectionInfo.getSSID())) {
				return connectionInfo.getSSID();
			}
		}
		return null;
	}

	private static boolean isWifiConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return networkInfo != null && networkInfo.isConnected();
	}
}
