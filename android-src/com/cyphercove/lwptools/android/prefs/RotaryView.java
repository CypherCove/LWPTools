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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class RotaryView extends View {

	private Paint paint;
	private float density;
	private OnAngleChangedListener listener;
	private int value = 0;
	private Path indicatorPath;

	float centerX, centerY, radius, radiusWithPadding;
	boolean tracking = false;
	float downX, downY;
	int downValue;

	private static final int DEFAULT_VIEW_SIZE_DP = 288;
	private static final int LINE_WIDTH_DP = 2;
	private static final int INDICATOR_WIDTH_DP = 8;
	private static final int INDICATOR_HEIGHT_DP = 24;
	private static final int BUILT_IN_PADDING_DP = 16; //Built-in 16dp padding to make it easier to touch
	private static final int COLOR = 0xffffffff;
	private static final int COLOR_PRESSED = 0xff808080;

	public interface OnAngleChangedListener{
		void onAngleChanged(int newAngle, View source);
	}

	public RotaryView(Context context) {
		this(context, null);
	}

	public RotaryView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RotaryView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		density = metrics.density;

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(LINE_WIDTH_DP*density);

		//The indicator is a narrow triangle
		indicatorPath = new Path();
		float indicatorWidth = density * INDICATOR_WIDTH_DP;
		float indicatorHeight = density * INDICATOR_HEIGHT_DP;
		indicatorPath.moveTo(-indicatorWidth/2, indicatorHeight);
		indicatorPath.lineTo(indicatorWidth/2, indicatorHeight);
		indicatorPath.lineTo(0, 0);
		indicatorPath.lineTo(-indicatorWidth/2, indicatorHeight);

	}

	public void setOnAngleChangedListener(OnAngleChangedListener listener){
		this.listener = listener;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthTarget = MeasureSpec.getSize(widthMeasureSpec);

		switch (widthMode){
		case MeasureSpec.UNSPECIFIED:
			widthTarget = (int)(DEFAULT_VIEW_SIZE_DP*density);
			break;
		case MeasureSpec.AT_MOST:
			widthTarget = Math.min((int)(DEFAULT_VIEW_SIZE_DP*density), widthTarget);
			break;
		}

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightTarget = MeasureSpec.getSize(heightMeasureSpec);

		switch (heightMode){
		case MeasureSpec.UNSPECIFIED:
			heightTarget = (int)(DEFAULT_VIEW_SIZE_DP*density);
			break;
		case MeasureSpec.AT_MOST:
			heightTarget = Math.min((int)(DEFAULT_VIEW_SIZE_DP*density), heightTarget);
			break;
		}

		centerX = widthTarget/2f;
		centerY = heightTarget/2f;
		radiusWithPadding = Math.min(centerX, centerY);
		radius = radiusWithPadding - 2*BUILT_IN_PADDING_DP;

		setMeasuredDimension(widthTarget, heightTarget);
	}

	@Override 
	protected void onDraw(Canvas canvas) {

		paint.setColor(COLOR);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(centerX, centerY, radius, paint);

		if (tracking){
			paint.setColor(COLOR_PRESSED);
		}
		paint.setStyle(Paint.Style.FILL);
		canvas.translate(centerX, centerY);
		canvas.rotate(value);
		canvas.translate(0, -radius);
		canvas.drawPath(indicatorPath, paint);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			requestFocus();
			float deltaX = x-centerX;
			float deltaY = y-centerY;
			double distanceFromCenter = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
			tracking = distanceFromCenter <= radiusWithPadding;
			downValue = value;
			if (tracking) {
				downX = x;
				downY = y;
				invalidate();
			} 
			break;
		case MotionEvent.ACTION_MOVE:
			if (tracking) {
				value = valueFromLines(downX-centerX, downY-centerY, x-centerX, y-centerY, downValue);
				invalidate();
			} 
			break;
		case MotionEvent.ACTION_UP:
			if (tracking) {
				value = valueFromLines(downX-centerX, downY-centerY, x-centerX, y-centerY, downValue);
				tracking = false;
				invalidate();
			} 
			break;
		}
		if (tracking && listener!=null){
			listener.onAngleChanged(value, this);
		}
		return true;
	}
	
	private static int valueFromLines(float x1, float y1, float x2, float y2, int touchDownValue){
		return (int)((touchDownValue+angleBetweenLines(x1, y1, x2, y2)) % 360);
	}
	
	private static float angleBetweenLines(float x1, float y1, float x2, float y2){
		float dot = x1*x2 + y1*y2;      // dot product
		float det = x1*y2 - y1*x2;      // determinant
		return (float)(Math.toDegrees(Math.atan2(det, dot))+360);
	}
}
