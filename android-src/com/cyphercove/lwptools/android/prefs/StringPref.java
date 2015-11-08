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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class StringPref extends Pref{

	public String def;
	
	public String getValue(SharedPreferences sharedPrefs){
		return sharedPrefs.getString(key, def);
	}
	
	public void resetToDefault(){
		if (getSharedPrefs()==null)
			logPrefMethodCalledTooSoonWarning();
		Editor editor = getSharedPrefs().edit();
		editor.putString(key, def);
		editor.commit();
	}

	public void setValue(String value, SharedPreferences sharedPrefs){
		Editor editor = sharedPrefs.edit();
		editor.putString(key, value);
		editor.commit();
	}

	@Override
	public String getValueAsString(SharedPreferences sharedPrefs) {
		return sharedPrefs.getString(key, def);
	}

	@Override
	public void setValueInBatch(Editor editor, String value) {
		editor.putString(key, value);
	}

	@Override
	public ValueType getValueType() {
		return ValueType.STRING;
	}
}
