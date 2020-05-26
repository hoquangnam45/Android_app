package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class BTDeviceConnectView  {
    Button connect;
    LinearLayout layout;
    TextView textView;
    Context curContext;
    String deviceName;
    boolean isConnect;
    BTDeviceConnectView thisbuttonview = this;
    BluetoothDevice device;
    static BTDeviceConnectView curConnect;
    MainActivity act;
    public BTDeviceConnectView(MainActivity act, Context context, boolean isConnect, String deviceName, BluetoothDevice device) {
        this.device = device;
        curContext = context;
        this.act = act;
        this.isConnect = isConnect;
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setWeightSum(10.0f);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 3.0f));
        textView.setTag(deviceName + "text");
        textView.setText(deviceName);
        connect = new Button(context);
        connect.setTag(deviceName + "button");
        connect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 7.0f));
        if (!isConnect) {
            connect.setText("CONNECT");
        } else {
            connect.setText("DISCONNECT");
        }
        layout.addView(textView);
        layout.addView(connect);
        layout.setTag(deviceName);
        checkAndActionConnect();
    }

    public LinearLayout getLayoutCustom() {
        return layout;
    }

    public void checkAndActionConnect() {
            connect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!isConnect) {
                        act.clientProcess(device);
                    } else {
                    }
                    if(curConnect== null) {
                        isConnect = !isConnect;
                        connect.setText(changeState());
                        curConnect = thisbuttonview;
                    }else if (curConnect != thisbuttonview){
                        curConnect.isConnect = !curConnect.isConnect;
                        curConnect.connect.setText(changeState());
                        curConnect.isConnect = !curConnect.isConnect;
                        curConnect.connect.setText(changeState());
                    }
                    else {
                        isConnect = !isConnect;
                        connect.setText(changeState());
                    }

                }
            });
    }
    public void changeTextAndState () {
        isConnect = !isConnect;
        connect.setText(changeState());
    }
    public String changeState() {
        if (isConnect) {
            return "DISCONNECT";
        } else {
            return "CONNECT";
        }
    }


}


