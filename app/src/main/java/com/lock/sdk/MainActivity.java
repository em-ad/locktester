package com.lock.sdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.lock.locklib.blelibrary.main.ServiceCommand;
import com.lock.locklib.LockTester;
import com.lock.locklib.blelibrary.notification.NotificationBean;
import com.lock.locklib.blelibrary.search.SearchBle;
import com.lock.locklib.blelibrary.search.SearchListener;
import com.lock.sdk.databinding.ActivityMainBinding;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ClickCallback {

    ActivityMainBinding binding;
    private SearchBle mSearch;
    private ChangesDeviceEvent selectedEvent;
    private BleShowAdapter bleShowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initViews();
        initBle();
        setClickListeners();
    }

    private void setClickListeners() {
        binding.unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEvent())
                    return;
                toast("Unlocking");
                LockTester.unlock(MainActivity.this, selectedEvent.mBleBase);
            }
        });
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEvent())
                    return;
                toast("Connecting");
                LockTester.connect(MainActivity.this, selectedEvent.mBleBase, selectedEvent.mBleStatus);
            }
        });
        binding.authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEvent())
                    return;
                toast("Authenticating");
                LockTester.authenticate(MainActivity.this, selectedEvent.mBleBase);
            }
        });
        binding.getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEvent())
                    return;
                toast("Fetching Status");
                Toast.makeText(MainActivity.this, "Lock is now " + LockTester.getLockStatus(selectedEvent.mBleStatus), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        bleShowAdapter = new BleShowAdapter(this);
        ((RecyclerView) findViewById(R.id.recycler)).setAdapter(bleShowAdapter);
        ((RecyclerView) findViewById(R.id.recycler)).setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private boolean checkEvent() {
        if (selectedEvent == null) {
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
        new CountDownTimer(1000, 200) {

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
                if (events.size() > 0 && events.size() != bleShowAdapter.getItemCount())
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
        selectedEvent = event;
        binding.selected.address.setText(selectedEvent.mBleBase.getAddress());
        binding.selected.name.setText(selectedEvent.mBleBase.getName());
        binding.selected.password.setText(selectedEvent.mBleBase.getPassWord());
        binding.selected.root.setBackground(getResources().getDrawable(R.drawable.rounded_blue));

    }
}