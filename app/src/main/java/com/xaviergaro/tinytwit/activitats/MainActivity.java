package com.xaviergaro.tinytwit.activitats;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xaviergaro.tinytwit.R;
import com.xaviergaro.tinytwit.dades.DBTweet;
import com.xaviergaro.tinytwit.dades.Tweet;
import com.xaviergaro.tinytwit.ui.SimpleToast;
import com.xaviergaro.tinytwit.ui.TweetArrayAdapter;
import com.xaviergaro.tinytwit.utilitats.DownloadImageAsyncTask;
import com.xaviergaro.tinytwit.utilitats.GestorDescarregues;
import com.xaviergaro.tinytwit.utilitats.ReceptorXarxa;
import com.xaviergaro.tinytwit.utilitats.UpdatableStatus;
import com.xaviergaro.tinytwit.utilitats.UtilitatsDisc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Activitat principal de la aplicació. Ampliacions realitzades:
 * <ul>
 * <li>Transparencia del StatusBar i NavBar per API19</li>
 * <li>Localització de la aplicació a óngles, cataló i espanyol (excepte pógina
 * de contingut offline).</li>
 * <li>Durant la actualització del feed queda deshabilitat el botó de refresh.</li>
 * <li>Actualització automótica del feed en els segóents casos: al connectar, al
 * obrir la aplicació i haver dades de autenticació emmagatzemades, al publicar
 * un feed, al activar la connexió a internet.</li>
 * <li>La actualizatció automótica nomós actualitza si la informació es de fa
 * mós de dos minuts (en el cas de connexió no hi ha informació anterior, aixó
 * que sempre actualitza).</li>
 * <li>Enregistra un BroadCast receiver per comprovar l'estat de la connexió i
 * refrescar el tweet automóticament al tornar la connexió. Nomós estó actiu a
 * l'activitat principal, el desactivem quan aquesta activitat no estó en primer
 * pló.</li>
 * <li>Descarrega i mostra les imatges dels perfils del usuari i dels autors
 * dels tweets.</li>
 * <li>Decarrega la imatge enllaóada als tweets que tenen alguna imatge.</li>
 * <li>Eliminació de totes les imatges al desconnectar</li>
 * <li>La vista detall mostra la imatge del autor del tweet.</li>
 * <li>La vista detall mostraró la imatge si hi ha alguna o el enllaó web si no
 * hi ha imatge.</li>
 * <li>Les descarregas son sincronitzades per mostrar la barra de progress
 * mentre hi hagi alguna cosa descarregant, ja siguin imatges o feed.</li>
 * <li>Els toasts i botons estan personalitzats per mostrar un esquema de colors
 * mós adient amb la resta de la aplicació.</li>
 * </ul>
 * 
 * M'agradaria aclarir que em consta que aquesta aplicació no es gens eficient,
 * perquó cada vegada que actualitzem el feed torno a descarregar tots els
 * avatars e imatges, buido la base de dades, etc. Ho he fet aixó per
 * simplificar, i he afegit alguna cosa una mica foróada com el
 * BroadcastReceiver per provar una mica mós.
 * 
 * Em sembla que aixó es tot, salutacions!
 * 
 * TODO: En un futur seria possible afegir una alarma que actualitzós el feed
 * cada dos minuts despres de la ultima actualització.
 * 
 * TODO: S'hauria de fer que al clicar logout s'interrompin totes les
 * descarregues (ara no ho fa).
 * 
 * @author Javier Garcóa
 * 
 */
public class MainActivity extends Activity implements OnClickListener,
		OnMenuItemClickListener, OnItemClickListener, UpdatableStatus,
		GestorDescarregues {
	private static final String TAG = "tinytwit";

	public static final String PROFILES_FOLDER = "/profiles";
	public static final String AVATAR_FOLDER = "/avatar";
	public static final String MEDIA_FOLDER = "/media";

	private static int DOS_MINUTS = 2 * 60 * 1000;
	private static final int MAX_TWEETS_PER_MOSTRAR = 50;

	// Configuració Twitter
	static final String TWITTER_API_KEY = "your api key here";
	static final String TWITTER_API_SECRET = "your api secret here";
	static final String CALLBACK = "tinytwit:///";

	private ConfigurationBuilder cb;
	private TwitterFactory factory;
	private Twitter twitter;
	private RequestToken rqToken;
	private AccessToken accessToken;
	private String verifier;

	// Widgets
	private Button botoConnectar;
	private TextView textViewUsuari;
	private ImageView imageViewProfile;
	private ListView listViewFeeds;
	private ProgressBar progressBar;
	private ArrayAdapter<Tweet> adapter;

	private SimpleToast toaster;

	// ActionBar
	private MenuItem menuItemRefresh;
	private MenuItem menuItemPublish;

	// Usuari
	private SharedPreferences prefs;
	private boolean autenticat = false;
	private String fotoUsuari;

	// Feed
	private List<Tweet> tweets;

	// Base de dades
	DBTweet db = new DBTweet(this);

	// Broadcast Receiver
	private ReceptorXarxa receptor;

	// Control de tasques
	private boolean feed_actualitzat;
	private boolean descarregantFeed;
	private int descarregues;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_principal);

		// Codi que s'executa nomós al crear l'activitat
		iniciarDadesTwitter();
		enregistrarWidgets();

		// Establim l'arxiu de preferencies
		prefs = getSharedPreferences("TinytwitsPreferences", MODE_PRIVATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflem els MenuItems que aniran a la ActionBar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_principal_actionbar, menu);

		// Enregistrem els MenuItem
		menuItemRefresh = menu.findItem(R.id.action_refresh);
		menuItemRefresh.setOnMenuItemClickListener(this);

		menuItemPublish = menu.findItem(R.id.action_publish);
		menuItemPublish.setOnMenuItemClickListener(this);

		menuItemRefresh.setEnabled(autenticat);
		menuItemRefresh.setVisible(autenticat);

		menuItemPublish.setEnabled(autenticat);
		menuItemPublish.setVisible(autenticat);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Configura els objectes inicials per poder instanciar objectes Twitter
	 */
	private void iniciarDadesTwitter() {
		cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(TWITTER_API_KEY);
		cb.setOAuthConsumerSecret(TWITTER_API_SECRET);
		Configuration conf = cb.build();
		factory = new TwitterFactory(conf);
	}

	/**
	 * Enregistra els widgets de la acitivtat i els seus listeners
	 */
	private void enregistrarWidgets() {
		botoConnectar = (Button) findViewById(R.id.botoConnexio);
		botoConnectar.setOnClickListener(this);

		textViewUsuari = (TextView) findViewById(R.id.textViewUsuari);
		textViewUsuari.setText("");

		imageViewProfile = (ImageView) findViewById(R.id.imageViewProfile);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		listViewFeeds = (ListView) findViewById(R.id.listViewFeed);
		listViewFeeds.setOnItemClickListener(this);

		toaster = new SimpleToast(this);
		receptor = new ReceptorXarxa(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Codi que s'executa cada vegada que es torna a la aplicació
		receptor.enregistrar();
		restaurarSessio();
		actualitzarVista();
		actualitzarFeed();
	}

	/**
	 * Actualitza l'estat de la sessió amb la informació emmagatzemada al arxiu
	 * de preferencies
	 * 
	 */
	private void restaurarSessio() {
		// Comprovem si tenim la informació de autenticació guardada
		if (prefs.contains("OAUTH_TOKEN")
				&& prefs.contains("OAUTH_TOKEN_SECRET")) {
			String token = prefs.getString("OAUTH_TOKEN", "");
			String secret = prefs.getString("OAUTH_TOKEN_SECRET", "");
			fotoUsuari = prefs.getString("FOTO", "");
			accessToken = new AccessToken(token, secret);
			twitter = factory.getInstance(accessToken);
			connectar(true);
		}
	}

	/**
	 * Actualitza el estat dels botons i el nom del usuari segons l'estat de la
	 * sessió.
	 */
	private void actualitzarVista() {
		// Comprovem que els MenuItem ja han estat creats
		if (menuItemRefresh != null) {
			menuItemRefresh.setEnabled(autenticat);
			menuItemRefresh.setVisible(autenticat);
			menuItemPublish.setEnabled(autenticat);
			menuItemPublish.setVisible(autenticat);
		}

		invalidateOptionsMenu();
		textViewUsuari.setText(prefs.getString("SCREEN_NAME", ""));
	}

	@Override
	public void onPause() {
		super.onPause();

		// Codi que s'executa cada vegada que es pausa l'activitat
		receptor.desenregistrar();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Uri uri = intent.getData();

		// Si no es null i comenóa amb "tinytwit:///"
		if (uri != null && uri.toString().startsWith(CALLBACK)) {

			// Guardem el verificador (ós un String)
			verifier = uri.getQueryParameter("oauth_verifier");

			// AsyncTask per obtenir l'AccessToken
			new AccessTokenAsyncTask().execute();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == botoConnectar) {
			autenticar();
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem mi) {

		if (mi == menuItemRefresh) {
			// Si hi ha connexió, actualitzem el feed
			if (!receptor.comprovarConnexio()) {
				return true;
			}
			actualitzaLastUpdate(false);
			actualitzarFeed();
			return true;

		} else if (mi == menuItemPublish) {
			// Llencem l'activitat per publicar
			Intent intent = new Intent(MainActivity.this,
					PublicarActivity.class);
			startActivity(intent);
			return true;
		}

		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		// Llencem l'activitat detall passant-li el index del tweet
		Intent intent = new Intent(MainActivity.this, DetallActivity.class);
		intent.putExtra("id", position);
		startActivity(intent);
	}

	/**
	 * Actualitza el valor de la óltima actualització al arxiu de preferencies a
	 * la hora actual o l'esborra.
	 * 
	 * @param actualitza
	 *            true si es vol fer servir la hora actual, o false si es vol
	 *            esborrar.
	 */
	private void actualitzaLastUpdate(boolean actualitza) {
		SharedPreferences.Editor editor = prefs.edit();
		if (actualitza) {
			editor.putLong("LAST_UPDATE", System.currentTimeMillis());
		} else {
			editor.remove("LAST_UPDATE");
		}
		editor.apply();
	}

	/**
	 * Llenóa la tasca per autenticar al usuari si no estó autenticat, o el
	 * desautentica si ho estaba.
	 */
	private void autenticar() {
		if (!autenticat) {
			// Si no hi ha connexió no es pot autenticar
			if (!receptor.comprovarConnexio()) {
				return;
			}

			// Iniciem la tasca d'autenticació
			twitter = factory.getInstance();
			new RequestTokenAsyncTask().execute();

		} else {
			// El desconectem
			connectar(false);
		}
	}

	/**
	 * Canvia l'estat a autenticat o desautenticat.
	 * 
	 * @param connectar
	 *            true per passar a autenticat o false per desautenticar.
	 */
	private void connectar(boolean connectar) {
		autenticat = connectar;
		if (connectar) {
			// Canviem el botó a desconnectar
			botoConnectar.setText(R.string.desconnectar);
			carregarAvatar(true);

		} else {
			// Posem els objectes a null
			twitter.setOAuthAccessToken(null);
			twitter.shutdown();

			// Canviem el botó a connectar
			botoConnectar.setText(R.string.connectar);

			// Eliminar les dades en preferencies
			guardarPreferencies(false);

			// Buidem la base de dades
			db.open().esborrarTweets().close();

			// Eliminem totes les imatges guardades
			UtilitatsDisc.buidarDirectori(getFilesDir() + PROFILES_FOLDER);
			UtilitatsDisc.buidarDirectori(getFilesDir() + MEDIA_FOLDER);
			UtilitatsDisc.buidarDirectori(getFilesDir() + AVATAR_FOLDER);
		}

		actualitzarVista();
		actualitzarFeed();
	}

	/**
	 * Guarda o esborra les dades de la sessió al arxiu de preferencies.
	 * 
	 * @param guardar
	 *            true per guardarles o false per esborrarles.
	 */
	private void guardarPreferencies(boolean guardar) {
		SharedPreferences.Editor editor = prefs.edit();

		if (guardar) {
			// Guardem les preferóncies
			editor.putString("OAUTH_TOKEN", accessToken.getToken());
			editor.putString("OAUTH_TOKEN_SECRET", accessToken.getTokenSecret());
			if (accessToken.getScreenName() == null) {
				editor.putString("SCREEN_NAME",
						prefs.getString("SCREEN_NAME", ""));
			} else {
				editor.putString("SCREEN_NAME",
						"@" + accessToken.getScreenName());
			}
			editor.putString("FOTO", fotoUsuari);
		} else {
			// Eliminar les dades en preferencies
			editor.remove("OAUTH_TOKEN");
			editor.remove("OAUTH_TOKEN_SECRET");
			editor.remove("SCREEN_NAME");
			editor.remove("LAST_UPDATE");
			editor.remove("FOTO");
			carregarAvatar(false);
		}
		editor.apply();
	}

	/**
	 * Actualitzar la llista de feeds. Si el usuari no esta autenticat esborra
	 * la llista, en cas contrari la llista s'actualitza amb la informació
	 * d'Internet si han pasat mós de dos minuts des de la óltima actualització.
	 * Si no ha pasat aquest temps o no hi ha connexió i la llista es buida,
	 * s'omple amb la informació de la base de dades.
	 */
	private void actualitzarFeed() {
		// Comprovem si ja s'estó actualitzant el feed
		if (descarregantFeed) {
			return;
		}

		// Comprovem si estó autenticat, si no ho estó esborrem el feed i tornem
		if (!autenticat) {
			if (adapter != null) {
				adapter.clear();
			}
			tweets = null;
			return;
		}

		// Comprovem si cal refrescar la informacio, o fem servir la de la base
		// de dades.
		long lastUpdate = prefs.getLong("LAST_UPDATE", 0);
		long now = System.currentTimeMillis();

		if (lastUpdate + DOS_MINUTS < now
				&& receptor.comprovarConnexioSilencios()) {
			new CarregarFeed().execute();

		} else if (adapter == null || adapter.isEmpty()) {
			// Si no estó connectat i la llista es buida, carreguem el feed de
			// la base de dades
			try {
				tweets = db.open().obtenirTweets();
			} catch (SQLException e) {
				// En cas d'error la llista seró buida.
				toaster.toast(R.string.error_db);
				tweets = new ArrayList<Tweet>();
			} finally {
				db.close();
			}
			ompleFeed();
		}
	}

	/**
	 * Omple l'adaptador de la llista amb la informació dels tweets en memoria.
	 */
	private void ompleFeed() {
		// Si no hi ha cap tweet per mostrar retornem
		if (tweets == null || tweets.isEmpty()) {
			return;
		}

		adapter = new TweetArrayAdapter(this, tweets);
		listViewFeeds.setAdapter(adapter);

		if (feed_actualitzat) {
			toaster.toast(R.string.feed_actualitzat);
			feed_actualitzat = false;
		}
	}

	@Override
	public void onUpdateStatus(boolean connexio) {
		// Quan l'estat de la connexió canvia a connectat actualitzem el feed
		if (connexio) {
			actualitzarFeed();
		}
	}

	/**
	 * Aquest mótode es crida al comenóar i finalitzar la descarrega de dades i
	 * imatges. Mentre hi ha descarregues en curs es mostra la barra de progres
	 * i es desactiva el botó de refresh.
	 * 
	 * @param descarrega
	 *            si es true indica que inicia la descarrega en cas contrari
	 *            s'indica que finalitza la descarrega.
	 */
	@Override
	public synchronized void descarregaEnCurs(boolean descarrega) {
		// Actualitzem el comptador
		if (descarrega) {
			descarregues++;
		} else {
			descarregues--;
		}

		if (descarregues > 0) {
			// Hi han descarregas pendents

			// Ens asegurem que el botó de refrescar estó desactivat i la barra
			// de progress visible
			if (menuItemRefresh != null) {
				menuItemRefresh.setEnabled(false);
			}
			progressBar.setVisibility(View.VISIBLE);

		} else {
			// No quedan mós descarregues pendents

			// Omplim el feed
			ompleFeed();

			// Mostrem l'avatar
			carregarAvatar(autenticat);

			// Reactivem el botó de refrescar
			menuItemRefresh.setEnabled(true);

			// Ocultem la barra de progrós
			progressBar.setVisibility(View.GONE);
		}
	}

	/**
	 * Mostra o oculta el avatar segons el valor del argument passat.
	 * 
	 * @param carregar
	 *            si es true mostra l'avatar, si es false l'oculta i l'esborra
	 *            del disc.
	 */
	private void carregarAvatar(boolean carregar) {
		if (carregar) {
			File file = new File(getFilesDir() + AVATAR_FOLDER, fotoUsuari);
			if (file.exists()) {
				Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
				imageViewProfile.setImageBitmap(image);
			}
		} else {
			UtilitatsDisc.buidarDirectori(getFilesDir() + AVATAR_FOLDER);
			imageViewProfile.setImageBitmap(null);
		}
	}

	/**
	 * Aquesta classe realitza una petició de RequestToken i obre un explorador
	 * per demanar les dades al usuari. Al finalitzar el token queda guardat al
	 * atribut de la classe principal rqToken
	 */
	private class RequestTokenAsyncTask extends AsyncTask<Void, Void, Void> {
		private static final String TAG = "RequestTokenAsyncTask";

		@Override
		protected Void doInBackground(Void... params) {
			try {
				rqToken = twitter.getOAuthRequestToken(CALLBACK);
			} catch (TwitterException e) {
				Log.e(TAG, "Twitter Exception", e);
				Log.e(TAG,
						getResources().getString(
								R.string.error_twitter_exception));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rqToken
					.getAuthenticationURL()));
			startActivity(intent);
		}
	}

	/**
	 * Aquesta classe fa servir els atributs rqtoken i verifier per obtenir un
	 * AccessToken per finalitzar la autenticació. Aquest token es guarda
	 * despres al atribut de la classe pricinal accessToken i es guardan el
	 * RequestToken i el AccessToken al arxiu de preferencies de la aplicació.
	 */
	private class AccessTokenAsyncTask extends AsyncTask<Void, Void, Void> {
		private static final String TAG = "AccessTokenAsyncTask";
		private String url;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				accessToken = twitter.getOAuthAccessToken(rqToken, verifier);
			} catch (TwitterException e) {
				Log.e(TAG, "Twitter Exception", e);
			}

			// Obtenim la URL del avatar del usuari
			try {
				url = twitter.showUser(twitter.getId())
						.getBiggerProfileImageURL();
			} catch (TwitterException e) {
				Log.e(TAG, "Twitter Exception", e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Descarreguem l'avatar
			new DownloadImageAsyncTask(MainActivity.this, getFilesDir()
					+ AVATAR_FOLDER).execute(url);
			fotoUsuari = Uri.parse(url).getLastPathSegment();

			// Fi de la autenticació
			guardarPreferencies(true);
			connectar(true);
		}
	}

	/**
	 * Carregar el feed de tweets, els pasa a la activitat principal, omple la
	 * llista i els guarda a la base de dades.
	 */
	private class CarregarFeed extends AsyncTask<Void, Void, List<Tweet>> {
		private static final String TAG = "CarregarFeed";

		// Fem servir un Set en lloc d'una llista per evitar descarregar URLs
		// duplicades
		Set<String> fotos = new HashSet<String>();
		Set<String> medias = new HashSet<String>();

		@Override
		protected void onPreExecute() {
			descarregaEnCurs(true);
			descarregantFeed = true;
		}

		@Override
		protected List<Tweet> doInBackground(Void... params) {
			List<twitter4j.Status> statuses = new ArrayList<twitter4j.Status>();
			List<Tweet> feed = new ArrayList<Tweet>();

			Paging paging = new Paging(1, MAX_TWEETS_PER_MOSTRAR);

			try {
				statuses = twitter.getHomeTimeline(paging);
			} catch (TwitterException e) {
				Log.e(TAG, "Twitter Exception", e);
			}

			for (twitter4j.Status status : statuses) {
				feed.add(new Tweet(status));
				fotos.add(status.getUser().getBiggerProfileImageURL());

				// Si aquest status te algun media, l'afegim a la llista
				if (status.getMediaEntities().length > 0) {
					medias.add(status.getMediaEntities()[0].getMediaURL());
				}
			}

			// Actualitzem la base de dades
			try {
				db.open().esborrarTweets().insereixTweets(feed);
				actualitzaLastUpdate(true);
			} catch (SQLException e) {
				toaster.toast(R.string.error_db);
			} finally {
				db.close();
			}

			return feed;
		};

		@Override
		protected void onPostExecute(List<Tweet> feed) {
			// Eliminem els perfils antics i descarreguem els nous
			String folder = getFilesDir() + PROFILES_FOLDER;
			String[] urlsFotos = fotos.toArray(new String[fotos.size()]);
			UtilitatsDisc.buidarDirectori(folder);
			new DownloadImageAsyncTask(MainActivity.this, folder)
					.execute(urlsFotos);

			// Eliminem els media antics i descarreguem els nous
			String[] urlsMedia = medias.toArray(new String[medias.size()]);
			folder = getFilesDir() + MEDIA_FOLDER;
			UtilitatsDisc.buidarDirectori(folder);
			new DownloadImageAsyncTask(MainActivity.this, folder)
					.execute(urlsMedia);

			tweets = feed;
			feed_actualitzat = true;
			descarregaEnCurs(false);
			descarregantFeed = false;
		}
	}
}
