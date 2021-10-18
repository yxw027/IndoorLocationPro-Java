package com.hust.indoorlocation.ui.main.collector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.hust.indoorlocation.R;
import com.hust.indoorlocation.databinding.ActivityCollectorBinding;
import com.hust.indoorlocation.ui.main.survey.SensorInfo;
import com.hust.indoorlocation.ui.settings.SettingsActivity;
import com.hust.indoorlocation.tools.util.LogUtil;
import com.hust.indoorlocation.tools.util.SensorUtil;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author admin
 */
public class CollectorActivity<COLLECTOR_IS_COLLECTING> extends AppCompatActivity {
    /**
     * 传感器列表
     */
    List<Sensor> sensors;
    /**
     * 传感器信息解析
     */
    static HashMap<Integer, SensorInfo> infoMap;

    private ActivityCollectorBinding mBinding;
    private CollectorReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCollectorBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());


        //获取系统传感器管理器
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //通过系统传感器管理器..获取本机所有传感器.
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        infoMap= SensorUtil.INSTANCE.getInfoMap();

        initActionBar();
        initCollectList();
        initBottomNav();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initCollectList() {
        mBinding.CollectList.removeAllViews();
        for (Sensor s : sensors) {
            MaterialCheckBox checkText=new MaterialCheckBox(this);
            checkText.setId(s.getType());
            if (infoMap.containsKey(s.getType())) {
                checkText.setText( infoMap.get(s.getType()).sensorTypeName);
            }else{
                checkText.setText( s.getName()+" 未知类型");
            }
            mBinding.CollectList.addView(checkText);
        }
    }

    private HashMap<Integer,Integer> mProgress;

    private void setSensorProgress(HashMap<Integer, Integer> mProgress) {
        for (Integer key:mProgress.keySet())
        {
            MaterialCheckBox checkText=findViewById(key);
            if (infoMap.containsKey(key)) {
                checkText.setText( infoMap.get(key).sensorTypeName+"   "+mProgress.get(key));
            }else{
                return;
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
                LogUtil.d("activity, SearchActivity::class.java 22");
                Intent intent = new Intent(CollectorActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarBase);
        toolbar.setTitle(this.getClass().getSimpleName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * bottom_navigation 点击事件
     */
    private void initBottomNav() {
        mBinding.bottomNavigationBle.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_collect_start:
                        if(!isCollect){
                            mBinding.collectInfo.setText("开始收集数据\n");
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_pause_24);
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setTitle("停止");
                            startCollectServer();
                        }else{
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_play_arrow_24);
                            mBinding.bottomNavigationBle.getMenu().getItem(0).setTitle("收集");
                            stopCollectServer();
                        }
                        break;
                    case R.id.action_collect_end:
                        initCollectList();
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
    }
    private Boolean isCollect=false;
    public final class CollectorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(@Nullable Context context, @Nullable Intent intent) {
            if (intent != null) {
                int what = intent.getIntExtra("what", 0);
                String info = intent.getStringExtra("info");
                LogUtil.d(what+" receive .."+info);
                switch(what) {
                    case 1:
                        mBinding.collectInfo.append("collecting...\n");
                        break;

                    case 2:
                        mBinding.collectInfo.append("done...\n");
                        break;
                    case 3:
                        mBinding.collectInfo.append(info+"\n");
                        break;
                    default:
                        break;

                }

            }
        }
    }

    public  int  COLLECTOR_IS_COLLECTING = 0X1;
    public static int  COLLECTOR_IS_READY = 0xBABE;
    public static int  COLLECTOR_GOT_INFO = 0xABCD;

    public void startCollectServer(){
        isCollect=true;
        Intent intent = new Intent(this, CollectorService.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public void stopCollectServer(){
        isCollect=false;
        Intent intent = new Intent(this, CollectorService.class);
        unbindService(conn);
        stopService(intent);
    }

    private CollectorService msgService;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            msgService = ((CollectorService.DownloadBinder)service).getService();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // 循环任务，按照上一次任务的发起时间计算下一次任务的开始时间
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                if(isCollect){
                    mProgress=msgService.getProgress();
                    setSensorProgress(mProgress);
                 //   LogUtil.d("first:" + TimeUtil.INSTANCE.getDateTime()+mProgress);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(this.getPackageName() + ".COLLECTOR_RECEIVER");
        receiver = new CollectorReceiver();
        registerReceiver(receiver, intentFilter);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isCollect){
            stopCollectServer();
        }
        mScheduledExecutorService.shutdown();
        unregisterReceiver(receiver);
    }

    // 通过静态方法创建ScheduledExecutorService的实例
    private ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(4);

}