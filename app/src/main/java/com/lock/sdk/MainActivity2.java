package com.lock.sdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lock.locklib2.BleConnectionManager;
import com.lock.locklib2.BleUtil;
import com.lock.locklib2.CommandCallback;
import com.lock.locklib2.OperationStatus;
import com.lock.sdk.databinding.ActivityMainBinding;

public class MainActivity2 extends AppCompatActivity implements CommandCallback {

    private ActivityMainBinding binding;
    BleConnectionManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mgr = new BleConnectionManager(MainActivity2.this, this);
        binding.editText.setText("0103b8804f53af4f");
//        BleUtil.convertAddress("0103b8804f53af4f");
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mgr != null)
                    mgr.addDevice(BleUtil.convertAddress(binding.editText.getText().toString()));
            }
        });

        binding.authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mgr != null)
                    mgr.manager.authenticateBlack();
            }
        });

        binding.unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mgr != null)
                    mgr.manager.unlockBlack();
            }
        });

        binding.getBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mgr != null)
                    mgr.manager.getBattery();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mgr = null;
    }

    @Override
    public void commandExecuted(OperationStatus status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.selected.status.setText(status.getName());
            }
        });
    }

    @Override
    public void commandExecuted(OperationStatus status, String code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.selected.status.setText(status.getName() + " " + code);
            }
        });
    }
}