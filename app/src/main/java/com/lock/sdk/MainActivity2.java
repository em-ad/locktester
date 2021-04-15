package com.lock.sdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lock.locklib2.BleConnectionManager;
import com.lock.sdk.databinding.ActivityMainBinding;

public class MainActivity2 extends AppCompatActivity{

    private ActivityMainBinding binding;
    BleConnectionManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mgr = new BleConnectionManager(MainActivity2.this);
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //grey
                mgr.addDevice("98:84:E3:CF:F1:71");
            }
        });

        binding.authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //orange
                mgr.addDevice("F8:30:02:2F:27:61");
            }
        });

        binding.unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //black
                mgr.addDevice("B8:80:4F:53:AF:4F");
            }
        });
    }
}