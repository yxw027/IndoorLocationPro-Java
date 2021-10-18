package com.hust.indoorlocation.ui.main.test;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import com.hust.indoorlocation.R;
import com.hust.indoorlocation.ui.main.test.pdrdata.TestFragment;

/**
 * @author admin
 */
public class PdrDataActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdr_test);

        //反射获得类
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, TestFragment.Companion.newInstance())
                .commit();
    }
}