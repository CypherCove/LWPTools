/*******************************************************************************
 * Copyright 2015 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.cyphercove.lwptools.android.prefs;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PrefRoot {

	public PreferenceActivity activity;
	public SharedPreferences sharedPrefs;
	public ArrayList<Pref> listToAddAllPrefsTo;
	PreferenceManager manager;
	PrefResources prefResources;

	public PreferenceScreen root;

	public PrefRoot(PreferenceManager manager, PreferenceActivity activity,SharedPreferences sharedPrefs,
			int androidPreferenceLayoutChild,
			int chooseColorString,
			AdvancedColorPickerDialog.TextResources advancedColorPrefTextResources,
			ArrayList<Pref> listToAddAllPrefsTo){
		this.manager = manager;
		this.activity=activity;
		this.sharedPrefs=sharedPrefs;
		this.listToAddAllPrefsTo = listToAddAllPrefsTo;

		PrefResources prefResources = new PrefResources();
		prefResources.chooseColorString = chooseColorString;
		prefResources.androidPreferenceLayoutChild = androidPreferenceLayoutChild;
		prefResources.advancedColorPrefTextResources = advancedColorPrefTextResources;

		this.root = manager.createPreferenceScreen(activity);
	}

	public void addPreference(Pref pref){
		addPreference(pref, false);
	}

	public void addPreference(Pref pref, boolean indented){
		root.addPreference(pref.create(manager, activity, sharedPrefs, indented, prefResources, listToAddAllPrefsTo));
	}
	
	public void addPreference(DialogPref pref, boolean indented, int message,
			DialogPref.DialogPrefListener dialogPrefListener){
		root.addPreference(pref.create(manager, activity, sharedPrefs, indented, prefResources,
				message, dialogPrefListener, listToAddAllPrefsTo));
	}
	
	public void addPreference(PreferenceCategory category){
		root.addPreference(category);
	}
	
	public void addPreference(Preference preference){
		root.addPreference(preference);
	}
}
