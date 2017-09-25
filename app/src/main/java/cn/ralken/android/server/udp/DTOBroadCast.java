package cn.ralken.android.server.udp;

import java.io.Serializable;

public class DTOBroadCast implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public String ssid;
	public String ip;
	public int port;
}
