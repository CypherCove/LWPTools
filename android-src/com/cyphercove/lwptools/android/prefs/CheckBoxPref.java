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
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;

public class CheckBoxPref extends Pref {

	public boolean def;

	int summary;
	CheckBoxPreference checkBoxPref;

	public CheckBoxPref(String key, boolean def, SummaryMode summaryMode, int title, int summaryIfApplicable){
		this.key = key;
		this.def = def;
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
	}

	public CheckBoxPreference create(PreferenceManager manager, Context context, SharedPreferences sharedPrefs,
									 boolean indented, PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		checkBoxPref = new CheckBoxPreference(context);
		pref = checkBoxPref;
		checkBoxPref.setKey(key);
		checkBoxPref.setDefaultValue(def);
		checkBoxPref.setTitle(title);

		updateSummary(context, sharedPrefs);

		if (indented)
			checkBoxPref.setLayoutResource(resources.androidPreferenceLayoutChild);

		if (listToAddTo!=null)
			listToAddTo.add(this);

		return checkBoxPref;
	}

	public void updateSummary(Context context, SharedPreferences sharedPrefs){
		if (checkBoxPref==null){
			logSummaryUpdatedTooSoonWarning();
		} else {
			switch (summaryMode){
			case NoneOrCustom:
				updateCustomSummary();
				break;
			case String:
				checkBoxPref.setSummary(summary);
				break;
			default:
				logUnsupportedSummaryWarning();
				break;
			}
		}
	}

	public boolean getValue(SharedPreferences sharedPrefs){
		return sharedPrefs.getBoolean(key, def);
	}

	@Override
	public boolean getValueAsBoolean(SharedPreferences sharedPrefs){
		return getValue(sharedPrefs);
	}

	public void resetToDefault(){
		if (getSharedPrefs()==null)
			logPrefMethodCalledTooSoonWarning();
		Editor editor = getSharedPrefs().edit();
		editor.putBoolean(key, def);
		editor.commit();
	}

	@Override
	public String getValueAsString(SharedPreferences sharedPrefs) {
		return String.valueOf(sharedPrefs.getBoolean(key, def));
	}

	public void setValue(boolean value, SharedPreferences sharedPrefs){
		Editor editor = sharedPrefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	@Override
	public void setValueInBatch(Editor editor, String value) {
		editor.putBoolean(key,Boolean.valueOf(value));
	}

	@Override
	public ValueType getValueType() {
		return ValueType.BOOLEAN;
	}
}
