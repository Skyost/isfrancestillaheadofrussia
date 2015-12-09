package fr.skyost.isfrancestillaheadofrussia;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
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
import fr.skyost.isfrancestillaheadofrussia.utils.DefaultClickableSpan;

public class MainActivity extends Activity {
	
	private Country[] countries;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		final Resources resources = this.getResources();
		final Typeface font = Typeface.createFromAsset(this.getAssets(), "OpenSans-Regular.ttf");
		final TextView response = (TextView)this.findViewById(R.id.main_textview_response);
		response.setOnClickListener(new OnClickListener() {

			@Override
			public final void onClick(final View view) {
				String text = response.getText().toString();
				if(text.equals(resources.getString(R.string.main_textfield_response_yes))) {
					text = resources.getString(R.string.main_share_message_yes, countries[0].name, countries[0].toString(resources), countries[1].name, countries[1].toString(resources));
				}
				else if(text.equals(resources.getString(R.string.main_textfield_response_no))) {
					text = resources.getString(R.string.main_share_message_no, countries[1].name, countries[1].toString(resources), countries[0].name, countries[0].toString(resources));
				}
				else {
					return;
				}
				final Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, text);
				MainActivity.this.startActivity(Intent.createChooser(share, resources.getString(R.string.main_share_title)));
			}
			
		});
		response.setTypeface(font, Typeface.BOLD);
		final String source = resources.getString(R.string.ranking_source);
		final String author = resources.getString(R.string.app_author);
		final SpannableString spannableFooter = new SpannableString(this.getResources().getString(R.string.main_textfield_footer, source, author));
		spannableFooter.setSpan(new DefaultClickableSpan(this, Parser.getSource()), spannableFooter.toString().indexOf(source), spannableFooter.toString().indexOf(source) + source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannableFooter.setSpan(new DefaultClickableSpan(this, "http://www.skyost.eu"), spannableFooter.toString().indexOf(author), spannableFooter.toString().indexOf(author) + author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		final TextView textViewFooter = (TextView)this.findViewById(R.id.main_textview_footer);
		textViewFooter.setText(spannableFooter);
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
	
	public final void refresh() {
		new Parser(this).execute(new Void[0]);
	}

	public final void onParseCompleted(final Country... countries) {
		if(countries == null || countries.length != 2) {
			return;
		}
		this.countries = countries;
		final Resources resources = this.getResources();
		final boolean isAhead = countries[0].ranking < countries[1].ranking;
		setResponse(resources.getString(isAhead ? R.string.main_textfield_response_yes : R.string.main_textfield_response_no), 60f, isAhead);
		final TextView footer = (TextView)this.findViewById(R.id.main_textview_footer);
		footer.setText(TextUtils.concat(countries[0].toString(resources) + ". " + countries[1].toString(resources) + ". ", footer.getText()));
		System.out.println(footer.getText());
	}
	
	public final void setResponse(final String response, final float size, final boolean good) {
		final TextView textViewResponse = (TextView)this.findViewById(R.id.main_textview_response);
		textViewResponse.setText(response);
		textViewResponse.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		textViewResponse.setTextColor(good ? Color.parseColor("#2ECC71") : Color.parseColor("#E74C3C"));
	}

}