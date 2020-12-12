package com.lock.sdk;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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

    ActivityMainBinding binding;
    private SearchBle mSearch;
//    private ChangesDeviceEvent selectedEvent;
    private BleShowAdapter bleShowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
        new LockTester().prepare();
        initViews();
        initBle();
        setClickListeners();
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
                if (!checkEvent())
                    return;
                toast("Unlocking");
                LockTester.unlock(MainActivity.this);
            }
        });
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEvent())
                    return;
                toast("Connecting");
                LockTester.connect(MainActivity.this);
            }
        });
        binding.authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEvent())
                    return;
                toast("Authenticating with pass=123456");
                LockTester.authenticate(MainActivity.this);
            }
        });
        binding.getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEvent())
                    return;
                Toast.makeText(MainActivity.this, "Lock is now " + LockTester.getLockStatus(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clickBleWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (LockTester.selectedEvent != null)
                    bleClicked(LockTester.selectedEvent);
            }
        }, 500);
    }

    private void initViews() {
        bleShowAdapter = new BleShowAdapter(this);
        ((RecyclerView) findViewById(R.id.recycler)).setAdapter(bleShowAdapter);
        ((RecyclerView) findViewById(R.id.recycler)).setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private boolean checkEvent() {
        if (LockTester.selectedEvent == null) {
            Toast.makeText(this, "select bluetooth device first!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initBle() {
        NotificationBean notificationBean = new NotificationBean(R.mipmap.ic_launcher, MainActivity.class.getName());
        ServiceCommand.start(this, notificationBean);
//        EventTool.register(this);
        this.mSearch = SearchBle.getInstance(this);
        this.mSearch.setSearchHas(true);
        new CountDownTimer(1000, 300) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                ArrayList<ChangesDeviceEvent> events = new ArrayList<>();
                for (int i = 0; i < mSearch.sharedPreferences.getSaveBle().BaseList.size(); i++) {
                    ChangesDeviceEvent event = new ChangesDeviceEvent(mSearch.sharedPreferences.getSaveBle().BaseList.get(i), new BleStatus());
                    events.add(event);
                }
//                if (events.size() > 0 && events.size() != bleShowAdapter.getItemCount())
                    bleShowAdapter.setDataSet(events);
                this.start();
            }

        }.start();
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void bleClicked(ChangesDeviceEvent event) {
        LockTester.eventSelected(event);
//        LockTester.selectedEvent = event;
        binding.selected.status.setText(LockTester.getLockStatus());
        binding.selected.address.setText("<" + LockTester.selectedEvent.mBleBase.getAddress() + ">");
        binding.selected.name.setText(LockTester.selectedEvent.mBleBase.getName());
        binding.selected.password.setText("pass :" + LockTester.selectedEvent.mBleBase.getPassWord());
        new CountDownTimer(500, 500){

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                bleClicked(LockTester.selectedEvent);
            }
        }.start();
    }
}