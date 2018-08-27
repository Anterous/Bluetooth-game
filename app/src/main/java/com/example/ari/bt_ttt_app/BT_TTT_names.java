package com.example.ari.bt_ttt_app;

import android.Manifest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;
import java.io.IOException;
import java.util.UUID;
import android.util.Log;

public class BT_TTT_names extends AppCompatActivity {

    private static final int Finished_Activity = 3;
    private static final String TAG = "BT_TTT_names_activity";
    private final static UUID uuid = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001a");
    public static final String MY_SERVICE_NAME = "BluetoothService";
    final private int REQUEST_CODE_ASK_ACCESS_FINE_LOCTION = 124;
    AppCompatActivity BT_TTT_names;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DISCOVERABILITY = 85;
    private static final int REQUEST_DISCOVERABILITY_TIME = 300;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;

    public static BluetoothDevice mBluetoothDevice;
    public static BluetoothSocket mBluetoothSocket = null;
    private String selectedDevice=null;

    ListeningThread t = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        BT_TTT_names=this;
        checkPerm();


    }

    //Check that app is allowed to use Bluetooth
    private void checkPerm(){

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_ACCESS_FINE_LOCTION);
            return;
        }
        testBT();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_CODE_ASK_ACCESS_FINE_LOCTION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload activity with permission granted or use the features what required the permission
                    testBT();
                } else
                {
                    Toast.makeText(this, "The app was not allowed to use Access Fine Location", Toast.LENGTH_LONG).show();
                }
            }
        }

    }



    protected void testBT() {
        // take an instance of BluetoothAdapter - Bluetooth radio
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Status: not supported");
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            text = findViewById(R.id.text);
            onBtn = findViewById(R.id.turnOn);
            onBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    on(v);
                }
            });

            offBtn = findViewById(R.id.turnOff);
            offBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    off(v);
                }
            });

            listBtn = findViewById(R.id.paired);
            listBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    list(v);
                }
            });

            findBtn = findViewById(R.id.search);
            findBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    find(v);
                }
            });

            myListView = findViewById(R.id.listView1);
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedDevice=myListView.getItemAtPosition(i).toString() ;
                }
            });


            // create arrayAdapter that contains BluetoothDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);
        }
    }

    public void on(View view){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(mBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
        if(requestCode == REQUEST_ENABLE_DISCOVERABILITY) {
            if(resultCode==REQUEST_DISCOVERABILITY_TIME) {
                text.setText("Discoverability: Enabled");
            }
            if(resultCode==RESULT_CANCELED) {
                text.setText("Discoverability: Disabled");
            }
        }

    }

    public void list(View view){
        // get already paired devices
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        // Add devices to ArrayAdapter
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

        Toast.makeText(getApplicationContext(),"Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Action finds bluetooth adapters
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add name and the MAC address of the BluetoothDevice to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if (mBluetoothAdapter.isDiscovering()) {
            // Check if discovery is already enabled. Cancel the discovery if that's the case.
            mBluetoothAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            enableDiscoverability();
            mBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            text.setText("Status: Find devices");
            BTArrayAdapter.add("Discovery" + "\n" + "Started");
            BTArrayAdapter.notifyDataSetChanged();
        }
    }

    public void off(View view){
        //Disable BT
        mBluetoothAdapter.disable();
        text.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Unregister BT receiver
        unregisterReceiver(bReceiver);
    }


    protected void server(View v){
        Button serverB=findViewById(R.id.buttonServer);
        if (serverB.getText().toString().equals("Server")){

            // Start server
            t = new ListeningThread();
            t.start();
            // Notify that server was started.
            serverB.setText("Stop");
            text.setText("Status: Server Active");

        }else{
            // Stop server if something goes wrong
            if (t!=null) {

                t.cancel();
                t.interrupt();
                t=null;
            }
            // Display Server text when server is stopped.
            serverB.setText("Server");
            text.setText("Status: -");
        }

    }

    protected void client(View v){
        Button clientB=findViewById(R.id.buttonClient);
        String deviceMacAddress;

        if (selectedDevice!=null && clientB.getText().toString().equals("Client")) {
            String[] s = selectedDevice.split("\n");
            deviceMacAddress = s[1];

            mBluetoothAdapter.cancelDiscovery();
            // Start connection
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceMacAddress);
            new ConnectingThread(device).start();
            // Notify that client mode was selected.
            text.setText("Status: Client selected");
            clientB.setText("Stop");
        }
         else {
             text.setText("Status:- ");
             clientB.setText("Client");
        }

    }

    protected void enableDiscoverability() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_DISCOVERABILITY_TIME);
        startActivityForResult(discoverableIntent,REQUEST_ENABLE_DISCOVERABILITY);
    }
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Move information to Game activity and finish this activity.
        mBluetoothDevice = device;
        mBluetoothSocket = socket;
        Intent intent = new Intent(BT_TTT_names.this, BT_TTT.class);
        startActivityForResult(intent, Finished_Activity);

    }
    private class ListeningThread extends Thread {

        BluetoothServerSocket bluetoothServerSocket;

        public ListeningThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_SERVICE_NAME, uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e); }
            bluetoothServerSocket = tmp;
        }
        public void run() {
            BluetoothSocket bluetoothSocket;
            while (true) {
                try {
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    break; }
                if (bluetoothSocket != null) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Connection accepted.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    connected(bluetoothSocket, bluetoothSocket.getRemoteDevice());

                    try {
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break; }
            }
        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the serversocket", e);
            }
        }
    }

    private class ConnectingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectingThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            if (mmSocket != null && mmDevice != null) {
                // The connection accepted and passed to other activity.
                connected(mmSocket, mmDevice);
            }
        }
        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

}
