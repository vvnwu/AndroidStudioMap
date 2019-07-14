package com.example.maptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

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
                        Toast.makeText(activity, "Found peers!", Toast.LENGTH_SHORT).show();

                        WifiP2pConfig config = new WifiP2pConfig();

                        try {
                            config.deviceAddress = refreshedPeers.get(0).deviceAddress;
                            Toast.makeText(activity, refreshedPeers.toString(), Toast.LENGTH_SHORT).show();
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Toast.makeText(activity, "No valid peers, creating hotspot", Toast.LENGTH_SHORT).show();
                        }

                        if (config.deviceAddress != null) {
                            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

                                @Override
                                public void onSuccess() {
                                    Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Toast.makeText(activity, "Not Connected", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
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
}