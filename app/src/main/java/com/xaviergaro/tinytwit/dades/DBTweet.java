package com.xaviergaro.tinytwit.dades;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.xaviergaro.tinytwit.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe per gestionar la base de dades de Tweets.
 * 
 * @author Javier Garc�a
 * @see Tweet
 */
public class DBTweet {
	public static final String TAG = "DBInterface";

	// Base de dades y taula
	public static final String BD_NOM = "BDTweets";
	public static final String BD_TAULA = "tweets";
	public static final int VERSIO = 5;

	// Camps
	public static final String CLAU_ID = "_id";
	public static final String CLAU_ID_USUARI = "id_usuari";
	public static final String CLAU_NOM = "nom";
	public static final String CLAU_TEXT = "text";
	public static final String CLAU_FAVORITS = "favorits";
	public static final String CLAU_RETWEETS = "retweets";
	public static final String CLAU_DATA_PUBLICACIO = "data";
	public static final String CLAU_URL = "url";
	public static final String CLAU_FOTO = "foto";
	public static final String CLAU_MEDIA = "media";

	// Consulta per crear la taula
	public static final String BD_CREATE = "CREATE TABLE " + BD_TAULA + "("
			+ CLAU_ID + " INTEGER PRIMARY KEY, " + CLAU_ID_USUARI
			+ " TEXT NOT NULL, " + CLAU_NOM + " TEXT NOT NULL, " + CLAU_TEXT
			+ " TEXT NOT NULL, " + CLAU_FAVORITS + " INTEGER NOT NULL, "
			+ CLAU_RETWEETS + " INTEGER NOT NULL, " + CLAU_DATA_PUBLICACIO
			+ " TEXT NOT NULL, " + CLAU_URL + " TEXT, " + CLAU_FOTO + " TEXT, "
			+ CLAU_MEDIA + " TEXT)";

	// Array amb els camps per facilitar les consultes
	private String[] columns = new String[] { CLAU_ID, CLAU_ID_USUARI,
			CLAU_NOM, CLAU_TEXT, CLAU_FAVORITS, CLAU_RETWEETS,
			CLAU_DATA_PUBLICACIO, CLAU_URL, CLAU_FOTO, CLAU_MEDIA };

	private AjudaBD ajuda;
	private SQLiteDatabase bd;
	private Context context;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat dateFormat = new SimpleDateFormat();

	/**
	 * El constructor crear una instancia de AjudaBD i li pasa el context de la
	 * activitat que la inclou.
	 * 
	 * @param context
	 */
	public DBTweet(Context context) {
		this.context = context;
		ajuda = new AjudaBD(context);
	}

	/**
	 * Obre la base de dades.
	 * 
	 * @return aquest mateix objecte per poder encadenar peticions.
	 * @throws SQLException
	 *             si hi ha un error al obrir la base de dades.
	 */
	public DBTweet open() throws SQLException {
		bd = ajuda.getWritableDatabase();
		return this;
	}

	/**
	 * Tanca la base de dades.
	 */
	public void close() {
		try {
			ajuda.close();
		} catch (SQLException e) {
			// Si hi ha un error al tancar la base de dades nom�s el mostrem al
			// log.
			Log.e(TAG, context.getResources().getString(R.string.error_db));
		}
	}

	/**
	 * Insereix un tweet amb la id pasada com argument.
	 * 
	 * @param id
	 *            clau del Tweet.
	 * @param tweet
	 *            tweet per inserir.
	 * @return aquest mateix objecte per poder encadenar peticions.
	 * @throws SQLException
	 *             si hi ha un error al inserir el tweet.
	 */
	public DBTweet insereixTweet(int id, Tweet tweet) throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(CLAU_ID, id);
		initialValues.put(CLAU_ID_USUARI, tweet.idUsuari);
		initialValues.put(CLAU_NOM, tweet.nom);
		initialValues.put(CLAU_TEXT, tweet.text);
		initialValues.put(CLAU_FAVORITS, tweet.favorits);
		initialValues.put(CLAU_RETWEETS, tweet.retweets);
		initialValues.put(CLAU_FOTO, tweet.foto);
		initialValues.put(CLAU_MEDIA, tweet.media);

		// Convertim la data en un string
		initialValues.put(CLAU_DATA_PUBLICACIO,
				dateFormat.format(tweet.dataPublicacio));

		// Convertim la URL en un string
		if (tweet.url != null) {
			initialValues.put(CLAU_URL, tweet.url.toString());
		}

		// Inserim els valors
		bd.insert(BD_TAULA, null, initialValues);

		return this;
	}

	/**
	 * Insereix la llista de tweets pasada com argument. Fa servir l'index de la
	 * llista com a clau primaria.
	 * 
	 * @param tweets
	 *            Llista de Tweets
	 * @return aquest mateix objecte per poder encadenar peticions.
	 * @throws SQLException
	 *             si hi ha un error al inserir qualsevol tweet.
	 */
	public DBTweet insereixTweets(List<Tweet> tweets) throws SQLException {
		for (int i = 0, len = tweets.size(); i < len; i++) {
			insereixTweet(i, tweets.get(i));
		}
		return this;
	}

	/**
	 * Elimina tots els Tweets emmagatzemmats.
	 * 
	 * @return aquest mateix objecte per poder encadenar peticions.
	 * @throws SQLException
	 *             si hi ha un error al eliminar els tweets
	 */
	public DBTweet esborrarTweets() throws SQLException {
		bd.delete(BD_TAULA, null, null);
		return this;
	}

	/**
	 * Retorna el Tweet amb la clau primaria pasada com argument.
	 * 
	 * @param id
	 *            clau del Tweet pre obtenir
	 * @return el Tweet amb la clau corresponent o null si no s'ha trobat.
	 * @throws SQLException
	 *             si hi ha un error al obtenir el tweet.
	 */
	public Tweet obtenirTweet(int id) throws SQLException {
		Tweet tweet = null;

		Cursor cursor = bd.query(true, BD_TAULA, columns, CLAU_ID + " = " + id,
				null, null, null, null, null);

		if (cursor != null) {
			cursor.moveToFirst();
			tweet = passarCursorATweet(cursor);
		}

		return tweet;
	}

	/**
	 * Retorna una llista amb tots els Tweets emmagatzemats a la base de dades.
	 * 
	 * @return Llista amb tots els Tweets o una llista buida si no s'ha trobat
	 *         cap.
	 * @throws SQLException
	 *             si hi ha un error al obtenir qualsevol tweet de la llista.
	 */
	public List<Tweet> obtenirTweets() throws SQLException {
		List<Tweet> tweets = new ArrayList<Tweet>();
		Cursor mCursor = bd.query(BD_TAULA, columns, null, null, null, null,
				null);

		// Recorrem el cursor tots i els afegim a la llista.
		if (mCursor.moveToFirst()) {
			do {
				tweets.add(passarCursorATweet(mCursor));
			} while (mCursor.moveToNext());
		}

		return tweets;
	}

	/**
	 * Retorna un Tweet amb la informaci� trobada a la posici� actual del cursor
	 * pasat com argument.
	 * 
	 * @param cursor
	 *            cursor del que volem extreure el Tweet.
	 * @return el Tweet generat a partir del cursor o null si hi ha un error al
	 *         crear-lo.
	 */
	private Tweet passarCursorATweet(Cursor cursor) {
		Tweet tweet = null;
		Date date = null;
		URL url = null;

		// Convertim la data de la base de dades en un objecte Date
		try {
			date = dateFormat.parse(cursor.getString(6));
		} catch (ParseException e) {
			Log.e(TAG,
					context.getResources()
							.getString(R.string.error_create_date),e);
		}

		// Convertim la URL de la base de dades en un objecte URL
		if (cursor.getString(7) != null) {
			try {
				url = new URL(cursor.getString(7));
			} catch (MalformedURLException e) {
				Log.e(TAG,
						context.getResources().getString(
								R.string.error_create_url),e);
			}
		}

		// Creem un nou Tweet a partir del cursor
		try {
			tweet = new Tweet(cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getInt(4), cursor.getInt(5),
					date, url, cursor.getString(8), cursor.getString(9));
		} catch (Exception e) {
			Log.e(TAG,
					context.getResources().getString(
							R.string.error_create_tweet),e);
		}

		return tweet;
	}

	/**
	 * Classe d'ajuda per gestionar la base de dades.
	 */
	private static class AjudaBD extends SQLiteOpenHelper {
		AjudaBD(Context con) {
			super(con, BD_NOM, null, VERSIO);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(BD_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int versioAntiga,
				int versioNova) {
			Log.w(TAG, "Actualitzant Base de dades de la versi� "
					+ versioAntiga + " a " + versioNova
					+ ". Destruir� totes les dades");
			db.execSQL("DROP TABLE IF EXISTS " + BD_TAULA);

			onCreate(db);
		}
	}
}
