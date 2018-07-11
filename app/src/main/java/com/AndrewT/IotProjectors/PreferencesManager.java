package com.AndrewT.IotProjectors;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PreferencesManager {

    private static PreferencesManager ourInstance = new PreferencesManager();
    private SharedPreferences mPreferenceManager;

    public static PreferencesManager getInstance() {
        return ourInstance;
    }

    private PreferencesManager() {

        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getContext());

    }

    public String getProjectorId(){
        return mPreferenceManager.getString("pref_Projector", null);
    }
    public String getProjectorIP(){
        return mPreferenceManager.getString("pref_IP", null);
    }

}
