package fr.skyost.isfrancestillaheadofrussia.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class LoadingImageView extends ImageView implements AnimationListener {
	
	private final RotateAnimation animation = new RotateAnimation(.0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
	private boolean isRunning = false;
	private boolean stop = false;
	
	public LoadingImageView(final Context context) {
		super(context);
		setDefaultAnimationProperties();
	}
	
	public LoadingImageView(final Context context, final AttributeSet attributes) {
		super(context, attributes);
		setDefaultAnimationProperties();
	}
	
	public LoadingImageView(final Context context, final AttributeSet attributes, final int defaultStyle) {
		super(context, attributes, defaultStyle);
		setDefaultAnimationProperties();
	}
	
	private final void setDefaultAnimationProperties() {
		animation.setAnimationListener(this);
		animation.setDuration(1500L);
		animation.setRepeatCount(0);
	}
	
	public final void startLoadingAnimation() {
		if(!isRunning && !stop) {
			this.startAnimation(animation);
		}
		stop = false;
	}
	
	public final void stopLoadingAnimation() {
		stop = true;
	}
	
	@Override
	public final void onAnimationStart(final Animation animation) {
		isRunning = true;
	}

	@Override
	public final void onAnimationEnd(final Animation animation) {
		isRunning = false;
		startLoadingAnimation();
	}

	@Override
	public final void onAnimationRepeat(final Animation animation) {}

}