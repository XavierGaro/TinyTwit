package com.xaviergaro.tinytwit.activitats;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.xaviergaro.tinytwit.R;
import com.xaviergaro.tinytwit.dades.DBTweet;
import com.xaviergaro.tinytwit.dades.Tweet;
import com.xaviergaro.tinytwit.ui.SimpleToast;
import com.xaviergaro.tinytwit.utilitats.ReceptorXarxa;

import java.io.File;

/**
 * Aquesta classe mostra el detall d'un tweet emmagatzemmat a la base de dades
 * del qual es pasa la id com a extra a l'intent, inclosa la web si hi ha alguna
 * afegida i hi ha connexi�.
 * 
 * @author Javier Garc�a
 * 
 */
public class DetallActivity extends Activity {
	private static final String TAG = "DetallTweet";

	// Base de dades
	DBTweet db = new DBTweet(this);

	// UI
	ImageView imageViewProfile;
	TextView textViewNom;
	TextView textViewTweetText;
	TextView textViewFavoriteCount;
	TextView textViewRetweetCount;
	TextView textViewPubDate;
	WebView web;
	ImageView imageViewPicture;

	private SimpleToast toaster;
	private ReceptorXarxa receptor;
	private Tweet tweet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detall);

		inicialitzarWidgets();
		obtenirTweet();
		carregarTweet();
		carregarImatgeProfile();

		// Si carreguem una imatge, no es carrega la web
		if (!carregarImatge()) {
			carregarWeb();
		}
	}

	/**
	 * Obtenim les referencies a tots els widgets
	 */
	private void inicialitzarWidgets() {
		receptor = new ReceptorXarxa(this);

		imageViewProfile = (ImageView) findViewById(R.id.imageViewProfile);
		textViewNom = (TextView) findViewById(R.id.textViewNom);
		textViewTweetText = (TextView) findViewById(R.id.textViewTweetText);
		textViewFavoriteCount = (TextView) findViewById(R.id.textViewFavoriteCount);
		textViewRetweetCount = (TextView) findViewById(R.id.textViewRetweetCount);
		textViewPubDate = (TextView) findViewById(R.id.textViewPubDate);
		web = (WebView) findViewById(R.id.webViewDetall);
		imageViewPicture = (ImageView) findViewById(R.id.imageViewPicture);

		// Preparem el WebView per acceptar les urls curtes dels tweets.
		web.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});
	}

	/**
	 * Obt� el tweet corresponent al id pasat al llen�ar aquesta activitat.
	 */
	private void obtenirTweet() {
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			// Obtenim la id del tweet
			int id = extras.getInt("id");

			// Obtenim el tweet corresponen de la base de dades
			try {
				db.open();
				tweet = db.obtenirTweet(id);
				db.close();
			} catch (Exception e) {
				toaster.toast(R.string.error_db);
			}
		}
	}

	/**
	 * Carrega les dades del tweet als widgets de la activitat.
	 */
	private void carregarTweet() {
		// Si no hi ha tweet no mostrem res.
		if (tweet == null) {
			toaster.toast(R.string.tweet_not_found);
			return;
		}

		textViewNom.setText(tweet.nom);
		textViewTweetText.setText(tweet.text);
		textViewFavoriteCount.setText(tweet.favorits + "");
		textViewRetweetCount.setText(tweet.retweets + "");
		textViewPubDate.setText(tweet.dataPublicacio.toString());
		setTitle("@" + tweet.idUsuari);
	}

	/**
	 * Si el tweet cont� una imatge la carreguem al ImageView i el fem visible.
	 * En cas contrari la ocultem.
	 * 
	 * @return true si s'ha mostrat la imatge o false si no s'ha mostrat.
	 */
	private boolean carregarImatge() {
		// Comprovem si aquest tweet te associat un media
		if (tweet.media != null && tweet.media.length() > 0) {
			imageViewPicture.setVisibility(View.VISIBLE);
		} else {
			imageViewPicture.setVisibility(View.GONE);
			return false;
		}

		// Intentem carregar la imatge corresponent al tweet
		try {
			String filename = tweet.media;
			File file = new File(getFilesDir() + MainActivity.MEDIA_FOLDER,
					filename);
			Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
			imageViewPicture.setImageBitmap(image);
			return true;
		} catch (Exception e) {
			// En cas de que hi hagi un error de qualsevol tipus retornem fals.
			return false;
		}
	}

	/**
	 * Carrega la web enlla�ada al tweet si hi ha alguna i tenim conexi�. Si no
	 * hi ha connexi� mostra el missatge per defecte.
	 */
	private void carregarWeb() {
		// Comprovem si aquest tweet te associat una URL
		if (tweet.url != null) {
			web.setVisibility(View.VISIBLE);
		} else {
			// Si no hi ha retornem
			web.setVisibility(View.GONE);
			return;
		}

		// Mostrem el WebView
		if (receptor.comprovarConnexioSilencios()) {
			web.loadUrl(tweet.url.toString());
		} else {
			web.loadUrl("file:///android_asset/offline.html");
		}
	}

	/**
	 * Carrega la imatge del perfil.
	 */
	private void carregarImatgeProfile() {
		// Comprovem si te una foto de perfil associada
		if (tweet.foto == null || tweet.foto.length() == 0) {
			// Si no hi ha retornem
			return;
		}

		String filename = tweet.foto;
		File file = new File(getFilesDir() + MainActivity.PROFILES_FOLDER,
				filename);
		Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
		imageViewProfile.setImageBitmap(image);
	}
}
