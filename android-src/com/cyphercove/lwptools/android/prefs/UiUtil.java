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
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public final class UiUtil {

	@SuppressWarnings("deprecation")
	public static boolean isTablet(Activity activity){
		DisplayMetrics dm = activity.getResources().getDisplayMetrics();
		
		Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		float widthInches = display.getWidth()/dm.xdpi;
		float heightInches = display.getHeight()/dm.ydpi;
		
		return (widthInches>3.4f && heightInches>3.4f);
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isWidescreen(Activity activity){
		Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		int width = display.getWidth();
		int height = display.getHeight();
		
		return width>height;
	}
}
