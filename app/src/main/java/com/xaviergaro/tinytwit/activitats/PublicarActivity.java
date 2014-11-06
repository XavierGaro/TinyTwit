package com.xaviergaro.tinytwit.activitats;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xaviergaro.tinytwit.R;
import com.xaviergaro.tinytwit.ui.SimpleToast;
import com.xaviergaro.tinytwit.utilitats.ReceptorXarxa;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Activitat que permet publicar tweets a internet. Es necessari que hi hagin
 * les dades d'autenticaci� emmagatzemades al fitxer de preferencies de la
 * aplicaci�.
 * 
 * @author Javier Garc�a
 * 
 */
public class PublicarActivity extends Activity implements OnClickListener {

	public static final String TAG = "PublicarActivity";

	// Configuraci� Twitter
	private SharedPreferences prefs;
	private ConfigurationBuilder cb;
	private TwitterFactory factory;
	private Twitter twitter;
	private AccessToken accessToken;

	// Elements UI
	private TextView textViewUsuari;
	private EditText editTextTweet;
	private Button botoPublicar;
	private Button botoCancelar;

	private SimpleToast toaster;
	private ReceptorXarxa receptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publicar);
		inicialitzar();
	}

	/**
	 * Inicialitza els objectes, dades i widgets.
	 */
	private void inicialitzar() {
		// Carreguem les prefer�ncies i obtenim el nom
		prefs = getSharedPreferences("TinytwitsPreferences", MODE_PRIVATE);
		toaster = new SimpleToast(this);
		receptor = new ReceptorXarxa(this);
		enregistrarWidgets();
		iniciarDadesTwitter();
	}

	/**
	 * Enregistra els widgets.
	 */
	private void enregistrarWidgets() {
		editTextTweet = (EditText) findViewById(R.id.editTextPublicar);

		botoPublicar = (Button) findViewById(R.id.botoEnviar);
		botoPublicar.setOnClickListener(this);

		botoCancelar = (Button) findViewById(R.id.botoCancel);
		botoCancelar.setOnClickListener(this);

		textViewUsuari = (TextView) findViewById(R.id.textViewUsuari);
		textViewUsuari.setText(prefs.getString("SCREEN_NAME", ""));
	}

	/**
	 * Configura els objectes inicials per poder instanciar objectes Twitter
	 */
	private void iniciarDadesTwitter() {
		cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(MainActivity.TWITTER_API_KEY);
		cb.setOAuthConsumerSecret(MainActivity.TWITTER_API_SECRET);
		Configuration conf = cb.build();
		factory = new TwitterFactory(conf);
	}

	@Override
	public void onClick(View v) {
		if (v == botoCancelar) {
			// Sortim de la activitat
			finish();
		} else if (v == botoPublicar) {
			// Intentem enviar el Tweet
			enviarTweet();
		}
	}

	/**
	 * Comprova que el missatge del EditText tingui la mida correcta i hi hagi
	 * connexi�, si es aix� l'intenta publicar.
	 */
	public void enviarTweet() {
		// Comprovem que el missatge te entre 1 i 140 car�cters
		String text = editTextTweet.getText().toString();
		if (text.length() == 0 || text.length() > 140) {
			toaster.toast(R.string.error_mida_tweet);
			return;
		} else if (!receptor.comprovarConnexio()) {
			return;
		}

		publicarTweet(text);
	}

	/**
	 * Publica el missatge passat com argument a twitter fent servir les dades
	 * d'autenticaci� guardades al arxiu de preferencies.
	 * 
	 * @param text
	 *            text a enviar.
	 */
	private void publicarTweet(String text) {
		// Comprovem si hi han les preferencies emmagatzemades
		if (prefs.contains("OAUTH_TOKEN")
				&& prefs.contains("OAUTH_TOKEN_SECRET")) {
			// Carreguem els tokens y obtenim un nou Twitter
			String token = prefs.getString("OAUTH_TOKEN", "");
			String secret = prefs.getString("OAUTH_TOKEN_SECRET", "");
			accessToken = new AccessToken(token, secret);
			twitter = factory.getInstance(accessToken);
		} else {
			toaster.toast(R.string.error_authentication);
			return;
		}

		// Cridem a la tasca per publicar el missatge
		new PublicarAsyncTask().execute(text);
	}

	/**
	 * Tasca per publicar un missatge a Twitter
	 * 
	 */
	private class PublicarAsyncTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			boolean correcte = false;
			String text = params[0];

			try {
				twitter.updateStatus(text);
				correcte = true;
			} catch (TwitterException e) {
				Log.e(TAG, "Twitter Exception", e);
			}
			return correcte;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Si s'ha enviat correctament mostrem el missatge i sortim de la
			// activitat.
			if (result) {
				toaster.toast(R.string.tweet_enviat);

				// Eliminem la data da la ultima actualitzaci�
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove("LAST_UPDATE");
				editor.apply();

				// Sortim de la activitat
				finish();

			} else {
				toaster.toast(R.string.error_publicar);
			}
		}
	}
}