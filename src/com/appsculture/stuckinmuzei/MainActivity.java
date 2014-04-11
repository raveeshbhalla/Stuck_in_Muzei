package com.appsculture.stuckinmuzei;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

public class MainActivity extends Activity implements View.OnClickListener {

    SharedPreferences mPrefs;
    ImageButton mThree, mSix, mTwelve, mTwentyFour;
    CheckBox mWifiOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = this.getSharedPreferences("stuckinmuzei", this.MODE_PRIVATE);

        mThree = (ImageButton) findViewById(R.id.imageButtonThree);
        mSix = (ImageButton) findViewById(R.id.imageButtonSix);
        mTwelve = (ImageButton) findViewById(R.id.imageButtonTwelve);
        mTwentyFour = (ImageButton) findViewById(R.id.imageButtonTwentyFour);

        mThree.setOnClickListener(this);
        mSix.setOnClickListener(this);
        mTwelve.setOnClickListener(this);
        mTwentyFour.setOnClickListener(this);

        mWifiOnly = (CheckBox) findViewById(R.id.checkboxOnlyWifi);
        mWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean("wifionly", b);
                editor.commit();
            }
        });
        setState();
    }

    @Override
    public void onClick(View view) {
        SharedPreferences.Editor editor = mPrefs.edit();
        int current = mPrefs.getInt("update", 24);
        int newVal = current;
        switch (view.getId()) {
            case R.id.imageButtonThree:
                newVal = 3;
                break;
            case R.id.imageButtonSix:
                newVal = 6;
                break;
            case R.id.imageButtonTwelve:
                newVal = 12;
                break;
            case R.id.imageButtonTwentyFour:
                newVal = 24;
                break;
        }

        if (current != newVal) {
            editor.putInt("update",newVal);
            editor.commit();
            setState();
            Intent intent = new Intent(MainActivity.this,StuckInRemoteArtSource.class);
            intent.setAction(StuckInRemoteArtSource.ACTION_RESET_UPDATE);
            startService(intent);
        }
    }

    private void setState() {
        switch (mPrefs.getInt("update", 24)) {
            case 3:
                mThree.getBackground().setAlpha(255);
                mSix.getBackground().setAlpha(80);
                mTwelve.getBackground().setAlpha(80);
                mTwentyFour.getBackground().setAlpha(80);
                break;
            case 6:
                mThree.getBackground().setAlpha(80);
                mSix.getBackground().setAlpha(255);
                mTwelve.getBackground().setAlpha(80);
                mTwentyFour.getBackground().setAlpha(80);
                break;
            case 12:
                mThree.getBackground().setAlpha(80);
                mSix.getBackground().setAlpha(80);
                mTwelve.getBackground().setAlpha(255);
                mTwentyFour.getBackground().setAlpha(80);
                break;
            case 24:
                mThree.getBackground().setAlpha(80);
                mSix.getBackground().setAlpha(80);
                mTwelve.getBackground().setAlpha(80);
                mTwentyFour.getBackground().setAlpha(255);
                break;
        }
    }
}
