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
import com.lock.locklib.blelibrary.Adapter.BleAdapter;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceListEvent;
import com.lock.locklib.blelibrary.EventBean.EventBean;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
import com.lock.locklib.blelibrary.EventBean.SaveBleEvent;
import com.lock.locklib.blelibrary.EventBean.WriteDataEvent;
import com.lock.locklib.blelibrary.base.BleBase;
import com.lock.locklib.blelibrary.base.BleStatus;
import com.lock.locklib.blelibrary.main.BluetoothLeService;
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

public class MainActivity extends AppCompatActivity implements ClickCallback {

    private ActivityMainBinding binding;
    private BleShowAdapter bleShowAdapter;
    private LockTester tester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
        tester = new LockTester();
        tester.prepare(SearchBle.getInstance(this));
        initViews();
        initBle();
        setClickListeners();
        LockTester.getSelectedEventLiveData().observe(this, new Observer<ChangesDeviceEvent>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(ChangesDeviceEvent changesDeviceEvent) {
                binding.selected.status.setText(LockTester.getLockStatus());
                binding.selected.address.setText("<" + changesDeviceEvent.mBleBase.getAddress() + ">");
                binding.selected.name.setText(changesDeviceEvent.mBleBase.getName());
                binding.selected.password.setText("pass :" + changesDeviceEvent.mBleBase.getPassWord());
            }
        });
        LockTester.getBleListLiveData().observe(this, new Observer<ArrayList<ChangesDeviceEvent>>() {
            @Override
            public void onChanged(ArrayList<ChangesDeviceEvent> changesDeviceEvents) {
//                bleShowAdapter.setDataSet(changesDeviceEvents);
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
                    LockTester.unlockByAddress(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                LockTester.unlock(MainActivity.this);
                toast("Unlocking");
            }
        });
        binding.disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
                    LockTester.disconnectByAddress(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                toast("Disconnecting");
                LockTester.disconnect(MainActivity.this);
            }
        });
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
                    LockTester.connectByAddress(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                toast("Connecting");
                LockTester.connect(MainActivity.this);
            }
        });
        binding.authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
                    LockTester.authenticateByAddress(MainActivity.this, binding.editText.getText().toString(), "123456");
                    return;
                }
                if (!checkEvent())
                    return;
                toast("Authenticating with password=123456");
                LockTester.authenticate(MainActivity.this);
            }
        });
        binding.getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editText.getText().toString().length() > 0){
                    LockTester.getStatusByAddress(MainActivity.this, binding.editText.getText().toString());
                    return;
                }
                if (!checkEvent())
                    return;
                LockTester.getStatus(MainActivity.this);
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
        ServiceCommand.stop(this);
    }

    private boolean checkEvent() {
        if (LockTester.getSelectedEventLiveData().getValue() == null) {
            Toast.makeText(this, "select bluetooth device first!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initBle() {
        NotificationBean notificationBean = new NotificationBean(R.mipmap.ic_launcher, MainActivity.class.getName());
        ServiceCommand.start(this, notificationBean);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tester != null)
            tester.destroy();
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void bleClicked(ChangesDeviceEvent event) {
        LockTester.eventSelected(event);
//        LockTester.eventSelected("0787f830022f2761");
    }
}