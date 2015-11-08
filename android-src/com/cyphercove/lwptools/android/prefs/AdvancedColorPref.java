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
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class AdvancedColorPref extends Pref implements AdvancedColorPickerDialog.OnAdvancedColorPrefChangedListener {

	public String def;

	PreferenceScreen prefScreen;
	int summary;
	int numberOfBatteryBasedColors;
	boolean usePrefTitleAsDialogTitle = true;
	LinkedHashMap<String, Integer> additionalModeValues;
	
	/**
	 * @param additionalModeValues ordered hash map of Strings (with their user-visible integer string resources) that are alternate options
	 * and will override any advanced color chosen.
	 */
	public AdvancedColorPref(String key, String def, SummaryMode summaryMode, int title, int summaryIfApplicable, int numberOfBatteryBasedColors, 
			LinkedHashMap<String, Integer> additionalModeValues, boolean usePrefTitleAsDialogTitle){
		this.key = key;
		this.def = def;
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		
		if (numberOfBatteryBasedColors<2)
			throw new IllegalArgumentException("Number of battery colors must be at least 2");
		this.numberOfBatteryBasedColors = numberOfBatteryBasedColors;
		
		this.additionalModeValues = additionalModeValues;
		
		this.usePrefTitleAsDialogTitle = usePrefTitleAsDialogTitle;
	}
	
	public AdvancedColorPref(String key, String def, SummaryMode summaryMode, int title, int summaryIfApplicable, int numberOfBatteryBasedColors, 
			LinkedHashMap<String, Integer> additionalModeValues){
		this(key,def,summaryMode,title,summaryIfApplicable,numberOfBatteryBasedColors,additionalModeValues,true);
	}

	public AdvancedColorPref(String key, String def, SummaryMode summaryMode, int title, int summaryIfApplicable, int numberOfBatteryBasedColors){
		this(key,def,summaryMode,title,summaryIfApplicable,numberOfBatteryBasedColors,null);
	}

	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								   boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setTitle(title);
		prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				new AdvancedColorPickerDialog(context, AdvancedColorPref.this, sharedPrefs.getString(key, def), numberOfBatteryBasedColors,
						key, resources.advancedColorPrefTextResources, additionalModeValues, (usePrefTitleAsDialogTitle?title:0)).show();
				return false;
			}
		});

		updateSummary(context, sharedPrefs);

		if (indented)
			prefScreen.setLayoutResource(resources.androidPreferenceLayoutChild);

		if (listToAddTo!=null)
			listToAddTo.add(this);

		return prefScreen;
	}

	public void updateSummary(Context context, SharedPreferences sharedPrefs){
		if (prefScreen==null){
			logSummaryUpdatedTooSoonWarning();
		} else {
			switch (summaryMode){
			case NoneOrCustom:
				updateCustomSummary();
				break;
			case AdvancedColor:
				prefScreen.setSummary(getAdvancedColorSummary(context, sharedPrefs));
				break;
			case AdvancedColorWithString:
				prefScreen.setSummary(
						TextUtils.concat(getAdvancedColorSummary(context, sharedPrefs), " ", context.getString(summary)));
				break;
			default:
				logUnsupportedSummaryWarning();
				break;
			}
		}
	}

	public CharSequence getAdvancedColorSummary(Context context, SharedPreferences sharedPrefs){
		String value = getValue(sharedPrefs);
		if (additionalModeValues!=null &&
				additionalModeValues.containsKey(value)){
			return context.getText(additionalModeValues.get(value));
		}

		AdvancedColor advColor = AdvancedColor.fromString(value);

        CharSequence response = null;
        try {
            response = createAdvancedColorSummary(advColor, numberOfBatteryBasedColors);
        } catch (ArrayIndexOutOfBoundsException e){
            String debugMsg = advColor.toDebugString();
            debugMsg = debugMsg + "\nNumber of battery based colors: " + numberOfBatteryBasedColors + ".";
            Log.e("AdvancedColorPref", e.getMessage());
            Log.e("AdvancedColorPref", debugMsg);
        }

		return (response == null) ? "" : response;
	}

	public static CharSequence createAdvancedColorSummary(AdvancedColor advColor, int numberOfBatteryBasedColors){
        if (advColor.getColorType()==AdvancedColor.ColorType.NOT_A_COLOR){
            return advColor.toString();
        }
		int[] colors = advColor.getColors();
        if (colors.length<=1){
            //safety check
            advColor.setColorType(AdvancedColor.ColorType.CONSTANT);
        }
        numberOfBatteryBasedColors = Math.min(numberOfBatteryBasedColors, colors.length); //safety check
		switch (advColor.getColorType()){
		case AdvancedColor.ColorType.CONSTANT:
			return ColorPref.createSingleColorSummary(colors[0]);
		case AdvancedColor.ColorType.BATTERY_LEVEL_BASED:
			if (numberOfBatteryBasedColors==2){
				return ColorPref.createBlendedColorSummary(colors[0], colors[1], null);
			} else {
				return ColorPref.createTripleBlendedColorSummary(colors[0], colors[1], colors[2], null);
			}
		case AdvancedColor.ColorType.PLUGGED_STATE_BASED:
			return TextUtils.concat(
					ColorPref.createSingleColorSummary(colors[0]), ColorPref.createSingleColorSummary(colors[1]));
		default:
			return "";
		}
	}

	public String getValue(SharedPreferences sharedPrefs){
		return sharedPrefs.getString(key, def);
	}

	/**
	 *
	 * @param sharedPrefs
	 * @return a newly created AdvancedColor instance.
	 */
	public AdvancedColor getAdvancedColor(SharedPreferences sharedPrefs){
		return AdvancedColor.fromString(sharedPrefs.getString(key, def));
	}

	@Override
	public void onAdvancedColorPrefChanged(String advancedColor, String key) {
		Editor editor = getSharedPrefs().edit();
		editor.putString(key, advancedColor);
		editor.commit();
	}

	public void resetToDefault(){
		if (getSharedPrefs()==null)
			logPrefMethodCalledTooSoonWarning();
		Editor editor = getSharedPrefs().edit();
		editor.putString(key, def);
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
