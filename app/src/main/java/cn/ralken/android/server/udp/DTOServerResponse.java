package cn.ralken.android.server.udp;

import java.io.Serializable;

public class DTOServerResponse implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public long timeStamp;
	public String displayConfigBase16;
}
