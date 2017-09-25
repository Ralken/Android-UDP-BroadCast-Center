package cn.ralken.android.server;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.ralken.android.server.udp.TCPServer;
import cn.ralken.android.server.udp.TCPServer.OnClientRequestListener;
import cn.ralken.android.server.udp.UDPBroadCast;

public class ServerActivity extends Activity {

	private UDPBroadCast discovererThread;
	private TCPServer tcpServer;

	ListView listView;
	LogAdapter adapter;
	List<String> logs = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startUDPBroadCastThread();
				startTCPServerThread();
			}
		});

		findViewById(R.id.buttonStop).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopUdpBroadCast();
			}
		});

		listView = (ListView) findViewById(R.id.listView1);
		adapter = new LogAdapter();
		listView.setAdapter(adapter);
	}

	private void stopUdpBroadCast(){
		if (discovererThread != null) {
			discovererThread.stopBroadCasting();
			discovererThread = null;
		}
	}
	
	public void postLog(String log){
		android.os.Message msg = new Message();
		msg.obj = log;
		handler.sendMessage(msg);
	}
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			logs.add((String) msg.obj);
			adapter.notifyDataSetChanged();
		};
	};
	
	private void startUDPBroadCastThread() {
		if (discovererThread != null && discovererThread.ismBroadCastEnabled() && !discovererThread.isInterrupted()) {
			Toast.makeText(getApplicationContext(), "已开启", Toast.LENGTH_SHORT).show();
			return;
		}else {
			discovererThread = new UDPBroadCast(this);
			discovererThread.start();
		}
	}
	
	private void startTCPServerThread() {
		if(tcpServer != null && !tcpServer.isInterrupted()){
			return;
		}else {
			tcpServer = new TCPServer(this);
			tcpServer.setOnClientRequestListener(new OnClientRequestListener() {
				@Override
				public void onClientRequestAccepted(String dspAddress) {
					discovererThread.stopBroadCasting();
				}
			});
			tcpServer.start();
		}
	}

	class LogAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return logs.size();
		}

		@Override
		public Object getItem(int position) {
			return logs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item_log_list, null);
				holder = new Holder();
				holder.logItemText = (TextView) convertView.findViewById(R.id.logItemText);
				convertView.setTag(holder);
			}

			holder = (Holder) convertView.getTag();

			holder.logItemText.setText(logs.get(position));
			return convertView;
		}

		Holder holder = null;

		class Holder {
			TextView logItemText;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		stopUdpBroadCast();
	}

}
