package fr.skyost.isfrancestillaheadofrussia.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class DefaultClickableSpan extends ClickableSpan {
	
	private final Activity activity;
	private final String link;
	
	public DefaultClickableSpan(final Activity activity, final String link) {
		this.activity = activity;
		this.link = link;
	}

	@Override
	public final void onClick(final View textView) {
		activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
	}
	
	@Override
	public final void updateDrawState(final TextPaint ds) {
		super.updateDrawState(ds);
		ds.setUnderlineText(false);
		ds.setColor(Color.parseColor("#3498DB"));
	}

}
