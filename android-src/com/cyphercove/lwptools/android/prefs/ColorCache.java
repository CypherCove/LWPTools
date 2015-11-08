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
import android.graphics.Color;

public class ColorCache {
	public static final String PREFS_NAME = "com.ColorCache";
	
	private static final int COLOR_DEFAULT = Color.BLACK;
	
	private static final int CACHE_SIZE = 20;
	
	private static final String BASE_KEY = "color"; //each key is this string concatenated with the integer index
	
	private static SharedPreferences getSharedPreferences(Context context){
		return context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
	}
	
	private static SharedPreferences.Editor getEditor(Context context){
		return getSharedPreferences(context).edit();
	}
	
	public static ArrayList<Integer> getCachedColors(Context context){
		ArrayList<Integer> list = new ArrayList<Integer>(CACHE_SIZE);
		SharedPreferences sharedPrefs = getSharedPreferences(context);
		for (int i=0; i<CACHE_SIZE; i++){
			int nextColor = sharedPrefs.getInt(BASE_KEY+i, COLOR_DEFAULT);
			list.add(nextColor);
		}
		return list;
	}
	
	public static boolean submitNewColors(Context context, Integer... colors){
		ArrayList<Integer> list = getCachedColors(context);
		
		boolean listChanged = false;
		for (Integer color : colors){
			if (list.get(0).equals(color)){
				//the color is already the first in the list. Nothing to do.
				continue;
			}
			
			//remove the color if it is in the list, because it will be moved to front
			list.remove(color);
			
			//add the color in the first index
			list.add(0, color);
			
			listChanged = true;
		}
		
		//save the revised list
		if (listChanged){
			SharedPreferences.Editor editor = getEditor(context);
			for (int i=0; i<CACHE_SIZE; i++){
				editor.putInt(BASE_KEY+i, list.get(i));
			}
			editor.commit();
		}
		
		return listChanged;
		
		
	}
	
}
