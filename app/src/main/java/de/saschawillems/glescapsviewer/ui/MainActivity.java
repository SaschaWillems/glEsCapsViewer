/*
 * OpenGL ES hardware capability viewer and database
 * Copyright (C) 2024 - Modern Update
 *
 * This code is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3 as published by the Free Software Foundation.
 */

package de.saschawillems.glescapsviewer.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import de.saschawillems.glescapsviewer.R;
import de.saschawillems.glescapsviewer.adapter.ViewPagerAdapter;
import de.saschawillems.glescapsviewer.util.GLESInfoCollector;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        initializeUI();

        // Collect OpenGL ES information on background thread
        collectGLESInfo();
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Device");
                    tab.setIcon(R.drawable.ic_device);
                    break;
                case 1:
                    tab.setText("OpenGL ES");
                    tab.setIcon(R.drawable.ic_opengl);
                    break;
                case 2:
                    tab.setText("Capabilities");
                    tab.setIcon(R.drawable.ic_capabilities);
                    break;
                case 3:
                    tab.setText("Extensions");
                    tab.setIcon(R.drawable.ic_extensions);
                    break;
            }
        }).attach();
    }

    private void collectGLESInfo() {
        new Thread(() -> {
            try {
                GLESInfoCollector.getInstance(this).collectAllInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh) {
            collectGLESInfo();
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_about) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_description)
                .setPositiveButton(R.string.about_close, null)
                .show();
    }
}
