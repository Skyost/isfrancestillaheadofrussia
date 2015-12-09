package fr.skyost.isfrancestillaheadofrussia;

import java.io.Serializable;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import fr.skyost.isfrancestillaheadofrussia.Parser.Country;

public class Parser extends AsyncTask<Void, Void, Country[]> {

	private static final short YEAR = 2016;
	private static final String URL = "http://kassiesa.home.xs4all.nl/bert/uefa/data/method4/crank%d.html";

	private final MainActivity parent;
	private final Country countryOne;
	private final Country countryTwo;

	private final ProgressDialog dialog;
	private Exception ex;

	public Parser(final MainActivity parent) {
		this.parent = parent;
		final Resources resources = parent.getResources();
		countryOne = new Country(resources.getString(R.string.parser_countries_one), "France", -1, -1f);
		countryTwo = new Country(resources.getString(R.string.parser_countries_two), "Russia", -1, -1f);
		this.dialog = new ProgressDialog(parent);
	}

	@Override
	protected final void onPreExecute() {
		dialog.setTitle(R.string.parser_dialog_title);
		dialog.setMessage(parent.getString(R.string.parser_dialog_message));
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected final Country[] doInBackground(final Void... args) {
		try {
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
			parent.onParseCompleted(result);
			return;
		}
		ex.printStackTrace();
		parent.setResponse(parent.getResources().getString(R.string.main_textfield_response_error, ex.getClass().getName()), 20f, false);
	}
	
	/**
	 * Gets the source url as string.
	 * 
	 * @return The source url.
	 */

	public static final String getSource() {
		return String.format(Locale.getDefault(), URL, YEAR);
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
			return "Please use toString(resources).";
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

	}

}