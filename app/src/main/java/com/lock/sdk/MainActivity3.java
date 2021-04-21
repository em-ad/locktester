package com.lock.sdk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.lock.locklib3.BleConnectionManager;
import com.lock.locklib3.BleUtil;

import com.lock.locklib3.LockStatusEnum;
import com.lock.locklib3.callback.BatteryCallback;
import com.lock.locklib3.callback.StatusCallback;
import com.lock.locklib3.callback.UnlockCallback;
import com.lock.sdk.databinding.ActivityMainBinding;

public class MainActivity3 extends AppCompatActivity {

    private ActivityMainBinding binding;
    BleConnectionManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mgr = new BleConnectionManager(MainActivity3.this);
                mgr.setTimeOut(3000);
                mgr.manager.setRetryCount(4);
            }
        });
        binding.editText.setText("0103b8804f53af4f"); //black
//        binding.editText.setText("01039884e3cff171"); //grey
//        BleUtil.convertAddress("01039884e3cff171"); //grey
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
                binding.selected.status.setText("UNLOCKING");
                if (mgr != null)
                    mgr.unlock(BleUtil.convertAddress(binding.editText.getText().toString()), new UnlockCallback() {
                        @Override
                        public void unlocked() {
                            binding.selected.status.setText("UNLOCK SUCCESSFUL");
                        }

                        @Override
                        public void failed() {
                            binding.selected.status.setText("UNLOCK FAILED");
                        }
                    });
            }
        });

        binding.getBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.selected.status.setText("GETTING BATTERY");
                if (mgr != null)
                    mgr.getBattery(BleUtil.convertAddress(binding.editText.getText().toString()), new BatteryCallback() {
                        @Override
                        public void failed() {
                            binding.selected.status.setText("BATTERY QUERY FAILED");
                        }

                        @Override
                        public void batteryReceived(String result) {
                            binding.selected.status.setText("BATTERY RECEIVED " + result);
                        }
                    });
            }
        });

        binding.getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.selected.status.setText("GETTING STATUS");
                if (mgr != null)
                    mgr.getStatus(BleUtil.convertAddress(binding.editText.getText().toString()), new StatusCallback() {
                        @Override
                        public void failed() {
                            binding.selected.status.setText("STATUS FAILED");
                        }

                        @Override
                        public void statusReceived(LockStatusEnum state) {
                            binding.selected.status.setText("STATUS:" + state);
                        }
                    });
            }
        });

        binding.disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgr.manager.disconnectDevices();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mgr = null;
    }
}