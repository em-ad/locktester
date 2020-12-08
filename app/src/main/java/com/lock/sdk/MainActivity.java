package com.lock.sdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lock.locklib.blelibrary.EventBean.ChangesDeviceEvent;
import com.lock.locklib.blelibrary.EventBean.ChangesDeviceListEvent;
import com.lock.locklib.blelibrary.EventBean.EventBean;
import com.lock.locklib.blelibrary.EventBean.EventTool;
import com.lock.locklib.blelibrary.EventBean.OtherEvent;
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

public class MainActivity extends AppCompatActivity implements SearchListener.ScanListener {

    ActivityMainBinding binding;
    private SearchBle mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initBle();

        binding.unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("Unlocking");
                LockTester.unlock(MainActivity.this, "F8:30:02:2F:2E:8E", "Buckle locksaeid", "123456");
            }
        });
        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("Connecting");
                LockTester.connect(MainActivity.this, "F8:30:02:2F:2E:8E", "Buckle locksaeid");
            }
        });
        binding.authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("Authenticating");
                LockTester.authenticate(MainActivity.this,"F8:30:02:2F:2E:8E", "Buckle locksaeid", "123456");
            }
        });
        binding.getStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("Fetching Status");
                Toast.makeText(MainActivity.this, "Lock is now " + LockTester.getLockStatus(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initBle() {
        NotificationBean notificationBean = new NotificationBean(R.mipmap.ic_launcher, MainActivity.class.getName());
        ServiceCommand.start(this, notificationBean);
        EventTool.register(this);
        this.mSearch = SearchBle.getInstance(this);
        this.mSearch.setListener(this);
        this.mSearch.setDataListener(new SearchListener.ScanDataListener() {
            public void onLeScan(byte[] bArr, BleBase bleBase, BleStatus bleStatus) {
                Log.e("tag", "Scan with 3 arguments called!" );
            }
        });
        this.mSearch.setSearchHas(false);
        this.mSearch.search();
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLeScan(BleBase bleBase, BleStatus bleStatus) {
        Log.e("tag", "Scan with 2 arguments called!"  );
    }
}