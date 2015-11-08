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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.util.ArrayList;

/** A Pref that has no key and is disabled by default*/
public class ExplanatoryPref extends StringPref {
	
	PreferenceScreen prefScreen;
	int summary;
	
	/**
	 * 
	 * @param summaryMode
	 * @param title Can be 0 to avoid showing a title.
	 * @param summaryIfApplicable
	 */
	public ExplanatoryPref(SummaryMode summaryMode, int title, int summaryIfApplicable){
		this.key = "_disabled_";
		this.def = "disabled";
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
	}
	
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								   boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		if (title!=0)
			prefScreen.setTitle(title);
		
		updateSummary(context, sharedPrefs);
		
		if (indented)
			prefScreen.setLayoutResource(resources.androidPreferenceLayoutChild);
		
		prefScreen.setEnabled(false);
		
		return prefScreen;
	}

	@Override
	public void updateSummary(Context context, SharedPreferences sharedPrefs) {
		if (prefScreen==null){
			logSummaryUpdatedTooSoonWarning();
		} else {
			switch (summaryMode){
			case NoneOrCustom:
				updateCustomSummary();
				break;
			case String:
				prefScreen.setSummary(summary);
				break;
			default:
				logUnsupportedSummaryWarning();
				break;
			}
		}
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
