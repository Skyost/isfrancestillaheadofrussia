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
import android.os.AsyncTask;
import fr.skyost.isfrancestillaheadofrussia.Parser.Country;
import fr.skyost.isfrancestillaheadofrussia.Parser.ParserListener;

public class Parser extends AsyncTask<ParserListener, Void, Country[]> {

	private static final short YEAR = 2016;
	private static final String URL = "http://kassiesa.home.xs4all.nl/bert/uefa/data/method4/crank%d.html";

	private final Activity parent;
	private final Country[] countries = new Country[2]; // Used to check all references in a row, AtomicReference can be used too.

	private final ProgressDialog dialog;
	private ParserListener[] listeners;
	private Exception ex;

	public Parser(final Activity parent) {
		this.parent = parent;
		countries[0] = new Country(parent.getString(R.string.parser_countries_one), "France", -1, -1f);
		countries[1] = new Country(parent.getString(R.string.parser_countries_two), "Russia", -1, -1f);
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
			for(int i = 0, currentRanking = 0, elementsLoaded = 0; i != ranking.size(); i++) {
				final Element row = ranking.get(i);
				final String attr = row.attr("align");
				if(attr == null || !attr.equals("left")) {
					continue;
				}
				if(currentRanking == 0) {
					currentRanking++; // < 2015
					continue;
				}
				currentRanking++;
				for(final Country country : countries) {
					if(!row.text().equals(country.scrappingName)) {
						continue;
					}
					country.ranking = currentRanking - 1;
					country.points = Float.parseFloat(ranking.get(i + 6).text());
					elementsLoaded++;
				}
				if(elementsLoaded == 2) {
					break;
				}
			}
			return countries;
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
		if(listeners == null || listeners.length == 0) {
			return;
		}
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
			return "Please use toString(activity).";
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