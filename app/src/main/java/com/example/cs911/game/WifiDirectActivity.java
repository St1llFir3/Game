package com.example.cs911.game;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.cs911.game.services.DeviceActionListener;
import com.example.cs911.game.fragments.DeviceDetailFragment;
import com.example.cs911.game.fragments.DeviceListFragment;
import com.example.cs911.game.services.WifiDirectBroadcastReceiver;

public class WifiDirectActivity extends Activity implements WifiP2pManager.ChannelListener, DeviceActionListener {

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    Handler mUpdateHandler;

    private WifiP2pConfig opponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hides title
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //hides title bar
        setContentView(R.layout.multi_frag_main);
        // add necessary intent values to be matched.
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    public void showPopUp(View view){
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.atn_direct_enable:
                        if (manager != null && channel != null){
                            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                        } else {
                            Log.d("TAG","channel or manager is null");
                        }
                        return true;
                    case R.id.atn_direct_discover:
                        if (!isWifiP2pEnabled) {
                            Toast.makeText(WifiDirectActivity.this,
                                    "Enable P2P from action bar button above or system settings",
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        manager.discoverPeers(channel, new WifiP2pManager.ActionListener(){
                            @Override
                            public void onSuccess(){
                                Toast.makeText(WifiDirectActivity.this,
                                        "Discover Initiated",
                                        Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(int reasonCode){
                                Toast.makeText(WifiDirectActivity.this,
                                        "Discovery Failed: " + reasonCode,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        return true;
                    default:
                        return this.onMenuItemClick(item);
                }
            }
        });
        popup.inflate(R.menu.action_items);
        popup.show();
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WifiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
    @Override
    public void configureDetails(WifiP2pDevice device) {
        opponent = configure(device);
        connect(opponent);
    }

    private WifiP2pConfig configure(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        log("Device Address: "+device.deviceAddress);
        config.wps.setup = WpsInfo.PBC;
        return config;
    }

    @Override
    public void cancelDisconnect() {
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        log("Connect abort request failed. Reason Code: " + reasonCode);
                    }
                });
            }
        }
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WifiDirectActivity.this, "Attempting to Connect",
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int i) {
                Toast.makeText(WifiDirectActivity.this, "Connect failed. Retry",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                fragment.getView().setVisibility(View.GONE);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this, "Severe! Channel may be lost permanently. Try Disable/Re-enable P2P",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null){
            fragmentList.clearPeers();
        }
    }

    public Handler getHandler(){
        return mUpdateHandler;
    }
    private void log(String msg){
        Log.d(this.getClass().toString(),msg);
    }
}
