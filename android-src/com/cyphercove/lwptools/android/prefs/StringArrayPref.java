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
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;

public class StringArrayPref extends StringPref {
	
	int arrayChoices;
	int arrayValues;
	int summary;
	ListPreference listPref;
	
	public StringArrayPref(String key, String def, SummaryMode summaryMode, int title, int summaryIfApplicable, int arrayChoices, int arrayValues){
		this.key = key;
		this.def = def;
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.arrayChoices = arrayChoices;
		this.arrayValues = arrayValues;
	}
	
	public ListPreference create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								 boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		listPref = new ListPreference(context);
		pref = listPref;
		listPref.setEntries(arrayChoices);
		listPref.setEntryValues(arrayValues);
		listPref.setDialogTitle(title);
		listPref.setKey(key);
		//TODO test this: pref.setDefaultValue(def);
		listPref.setTitle(title);
		
		updateSummary(context, sharedPrefs);
		
		if (indented)
			listPref.setLayoutResource(resources.androidPreferenceLayoutChild);
		
		if (listToAddTo!=null)
			listToAddTo.add(this);
		
		return listPref;
	}
	
	public void updateSummary(Context context, SharedPreferences sharedPrefs){
		if (listPref==null){
			logSummaryUpdatedTooSoonWarning();
		} else {
			switch (summaryMode){
			case NoneOrCustom:
				updateCustomSummary();
				break;
			case Value:
				listPref.setSummary(sharedPrefs.getString(key, def).replace("%", "%%"));
				break;
			case ValueInFormattedString:
				listPref.setSummary(String.format(context.getString(summary),sharedPrefs.getString(key, def).replace("%", "%%")));
				break;
			case ArrayValue:
				listPref.setSummary(getChoiceFromValue(context, sharedPrefs).replace("%", "%%"));
				break;
			case ArrayValueInFormattedString:
				listPref.setSummary(String.format(context.getString(summary),getChoiceFromValue(context, sharedPrefs).replace("%", "%%")));
				break;
			case String:
				listPref.setSummary(summary);
				break;
			default:
				logUnsupportedSummaryWarning();
				break;
			}
		}
	}
	
	protected String getChoiceFromValue(Context context, SharedPreferences sharedPrefs){
		String[] summaries = context.getResources().getStringArray(arrayChoices);
		String[] values = context.getResources().getStringArray(arrayValues);
		String value = getValue(sharedPrefs);
		
		for (int i=0; i<summaries.length; i++){
			if (value.equals(values[i])){
				return summaries[i];
			}
		}
		
		Log.w(this.getClass().getName(),"No choice found to match value. Returning blank choice.");
		return "";
	}

}
