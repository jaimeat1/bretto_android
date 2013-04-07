package com.bretto.xbretto.fragments;

import com.bretto.xbretto.MainActivity;
import com.bretto.xbretto.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class HomeFragment extends Fragment {
	
	private Activity mActivity;
	private boolean mLaid = false;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.home_layout, container, false);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Fragment myFragment = this;

		this.getView().getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					
				@Override
				public void onGlobalLayout() {
					
					View climateBtn = myFragment.getView().findViewById(R.id.climateButton);
					View engineBtn = myFragment.getView().findViewById(R.id.engineButton);
					View ignitionBtn = myFragment.getView().findViewById(R.id.ignitionButton);
					
					// Second time onGlobalLayout() is called, remove listener
					// Avoid a strange behavior laying these buttons, they appear in the corner before be laid properly
					// Now, they are invisible, and become visible when they are in the right position (second time onGlobalLayout() is called)
					if (mLaid){
						// make sure it is not called anymore
						myFragment.getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
						climateBtn.setVisibility(View.VISIBLE);
						engineBtn.setVisibility(View.VISIBLE);
						ignitionBtn.setVisibility(View.VISIBLE);
						mLaid = false;
						return;
					}

					// Resize and relocate central buttons
					// First, buttons must have same total width than escreen
					// Then, relocate buttons in the middle of screen
					
					DisplayMetrics displaymetrics = new DisplayMetrics();
					mActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
					int screenWidth = displaymetrics.widthPixels;
					Log.d(MainActivity.APP_TAG,"screen width "+screenWidth);
/*					
					int difference = screenWidth - (sensorBtn.getWidth() + locationBtn.getWidth() + saveBtn.getWidth());
					Log.d(MainActivity.APP_TAG,"difference "+difference);
					
					// First, buttons must have same total width than escreen
					if (difference != 0){
						
						int offset = difference / 2;
						
						// Distribute difference between left and right buttons
						RelativeLayout.LayoutParams sensorParams = (RelativeLayout.LayoutParams) sensorBtn.getLayoutParams();
						Log.d(MainActivity.APP_TAG,"sensor width "+sensorParams.width);
						sensorParams.width += offset;
						Log.d(MainActivity.APP_TAG,"new sensor width "+sensorParams.width);
						sensorBtn.setLayoutParams(sensorParams);
						
						RelativeLayout.LayoutParams saveParams = (RelativeLayout.LayoutParams) saveBtn.getLayoutParams();
						Log.d(MainActivity.APP_TAG,"save width "+sensorParams.width);
						saveParams.width += offset;
						Log.d(MainActivity.APP_TAG,"new save width "+saveParams.width);
						saveBtn.setLayoutParams(saveParams);
						
						// It's not a even difference 
						if ((difference % 2) != 0) {
							// difference positive
							if (offset > 0){
								saveParams.width += 1;
							// difference negative
							} else {
								saveParams.width -= 1;
							}
							saveBtn.setLayoutParams(saveParams);
						}
					}
*/					
					// Then, relocate buttons in the middle of screen
					
					View assembleOn = myFragment.getView().findViewById(R.id.assembleOnButton);
					int heightAssemble = assembleOn.getHeight();

					int heightClimate = climateBtn.getHeight();

					int climateBtnX = 0;
					int climateBtnY = ((heightAssemble * 2) - heightClimate) / 2;
					
					//int locationBtnX = sensorBtnX + sensorBtn.getWidth();
					int engineBtnX = (screenWidth/2) - (engineBtn.getWidth()/2);
					int engineBtnY = climateBtnY;
					
					//int saveBtnX = locationBtnX + locationBtn.getWidth();
					int ignitionBtnX = screenWidth - ignitionBtn.getWidth();
					int ignitionBtnY = climateBtnY;
					
					RelativeLayout.LayoutParams climateParams = (RelativeLayout.LayoutParams) climateBtn.getLayoutParams();
					climateParams.leftMargin = climateBtnX;
					climateParams.topMargin = climateBtnY;
					climateBtn.setLayoutParams(climateParams);
					
					RelativeLayout.LayoutParams engineParams = (RelativeLayout.LayoutParams) engineBtn.getLayoutParams();
					engineParams.leftMargin = engineBtnX;
					engineParams.topMargin = engineBtnY;
					engineBtn.setLayoutParams(engineParams);
					
					RelativeLayout.LayoutParams ignitionParams = (RelativeLayout.LayoutParams) ignitionBtn.getLayoutParams();
					ignitionParams.leftMargin = ignitionBtnX;
					ignitionParams.topMargin = ignitionBtnY;
					ignitionBtn.setLayoutParams(ignitionParams);
					
					mLaid = true;
				}
				
		});
		
	} // end of onActivityCreated
  
}
