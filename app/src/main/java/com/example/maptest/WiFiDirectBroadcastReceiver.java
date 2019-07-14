package com.example.maptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    MainActivity activity;

    private List<WifiP2pDevice> peers = new ArrayList<>();


    public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(activity, "Wifi on", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Wifi off", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(activity, "Peers Found", Toast.LENGTH_SHORT).show();
            if (wifiP2pManager != null) {

                wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peerList) {

                        List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());
                        if (!refreshedPeers.equals(peers)) {
                            peers.clear();
                            peers.addAll(refreshedPeers);

                            // If an AdapterView is backed by this data, notify it
                            // of the change. For instance, if you have a ListView of
                            // available peers, trigger an update.
                            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                            // Perform any other updates needed based on the new list of
                            // peers connected to the Wi-Fi P2P network.
                            WifiP2pConfig config = new WifiP2pConfig();

                            config.deviceAddress = refreshedPeers.get(0).deviceAddress;
                            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

                                @Override
                                public void onSuccess() {
                                    try {
                                        Socket socket = new Socket("kq6py",8888);
                                        PrintWriter out = new PrintWriter(socket.getOutputStream(),
                                                true);
                                        out.println("You've Been Found.");

                                    }catch (IOException e) {
                                        System.out.println("No I/O");
                                        System.exit(1);
                                    }
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Toast.makeText(activity, "Not Connected", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        if (peers.size() == 0) {
//                            Log.d("notif", "No devices found");
//                            return;
                            Context context = activity.getApplicationContext();
                            FileServerAsyncTask listen = new FileServerAsyncTask(context,activity.findViewById(R.id.text));
                            Toast.makeText(activity, (String)listen.doInBackground(null), Toast.LENGTH_SHORT).show();

                        }
//                        Toast.makeText(activity, refreshedPeers.toString(), Toast.LENGTH_SHORT).show();
//
//                        WifiP2pConfig config = new WifiP2pConfig();
//
//                        config.deviceAddress = refreshedPeers.get(0).deviceAddress;
//                        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
//
//                            @Override
//                            public void onSuccess() {
//                                Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onFailure(int reason) {
//                                Toast.makeText(activity, "Not Connected", Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    }
                });
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }
    }

    public static class FileServerAsyncTask extends AsyncTask {
        private Context context;
        private TextView statusText;

        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
//                File tempPeopleFile ;
//                File tempMarkerFile ;

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */

                try {
//                    final File f = new File(Environment.getExternalStorageDirectory() + "/"
//                            + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
//                            + ".txt");
//
                    InputStream inputStream = client.getInputStream();
//                    copyFile(inputstream, new FileOutputStream(f));
//                    File peopleFile = File.createTempFile("people", null, context.getCacheDir());
//                    File markerFile = File.createTempFile("marker", null, context.getCacheDir());

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(inputStream, writer);
                    String theString = writer.toString();

                    return(theString);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (
                    IOException e) {
                Log.e("hi", e.getMessage());
                return null;
            }

        }
    }
}
/*
    private WifiP2pManager.PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peerList.getDeviceList();

            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);

                System.out.println(peers);

                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
            }

            if (peers.size() == 0) {
                System.out.println("No peers");
                return;
            }
        }
    };*/
