package com.lock.sdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.lock.locklib2.BleConnectionManager;
import com.lock.locklib2.LockLibManager;
import com.lock.sdk.databinding.ActivityMainBinding;

public class MainActivity2 extends AppCompatActivity {

    private ActivityMainBinding binding;
    BleConnectionManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mgr = new BleConnectionManager(this);
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mgr.mBluetoothManager.getAdapter().getRemoteDevice("0103b8804f53af4f");
            }
        });
    }
}