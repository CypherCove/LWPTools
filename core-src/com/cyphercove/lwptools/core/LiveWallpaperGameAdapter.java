package com.cyphercove.lwptools.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class LiveWallpaperGameAdapter extends Game{

	LiveWallpaperBaseRenderer liveWallpaper;
	MultiTapListener multiTapListener;

	//Screen tapping
	int tapCount=0;
	float timeSinceFirstTap=0;
	public static float timeToDoubleTap = 0.35f;
	public static float timeToTripleTap = 0.6f;
	float firstTapX;
	float firstTapY;
	static final float tapThreshold = 80;

	private long currentTimeMillis=0;
	private long deltaTimeMillis=0;
	private int deltaMillisMin=1;

	public LiveWallpaperGameAdapter(LiveWallpaperBaseRenderer liveWallpaper, MultiTapListener multiTapListener, int maxFPS){
		this.liveWallpaper = liveWallpaper;
		liveWallpaper.setIsPreview(false);
		this.multiTapListener = multiTapListener;
		setFPSLimit(maxFPS);
	}
	
	public void setFPSLimit(int limit){
		deltaMillisMin=1000/limit;
	}
	
	/**Call once at beginning of renderer's draw()*/
	public void updateTime () {
		currentTimeMillis = System.currentTimeMillis();
	}
	
	/**Call once at end of renderer's draw()*/
	public void limitFPS(){
		deltaTimeMillis = System.currentTimeMillis()-currentTimeMillis;
		if (deltaTimeMillis<deltaMillisMin){
			try {Thread.sleep(deltaMillisMin-deltaTimeMillis);}
			catch (InterruptedException e) {}
		}
	}
	
	@Override
	public void create() {
		Gdx.graphics.setVSync(true);
		liveWallpaper.create();
	}
	
	float xOffset = 0.5f;
	float xOffsetTarget = 0.5f;
	
	/**A version of xOffset that automatically pans based on finger swipes, ignoring launcher.*/
	protected float xOffsetFake=0.5f;
	float xOffsetFakeTarget=0.5f;
	static final float PIXEL_TO_XOFFSET_FAKE_RATIO = 750f;
	boolean fingerDown=false;
	private float offsetMaxVelocity=1.5f; //per second
	private float offsetMinVelocity=.4f; //per second
	
	boolean isTouched = false;
	
	@Override
	public void render() {
		updateTime();		
		
		if (Gdx.input.justTouched() && Gdx.input.getY()<100){
			if (Gdx.input.getX() < 100){
				xOffsetTarget = Math.max(xOffsetTarget-0.2f, 0);
			} else if (Gdx.input.getX() > (Gdx.graphics.getWidth()-100) ){
				xOffsetTarget = Math.min(xOffsetTarget+0.2f, 1);
			}
			MathUtils.clamp(xOffsetTarget, 0, 1);
		}
		xOffset += (xOffsetTarget-xOffset)*2*Gdx.graphics.getDeltaTime();
		
		handleFakeTouch();
		
		handleMultiTaps();
		
		liveWallpaper.draw(Gdx.graphics.getDeltaTime(), xOffsetFake, xOffset, xOffset, 0.5f);
		
		limitFPS();
	}
	
	private void handleMultiTaps() {
		if (Gdx.input.justTouched()){
			if (tapCount==0){
				timeSinceFirstTap=0;
				firstTapX = Gdx.input.getX();
				firstTapY = Gdx.input.getY();
				tapCount = 1;
			} else if (Math.abs(firstTapX - Gdx.input.getX())<=tapThreshold &&
					Math.abs(firstTapY - Gdx.input.getY())<=tapThreshold){
				tapCount++;
				if (tapCount==2 && timeSinceFirstTap<=timeToDoubleTap){
					liveWallpaper.onDoubleTap();
					if (multiTapListener!=null)
						multiTapListener.onDoubleTap();
				}
				if (tapCount==3){// no need to check time here because we wouldn't be here if not satisfied (see handleMultiTaps)
					liveWallpaper.onTripleTap();
					if (multiTapListener!=null)
						multiTapListener.onTripleTap();
				}	
			}
		}
				
		timeSinceFirstTap += Gdx.graphics.getDeltaTime();
		if (timeSinceFirstTap>=timeToTripleTap)
			tapCount=0;
	}

	void handleFakeTouch(){
		if (Gdx.input.justTouched()){
			fingerDown=true;
		}
		if (fingerDown){
			if (Gdx.input.getDeltaX()!=0){
				float swipeDelta = -Gdx.input.getDeltaX();
				xOffsetFakeTarget += swipeDelta/PIXEL_TO_XOFFSET_FAKE_RATIO;
				if (xOffsetFakeTarget<0)
					xOffsetFakeTarget=0;
				else if (xOffsetFakeTarget>1)
					xOffsetFakeTarget=1;
			}
		}
		
		if (!Gdx.input.isTouched())
			fingerDown=false;
		
		float offsetDelta = xOffsetFakeTarget-xOffsetFake;
		float offsetVelocity = offsetMaxVelocity*MathUtils.sin(offsetDelta*MathUtils.PI);
		if (offsetDelta<0){//moving left
			offsetVelocity-=offsetMinVelocity;
		} else if (offsetDelta>0){//moving right
			offsetVelocity+=offsetMinVelocity;
		} 
		float offsetAdder =offsetVelocity*Gdx.graphics.getDeltaTime();
		if (Math.abs(offsetAdder)>=Math.abs(offsetDelta)){
			xOffsetFake=xOffsetFakeTarget;
		} else {
			xOffsetFake+=offsetAdder;
		}
	}

	@Override
	public void resize(int width, int height){
		liveWallpaper.resize(width,height);
	}
	
	@Override
	public void pause() {
		liveWallpaper.pause();
	}

	@Override
	public void resume() {
		liveWallpaper.resume();
	}
	
	@Override
	public void dispose() {
		liveWallpaper.dispose();
	}
}
