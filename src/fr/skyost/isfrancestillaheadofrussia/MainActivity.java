package fr.skyost.isfrancestillaheadofrussia;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import fr.skyost.isfrancestillaheadofrussia.Parser.Country;
import fr.skyost.isfrancestillaheadofrussia.Parser.ParserListener;
import fr.skyost.isfrancestillaheadofrussia.utils.DefaultClickableSpan;
import fr.skyost.isfrancestillaheadofrussia.utils.LoadingImageView;

public class MainActivity extends Activity implements ParserListener {
	
	private Country[] countries;
	private static SpannableString spannableFooter;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		((LoadingImageView)this.findViewById(R.id.main_imageview_refresh)).setOnClickListener(new OnClickListener() {

			@Override
			public final void onClick(final View view) {
				refresh();
			}
			
		});
		final Typeface font = Typeface.createFromAsset(this.getAssets(), "OpenSans-Regular.ttf");
		final TextView response = (TextView)this.findViewById(R.id.main_textview_response);
		response.setOnClickListener(new OnClickListener() {

			@Override
			public final void onClick(final View view) {
				String text = response.getText().toString();
				if(text.equals(MainActivity.this.getString(R.string.main_textfield_response_yes))) {
					text = MainActivity.this.getString(R.string.main_share_message_yes, countries[0].name, countries[0].toString(MainActivity.this), countries[1].name, countries[1].toString(MainActivity.this));
				}
				else if(text.equals(MainActivity.this.getString(R.string.main_textfield_response_no))) {
					text = MainActivity.this.getString(R.string.main_share_message_no, countries[1].name, countries[1].toString(MainActivity.this), countries[0].name, countries[0].toString(MainActivity.this));
				}
				else {
					refresh();
					return;
				}
				final Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, text);
				MainActivity.this.startActivity(Intent.createChooser(share, MainActivity.this.getString(R.string.main_share_title)));
			}
			
		});
		response.setTypeface(font, Typeface.BOLD);
		final String source = this.getString(R.string.ranking_source);
		final String author = this.getString(R.string.app_author);
		if(spannableFooter == null) {
			final int year = Parser.getYear();
			spannableFooter = new SpannableString(this.getString(R.string.main_textfield_footer, source, year - 1, year, author));
			spannableFooter.setSpan(new DefaultClickableSpan(this, Parser.getSource()), spannableFooter.toString().indexOf(source), spannableFooter.toString().indexOf(source) + source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannableFooter.setSpan(new DefaultClickableSpan(this, "http://www.skyost.eu"), spannableFooter.toString().indexOf(author), spannableFooter.toString().indexOf(author) + author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		final TextView textViewFooter = (TextView)this.findViewById(R.id.main_textview_footer);
		textViewFooter.setMovementMethod(LinkMovementMethod.getInstance());
		textViewFooter.setTypeface(font);
		if(savedInstanceState == null || savedInstanceState.getSerializable("country-one") == null || savedInstanceState.getSerializable("country-two") == null) {
			refresh();
		}
		else {
			onParseCompleted((Country)savedInstanceState.getSerializable("country-one"), (Country)savedInstanceState.getSerializable("country-two"));
		}
	}
	
	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if(countries != null && countries.length == 2) {
			outState.putSerializable("country-one", countries[0]);
			outState.putSerializable("country-two", countries[1]);
		}
	}
	
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()) {
		case R.id.main_menu_refresh:
			refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Refresh the results.
	 */
	
	public final void refresh() {
		loadingAnimation(true);
		countries = null;
		setFooter(spannableFooter);
		new Parser(this).execute(this);
	}
	
	@Override
	public final void onParseCompleted(final Country... countries) {
		loadingAnimation(false);
		if(countries == null || countries.length != 2) {
			return;
		}
		this.countries = countries;
		final boolean isAhead = countries[0].ranking < countries[1].ranking;
		setResponse(this.getString(isAhead ? R.string.main_textfield_response_yes : R.string.main_textfield_response_no), 60f, isAhead);
		setFooter(countries[0].toString(this) + ". " + countries[1].toString(this) + ". ", spannableFooter);
	}
	
	@Override
	public final void onParseFailed(final Exception ex) {
		loadingAnimation(false);
		ex.printStackTrace();
		setResponse(this.getString(R.string.main_textfield_response_error, ex.getClass().getName()), 20f, false);
	}
	
	/**
	 * Shows a response on the screen.
	 * 
	 * @param response The response.
	 * @param size The font' size.
	 * @param good If it is good, the response will be displayed in green. Otherwise it is red.
	 */
	
	public final void setResponse(final String response, final float size, final boolean good) {
		final TextView textViewResponse = (TextView)this.findViewById(R.id.main_textview_response);
		textViewResponse.setText(response);
		textViewResponse.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		textViewResponse.setTextColor(good ? Color.parseColor("#2ECC71") : Color.parseColor("#E74C3C"));
	}
	
	public final void setFooter(final CharSequence... text) {
		((TextView)this.findViewById(R.id.main_textview_footer)).setText(text.length == 1 ? text[0] : TextUtils.concat(text));
	}
	
	public final void loadingAnimation(final boolean start) {
		if(start) {
			((LoadingImageView)this.findViewById(R.id.main_imageview_refresh)).startLoadingAnimation();
			return;
		}
		((LoadingImageView)this.findViewById(R.id.main_imageview_refresh)).stopLoadingAnimation();
	}

}