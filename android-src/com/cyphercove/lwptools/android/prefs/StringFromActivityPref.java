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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

/**Used to build a StringPreference whose value is set by an arbitrary Activity.
 *
 * Use a subclass of StringPrefActivity and call its convenience methods to obtain the current value of the preference
 * and to set the new value when one is selected by the user. Alternatively, an arbitrary Activity superclass can be
 * used, and the StringPrefActivityUtil can be used for the same purpose.
 */
public class StringFromActivityPref extends StringPref {
	
	PreferenceScreen prefScreen;
	int summary;
	Class<? extends Activity> pickerActivity;
	String infoForActivity;
	
	public static final String EXTRA_KEY = "com.cyphercove.lwptools.android.prefs.StringFromActivityPref#key";
	public static final String EXTRA_CURRENT = "com.cyphercove.lwptools.android.prefs.StringFromActivityPref#current";
	public static final String EXTRA_INFO = "com.cyphercove.lwptools.android.prefs.StringFromActivityPref#info";
	
	public StringFromActivityPref(String key, String def, SummaryMode summaryMode, int title, int summaryIfApplicable, 
			Class<? extends Activity> pickerActivity, String infoForActivity){
		this.key = key;
		this.def = def;
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.pickerActivity = pickerActivity;
		this.infoForActivity = infoForActivity;
	}
	
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								   boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setKey(key);
		prefScreen.setTitle(title);
		
		Intent intent = new Intent(context,pickerActivity);
		intent.putExtra(EXTRA_KEY, key);
		intent.putExtra(EXTRA_CURRENT, def);
		intent.putExtra(EXTRA_INFO, infoForActivity);
		prefScreen.setIntent(intent);
		
		updateSummary(context, sharedPrefs);
		
		if (indented)
			prefScreen.setLayoutResource(resources.androidPreferenceLayoutChild);
		
		if (listToAddTo!=null)
			listToAddTo.add(this);
		
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
			case Value:
				prefScreen.setSummary(sharedPrefs.getString(key, def).replace("%", "%%"));
				break;
			case ValueInFormattedString:
				prefScreen.setSummary(String.format(context.getString(summary),sharedPrefs.getString(key, def).replace("%", "%%")));
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
	
	public String getInfoForActivity(){
		return infoForActivity;
	}

}
