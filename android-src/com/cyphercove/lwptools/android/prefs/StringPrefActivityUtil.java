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

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StringPrefActivityUtil{
	
	public static String getPrefKey(Activity activity){
		return activity.getIntent().getExtras().getString(StringFromActivityPref.EXTRA_KEY);
	}

	public static String getPrefCurrentValue(Activity activity){
		String def = activity.getIntent().getExtras().getString(StringFromActivityPref.EXTRA_CURRENT);
		return PreferenceManager.getDefaultSharedPreferences(
				activity.getApplicationContext()).getString(getPrefKey(activity), def);
	}

	public static String getPrefExtraInfo(Activity activity){
		return activity.getIntent().getExtras().getString(StringFromActivityPref.EXTRA_INFO);
	}
	
	public static void commitNewValue(Activity activity, String newValue){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).edit();
		editor.putString(getPrefKey(activity), newValue);
		editor.commit();
	}
	
}
