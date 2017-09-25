package cn.ralken.android.server.udp;

import java.io.Serializable;

public class DTOClientRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public int requestCode; // 0-time 1-config
}
