package com.ccjeng.tptrashcan.view;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ccjeng.tptrashcan.R;

/**
 * Created by andycheng on 2015/12/30.
 */
public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
