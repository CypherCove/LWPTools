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

package com.cyphercove.lwptools.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

/** Helper class for creating a live wallpaper's INFO Activity. Forwards user to the wallpaper picker.
 */
public abstract class InfoActivityBase extends Activity {

	protected abstract int getWallpaperChooserToastStringResource();
	protected abstract Class getWallpaperServiceClass();
	protected abstract int getNoLiveWallpapersToastStringResource();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!checkForWallpaperChooser()){
			Toast.makeText(getApplicationContext(),
					getNoLiveWallpapersToastStringResource(),
					Toast.LENGTH_LONG)
					.show();
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
			openWallpaperChooser();
		} else {
			openWallpaperPreview();
		}

		finish();
	}

	private boolean checkForWallpaperChooser() {
		Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		return intent.resolveActivity(getPackageManager()) != null;
	}

	private void openWallpaperChooser() {
		Toast.makeText(getApplicationContext(),
				getWallpaperChooserToastStringResource(),
				Toast.LENGTH_LONG)
				.show();

		Intent i = new Intent();
		i.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		startActivity(i);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openWallpaperPreview() {
		String packageName = getPackageName();
		ComponentName componentName = new ComponentName(packageName,
				getWallpaperServiceClass().getCanonicalName());

		Intent i = new Intent();
		i.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
		i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName);
		startActivity(i);
	}

}