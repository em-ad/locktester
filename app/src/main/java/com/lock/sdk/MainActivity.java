package com.lock.sdk;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lock.locklib.OperationStatus;
import com.lock.locklib.blelibrary.Adapter.BleAdapter;
import com.lock.locklib.blelibrary.CommandCallback;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceListEvent;
import com.lock.locklib.blelibrary.EventBean.EventBean;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
import com.lock.locklib.blelibrary.EventBean.SaveBleEvent;
import com.lock.locklib.blelibrary.EventBean.WriteDataEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.main.ServiceCommand;
import com.lock.locklib.LockTester;
import com.lock.locklib.blelibrary.notification.NotificationBean;
import com.lock.locklib.blelibrary.search.SearchBle;
import com.lock.locklib.blelibrary.search.SearchListener;
import com.lock.sdk.databinding.ActivityMainBinding;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements ClickCallback, CommandCallback {

    private ActivityMainBinding binding;
    private BleShowAdapter bleShowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
        LockTester.getInstance().prepare(SearchBle.getInstance(this), this);
        LockTester.getInstance().setCallback(this);
        initViews();
//        initBle();
        setClickListeners();
//        binding.editText.setText("0787f830022f2761");
        binding.editText.setText("0103b8804f5367a2");
        LockTester.getInstance().getSelectedEventLiveData().observe(this, new Observer<ChangesDeviceEvent>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(ChangesDeviceEvent changesDeviceEvent) {
                binding.selected.status.setText(LockTester.getInstance().getLockStatus().getName());
                binding.selected.address.setText("<" + changesDeviceEvent.mBleBase.getAddress() + ">");
                binding.selected.name.setText(changesDeviceEvent.mBleBase.getName());
                binding.selected.password.setText("pass :" + changesDeviceEvent.mBleBase.getPassWord());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN, BLUETOOTH, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, CAMERA}, 313);
    }

    private void setClickListeners() {
        binding.unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
//                    LockTester.getInstance().unlockByAddress(MainActivity.this, binding.editText.getText().toString());
                    LockTester.getInstance().unlockByAddressAlt(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                LockTester.getInstance().unlock(MainActivity.this);
                toast("Unlocking");
            }
        });
        binding.disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
                    LockTester.getInstance().disconnectByAddress(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                toast("Disconnecting");
                LockTester.getInstance().disconnect(MainActivity.this);
            }
        });
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
                    LockTester.getInstance().connectByAddress(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                toast("Connecting");
                LockTester.getInstance().connect(MainActivity.this);
            }
        });
        binding.authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
//                    LockTester.getInstance().authenticateByAddress(MainActivity.this, binding.editText.getText().toString(), "123456");
                    LockTester.getInstance().authenticateByAddressAlt(MainActivity.this, binding.editText.getText().toString(), "000000");
                    return;
                }
                if (!checkEvent())
                    return;
                LockTester.getInstance().authenticate(MainActivity.this);
            }
        });
        binding.getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
                    LockTester.getInstance().getStatusByAddress(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                LockTester.getInstance().getStatus(MainActivity.this);
//                Toast.makeText(MainActivity.this, "Lock is now " + LockTester.getLockStatus(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        bleShowAdapter = new BleShowAdapter(this);
        ((RecyclerView) findViewById(R.id.recycler)).setAdapter(bleShowAdapter);
        ((RecyclerView) findViewById(R.id.recycler)).setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean checkEvent() {
        if (LockTester.getInstance().getSelectedEventLiveData().getValue() == null) {
            Toast.makeText(this, "select bluetooth device first!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void bleClicked(ChangesDeviceEvent event) {
        LockTester.getInstance().eventSelected(event);
//        LockTester.eventSelected("0787f830022f2761");
    }

    @Override
    public void commandExecuted(OperationStatus status) {
        binding.selected.status.setText(status.getName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, status.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}