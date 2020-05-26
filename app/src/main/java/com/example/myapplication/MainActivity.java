package com.example.myapplication;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;
        import android.Manifest;
        import android.annotation.SuppressLint;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothServerSocket;
        import android.bluetooth.BluetoothSocket;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.LinearLayout;
        import android.widget.ScrollView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.lang.reflect.Field;
        import java.util.ArrayList;
        import java.util.Set;
        import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    int REQUEST_BLUETOOTH_EN;
    BluetoothAdapter myBA;
    Set<BluetoothDevice> myBD;
    ArrayList<String> newDevice = new ArrayList<String>();
    ArrayList<BTDeviceConnectView> btViews = new ArrayList<BTDeviceConnectView>();
    Button enable;
    Button disable;
    Button select;
    Button discovery;
    TextView text;
    TextView receiveMsg;
    EditText message;
    Button sendText;
    Button listen;
    TextView status;
    SendReceiver sendReceiver;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    private static final UUID myUUID = UUID.fromString("da8f0ff3-5059-46ba-ae89-e790a5ed0165");
    private static final String APP_NAME = "BTChat";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enable = findViewById(R.id.enable);
        disable = findViewById(R.id.disable);
        select  = findViewById(R.id.select);
        discovery = findViewById(R.id.discovery);
        myBA = BluetoothAdapter.getDefaultAdapter();
        receiveMsg = findViewById(R.id.receive_msg);
        sendText = findViewById(R.id.send_text);
        message = findViewById(R.id.message);
        listen = findViewById(R.id.listen);
        status = findViewById(R.id.status);
        REQUEST_BLUETOOTH_EN = 1;

        if(myBA == null) {
            enable.setEnabled(false);
            disable.setEnabled(false);
            select.setEnabled(false);
            discovery.setEnabled(false);

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            bluetoothOnMethod();
            bluetoothOffMethod();
            bluetoothSelectMethod();
            bluetoothDiscoveryMethod() ;
            checkListenServer();
            checkListenClient();


//            Thread_BT newThread = new Thread_BT();
//            newThread.start();
        }


    }
    private void bluetoothOnMethod() {
        enable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(myBA != null) {
                    if(!myBA.isEnabled()) {
                        Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOnIntent, REQUEST_BLUETOOTH_EN);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "bluetooth not support",Toast.LENGTH_LONG).show();
                }
                // Code here executes on main thread after user presses button
            }
        });
    }
    private void bluetoothOffMethod() {
        disable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(myBA.isEnabled()) {
                    myBA.disable();
                }
                // Code here executes on main thread after user presses button
            }
        });
    }
    private void bluetoothSelectMethod() {
        select.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myBD = myBA.getBondedDevices();
                for (BluetoothDevice device : myBD) {
                    String address = device.getAddress();
                    if (device.getName()!= null) {
                        String name = device.getName();
                        addButtonLinearLayout(name+"  "+address, false, "pairedScroll", device);
                    } else {
                        addButtonLinearLayout(address, false, "pairedScroll",device);
                    }
                }
                //checkBtViews();
            }
        });
    }

    private void bluetoothDiscoveryMethod() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        discovery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myBA.isDiscovering()) {
                    // the button is pressed when it discovers, so cancel the discovery
                    int resID = getResId("discoveryScroll", R.id.class);
                    ScrollView curScroll = findViewById(resID);
                    LinearLayout curLayout = curScroll.findViewWithTag("discoveryScroll".replace("Scroll", "layout"));
                    for(BTDeviceConnectView btView: btViews) {
                        curLayout.removeView(btView.getLayoutCustom());
                    }
                    unregisterReceiver(myReceiver);
                    myBA.cancelDiscovery();
                } else {
                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(myReceiver, intentFilter);
                    myBA.startDiscovery();
                }

            }
        });
    }


    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //do some code
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = device.getAddress();
                if (device.getName()!= null) {
                    String name = device.getName();
                    if(newDevice.contains(address)) {
                        setTextButtonLinearLayout(address, name+"  "+address, "discoveryScroll");
                        newDevice.remove(address);
                    } else {
                        btViews.add(addButtonLinearLayout(address + "  " + name, false, "discoveryScroll", device));
                    }
                } else {
                    newDevice.add(address);
                    btViews.add(addButtonLinearLayout(address, false, "discoveryScroll", device));
                }
            }
            Log.d("dd", "dd");
        }
    };

    protected BTDeviceConnectView addButtonLinearLayout(String customTag, boolean isConnect, String layoutId, BluetoothDevice device) {
        int resID = getResId(layoutId, R.id.class);
        ScrollView curScroll = findViewById(resID);
        LinearLayout curLayout = curScroll.findViewWithTag(layoutId.replace("Scroll", "layout"));
        BTDeviceConnectView btCustom = new BTDeviceConnectView(this, this,isConnect, customTag, device);
        curLayout.addView(btCustom.getLayoutCustom());
        return btCustom;
    }
    protected void setTextButtonLinearLayout(String customTag, String customTagNew, String layoutId) {
        int resID = getResId(layoutId, R.id.class);
        ScrollView curScroll = findViewById(resID);
        LinearLayout curLayout = curScroll.findViewWithTag(layoutId.replace("Scroll", "layout"));
        LinearLayout layoutCustom = curLayout.findViewWithTag(customTag);
        layoutCustom.setTag(customTagNew);
        if(layoutCustom.findViewWithTag(customTag+"connect")!= null) {
            Button conn = layoutCustom.findViewWithTag(customTag+"connect");
            conn.setTag(customTagNew+"connect");
        }
        if(layoutCustom.findViewWithTag(customTag+"disconnect")!= null){
            Button disc = layoutCustom.findViewWithTag(customTag+"disconnect");
            disc.setTag(customTagNew+"disconnect");
        }
        if(layoutCustom.findViewWithTag(customTag+"text")!= null){
            TextView text = layoutCustom.findViewWithTag(customTag+"text");
            text.setTag(customTagNew+"text");
            text.setText(customTag);
        }
    }
    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected void abc () {
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoveryIntent);

    }
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if(request == REQUEST_BLUETOOTH_EN) {
            if(result == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "bluetooth is enable",Toast.LENGTH_LONG).show();
            } else if (result == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "bluetooth enabling cancelled",Toast.LENGTH_LONG).show();
            }
        }
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case  STATE_LISTENING : {
                    status.setText("Listening");
                    break;

                }
                case STATE_CONNECTING : {
                    status.setText("Connecting");
                    break;
                }
                case STATE_CONNECTED : {
                    status.setText("Connected");
                    break;
                }
                case STATE_CONNECTION_FAILED  : {
                    status.setText("Connection failed");
                    break;
                }
                case STATE_MESSAGE_RECEIVED : {
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMsg = new String(readBuff, 0 ,msg.arg1);
                    receiveMsg.setText(tempMsg);
                    // write it later
                    break;
                }
            }
            return true;
        }
    });


    protected void checkListenServer ( ) {
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });

    }
    protected void checkListenClient() {
        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = String.valueOf(message.getText());
                sendReceiver.write(string.getBytes());
            }
        });
    }

    public void clientProcess (BluetoothDevice device) {
        ClientClass clientClass = new ClientClass(device);
        clientClass.start();
        Message msg = Message.obtain();
        msg.what = STATE_CONNECTING;
        handler.sendMessage(msg);
    }

    private class SendReceiver extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceiver (BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tempInt = null;
            OutputStream tempOut = null;
            try {
                tempInt = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            }catch (IOException ex) {
                ex.printStackTrace();
            }
            inputStream = tempInt;
            outputStream = tempOut;

        }
        public void run () {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException ex ) {
                    ex.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                serverSocket = myBA.listenUsingRfcommWithServiceRecord(APP_NAME, myUUID);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
        public void run() {
            BluetoothSocket socket = null;
            while(socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();

                } catch (IOException e){
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceiver = new SendReceiver(socket);
                    sendReceiver.start();
                    //do something to send or receive
                    break;
                }
            }
        }
    }
    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;
        public ClientClass(BluetoothDevice device) {
            this.device = device;
            try {
                socket = this.device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        public void run () {
            myBA.cancelDiscovery();
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what= STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceiver = new SendReceiver(socket);
                sendReceiver.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what= STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }
}
