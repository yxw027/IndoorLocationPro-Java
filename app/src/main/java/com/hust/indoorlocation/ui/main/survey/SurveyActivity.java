package com.hust.indoorlocation.ui.main.survey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
public class SurveyActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_survey);
//    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menulist);

        initActionBar();

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
    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarBase);
        toolbar.setTitle(this.getClass().getSimpleName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    void onListItemClick(int index) {
        Intent intent;
        intent = new Intent(this, DEMOS[index].demoClass);
        this.startActivity(intent);
    }

    private static final DemoInfo[] DEMOS = {
            new DemoInfo( R.string.survey_title_sensor, R.string.survey_desc_sensor, SensorSurveyActivity.class),
            new DemoInfo( R.string.survey_title_ble, R.string.survey_desc_ble, BleSurveyActivity.class),
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void goLineGraphs(int type){
        return;
    }
}