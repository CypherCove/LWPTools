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
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import com.cyphercove.lwptools.core.ColorUtil;

public class ColorPref extends Pref implements ColorPickerDialog.OnColorPrefChangedListener{

	public int def;
	
	PreferenceScreen prefScreen;
	int summary;
	String summaryLabel;
	
	public ColorPref(String key, int def, SummaryMode summaryMode, int title, int summaryIfApplicable, String summaryLabel){
		this.key = key;
		this.def = def;
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.summaryLabel = summaryLabel;
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
				new ColorPickerDialog(context, ColorPref.this, sharedPrefs.getInt(key, def),
						key, resources.chooseColorString).show();
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
			case String:
				prefScreen.setSummary(summary);
				break;
			case Color:
				prefScreen.setSummary(getColorSummary(false,sharedPrefs));
				break;
			case LabeledColor:
				prefScreen.setSummary(getColorSummary(true,sharedPrefs));
				break;
			case ColorWithString:
				prefScreen.setSummary(
						TextUtils.concat(getColorSummary(false,sharedPrefs), " ", context.getString(summary)));
				break;
			case LabeledColorWithString:
				prefScreen.setSummary(
						TextUtils.concat(getColorSummary(true,sharedPrefs), " ", context.getString(summary)));
				break;
			default:
				logUnsupportedSummaryWarning();
				break;
			}
		}
	}
	
	public CharSequence getColorSummary(boolean labeled,SharedPreferences sharedPrefs) {
		int color = getValue(sharedPrefs);
		return createSingleColorSummary(color, labeled? summaryLabel : null);
	}
	
	public static CharSequence createSingleColorSummary(int color){
		return createSingleColorSummary(color,null);
	}
	
	public static CharSequence createSingleColorSummary(int color, String label){
		Spannable summary = new SpannableString ( label!=null ? "  "+label+"  " : "      " );
		
		summary.setSpan( new BackgroundColorSpan( color ), 0, summary.length(), 0 );
		
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		summary.setSpan( new ForegroundColorSpan( hsv[2]<0.30f ? Color.WHITE : Color.BLACK ), 0, summary.length(), 0 );
		
		return summary;
	}
	
	public static CharSequence createBlendedColorSummary(int start, int end, String label){
		if (label == null)
			label = " ";
		//space, then label, then 10 blend spaces, then 2 of end color
		Spannable summary = new SpannableString ( " " + label + "          " + "  ");
		int[] blendColors = new int[11]; //[0] won't be used because same color as start color
		for (int i=0;i<blendColors.length;i++){
			float fraction = (float)i/(float)blendColors.length;
			blendColors[i]=ColorUtil.blendAndroidIntsPreservingLerpedSaturation(start, end, fraction);
		}
		
		summary.setSpan( new BackgroundColorSpan( start ), 0, 1+label.length(), 0 );
		for (int i=1;i<blendColors.length;i++){
			int first = label.length() + i;
			summary.setSpan( new BackgroundColorSpan( blendColors[i] ), first, first+1, 0 );
		}
		summary.setSpan( new BackgroundColorSpan( end ), summary.length()-2, summary.length(), 0 );
		
		float[] hsv = new float[3];
		Color.colorToHSV(start, hsv);
		summary.setSpan( new ForegroundColorSpan( hsv[2]<0.30f ? Color.WHITE : Color.BLACK ), 0, summary.length(), 0 );
		
		return summary;
	}
	
	public static CharSequence createNarrowBlendedColorSummary(int start, int end, String label){
		if (label == null)
			label = " ";
		//space, then label, then 5 blend spaces, then 1 of end color
		Spannable summary = new SpannableString ( " " + label + "     " + " ");
		int[] blendColors = new int[6]; //[0] won't be used because same color as start color
		for (int i=0;i<blendColors.length;i++){
			float fraction = (float)i/(float)blendColors.length;
			blendColors[i]= ColorUtil.blendAndroidIntsPreservingLerpedSaturation(start, end, fraction);
		}
		
		summary.setSpan( new BackgroundColorSpan( start ), 0, 1+label.length(), 0 );
		for (int i=1;i<blendColors.length;i++){
			int first = label.length() + i;
			summary.setSpan( new BackgroundColorSpan( blendColors[i] ), first, first+1, 0 );
		}
		summary.setSpan( new BackgroundColorSpan( end ), summary.length()-1, summary.length(), 0 );
		
		float[] hsv = new float[3];
		Color.colorToHSV(start, hsv);
		summary.setSpan( new ForegroundColorSpan( hsv[2]<0.30f ? Color.WHITE : Color.BLACK ), 0, summary.length(), 0 );
		
		return summary;
	}
	
	public static CharSequence createTripleBlendedColorSummary(int start, int mid, int end, String label){
		CharSequence summary = 
			TextUtils.concat(createNarrowBlendedColorSummary(start,mid,label),createNarrowBlendedColorSummary(mid,end,""));
		return summary;
	}

	public int getValue(SharedPreferences sharedPrefs){
		return sharedPrefs.getInt(key, def);
	}

	@Override
	public int getValueAsInt(SharedPreferences sharedPrefs){
		return getValue(sharedPrefs);
	}

	@Override
	public void onColorPrefChanged(int color, String key) {
		Editor editor = getSharedPrefs().edit();
    	editor.putInt(key, color);
    	editor.commit();
	}

	public void resetToDefault(){
		if (getSharedPrefs()==null)
			logPrefMethodCalledTooSoonWarning();
		Editor editor = getSharedPrefs().edit();
		editor.putInt(key, def);
		editor.commit();
	}

	@Override
	public String getValueAsString(SharedPreferences sharedPrefs) {
		return String.valueOf(sharedPrefs.getInt(key, def));
	}

	@Override
	public void setValueInBatch(Editor editor, String value) {
		editor.putInt(key, Integer.valueOf(value));
	}


	@Override
	public ValueType getValueType() {
		return ValueType.INT;
	}
}
