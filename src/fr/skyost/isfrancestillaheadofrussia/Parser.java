package fr.skyost.isfrancestillaheadofrussia;

import java.io.Serializable;
import java.util.Locale;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import fr.skyost.isfrancestillaheadofrussia.Parser.Country;
import fr.skyost.isfrancestillaheadofrussia.Parser.ParserListener;

public class Parser extends AsyncTask<ParserListener, Void, Country[]> {

	private static final short YEAR = 2016;
	private static final String URL = "http://kassiesa.home.xs4all.nl/bert/uefa/data/method4/crank%d.html";

	private final Activity parent;
	private final Country countryOne;
	private final Country countryTwo;

	private final ProgressDialog dialog;
	private ParserListener[] listeners;
	private Exception ex;

	public Parser(final Activity parent) {
		this.parent = parent;
		countryOne = new Country(parent.getString(R.string.parser_countries_one), "France", -1, -1f);
		countryTwo = new Country(parent.getString(R.string.parser_countries_two), "Russia", -1, -1f);
		this.dialog = new ProgressDialog(parent);
	}

	@Override
	protected final void onPreExecute() {
		final String[] messages = parent.getResources().getStringArray(R.array.parser_dialog_messages_prefix);
		dialog.setTitle(R.string.parser_dialog_title);
		dialog.setMessage(String.format(messages[new Random().nextInt(messages.length)], parent.getString(R.string.parser_dialog_messages_suffix)));
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected final Country[] doInBackground(final ParserListener... listeners) {
		try {
			this.listeners = listeners;
			final Document document = Jsoup.connect(getSource()).get();
			final Elements ranking = document.select("tr td");
			for(int i = 0, currentRanking = 0; i != ranking.size(); i++) {
				final Element country = ranking.get(i);
				final String attr = country.attr("align");
				if(attr == null || !attr.equals("left")) {
					continue;
				}
				if(currentRanking == 0) {
					currentRanking++; // < 2015
					continue;
				}
				currentRanking++;
				final String name = country.text();
				if(name.equals(countryOne.scrappingName)) {
					countryOne.ranking = currentRanking - 1;
					countryOne.points = Float.parseFloat(ranking.get(i + 6).text());
				}
				else if(name.equals(countryTwo.scrappingName)) {
					countryTwo.ranking = currentRanking - 1;
					countryTwo.points = Float.parseFloat(ranking.get(i + 6).text());
				}
				if(countryOne.ranking != -1 && countryTwo.ranking != -1) {
					break;
				}
			}
			return new Country[]{countryOne, countryTwo};
		}
		catch(final Exception ex) {
			this.ex = ex;
		}
		return null;
	}

	@Override
	protected final void onPostExecute(final Country[] result) {
		super.onPostExecute(result);
		dialog.dismiss();
		if(ex == null) {
			for(final ParserListener listener : listeners) {
				listener.onParseCompleted(result);
			}
			return;
		}
		for(final ParserListener listener : listeners) {
			listener.onParseFailed(ex);
		}
	}
	
	/**
	 * Gets the source url as string.
	 * 
	 * @return The source url.
	 */

	public static final String getSource() {
		return String.format(Locale.getDefault(), URL, YEAR);
	}
	
	public interface ParserListener {
		
		/**
		 * Called when a parse has been completed with success.
		 * 
		 * @param countries The countries.
		 */
		
		public void onParseCompleted(final Country... countries);
		
		/**
		 * Called when a parse fail for some reason.
		 * 
		 * @param ex The exception occurred.
		 */
		
		public void onParseFailed(final Exception ex);
		
	}

	public static class Country implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public final String name;
		public final String scrappingName;
		public int ranking = -1;
		public float points;
		
		private Country(final String name, final String scrappingName, final int ranking, final float points) {
			this.name = name;
			this.scrappingName = scrappingName;
			this.ranking = ranking;
			this.points = points;
		}
		
		@Override
		public final String toString() {
			return "Please use toString(resources) or toString(activity).";
		}
		
		/**
		 * The real toString() method.
		 * 
		 * @param resources Resources needed to get the string.
		 * 
		 * @return The String representation of this object.
		 */
		
		public final String toString(final Resources resources) {
			return resources.getString(R.string.parser_country_tostring, name, ranking, String.valueOf(points));
		}
		
		/**
		 * The real toString() method.
		 * 
		 * @param activity Activity needed to get the string.
		 * 
		 * @return The String representation of this object.
		 */
		
		public final String toString(final Activity activity) {
			return activity.getString(R.string.parser_country_tostring, name, ranking, String.valueOf(points));
		}

	}

}