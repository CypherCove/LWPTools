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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PrefScreen extends Pref {
	
	int summary;
	PreferenceScreen preferenceScreen;
	PreferenceManager manager;
	
	public PrefScreen(SummaryMode summaryMode, int title, int summaryIfApplicable){
		this.key = "";
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
	}
	
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								   boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		try{
			this.sharedPrefs = sharedPrefs;
			this.manager = manager;
			preferenceScreen = manager.createPreferenceScreen(context);
			pref = preferenceScreen;
			preferenceScreen.setTitle(title);
			
			updateSummary(context, sharedPrefs);
			
			if (indented)
				preferenceScreen.setLayoutResource(resources.androidPreferenceLayoutChild);
			
			if (listToAddTo!=null)
				listToAddTo.add(this);
			
			return preferenceScreen;
		} catch (NullPointerException e){
			Log.e("PrefScreen", "Failed to create pref screen. Did you forget to call prepareForChildren() on the parent?");
			throw e;
		}
	}
	
	public void updateSummary(Context context, SharedPreferences sharedPrefs){
		if (preferenceScreen==null){
			logSummaryUpdatedTooSoonWarning();
		} else {
			switch (summaryMode){
			case NoneOrCustom:
				updateCustomSummary();
				break;
			case String:
				preferenceScreen.setSummary(summary);
				break;
			default:
				logUnsupportedSummaryWarning();
				break;
			}
		}
	}
	
	PreferenceActivity activity;
	SharedPreferences sharedPrefs;
	PrefResources prefResources;
	ArrayList<Pref> listToAddAllPrefsTo;

	public void prepareForChildren(PrefRoot rootToCopyFrom){
		this.activity=rootToCopyFrom.activity;
		this.sharedPrefs=rootToCopyFrom.sharedPrefs;
		this.prefResources = rootToCopyFrom.prefResources;
		this.listToAddAllPrefsTo = rootToCopyFrom.listToAddAllPrefsTo;
	}

	public void addPreference(Preference preference){
		preferenceScreen.addPreference(preference);
	}

	public void addPreference(Pref pref){
		addPreference(pref, false);
	}

	public void addPreference(Pref pref, boolean indented){
		preferenceScreen.addPreference(pref.create(manager, activity, sharedPrefs, indented, prefResources,
				listToAddAllPrefsTo));
	}
	
	public void addPreference(DialogPref pref, boolean indented, int message,
			DialogPref.DialogPrefListener dialogPrefListener){
		preferenceScreen.addPreference(pref.create(manager, activity, sharedPrefs, indented, prefResources,
				message, dialogPrefListener, listToAddAllPrefsTo));
	}

	@Override
	public void resetToDefault() {
		//do nothing
	}

	@Override
	public String getValueAsString(SharedPreferences sharedPrefs) {
		return "";
	}

	@Override
	public void setValueInBatch(Editor editor, String value) {
		
	}

	@Override
	public ValueType getValueType() {
		return ValueType.NONE;
	}

}
