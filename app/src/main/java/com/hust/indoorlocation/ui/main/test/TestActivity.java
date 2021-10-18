package com.hust.indoorlocation.ui.main.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hust.baiduimap.util.DemoInfo;
import com.hust.baiduimap.util.DemoListAdapter;
import com.hust.indoorlocation.R;

/**
 * @author admin
 */
public class TestActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menulist);

        ListView demoList = (ListView) findViewById(R.id.mapList);
        // 添加ListItem，设置事件响应
        demoList.setAdapter(new DemoListAdapter(this,DEMOS));
        demoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                onListItemClick(index);
            }
        });

    }

    void onListItemClick(int index) {
        Intent intent;
        intent = new Intent(this, DEMOS[index].demoClass);
        this.startActivity(intent);
    }

    private static final DemoInfo[] DEMOS = {
            new DemoInfo( R.string.div_title_pdrData, R.string.div_desc_pdrData, PdrDataActivity.class),
            new DemoInfo( R.string.div_title_simulation, R.string.div_desc_simulation, PdrNaiveActivity.class),
            };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}