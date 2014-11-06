package com.xaviergaro.tinytwit.dades;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import twitter4j.Status;
import android.net.Uri;
import android.util.Log;

/**
 * Aquesta classe emmagatzema les dades obtingudes d'un tweet, es immutable
 * nom�s es poden establir els valors en el moment de crearla i no son
 * necessaris getters per accedir als seus valors.
 * 
 * @author Javier Garc�a
 * 
 */
public class Tweet {
	public static final String TAG = "Tweet";

	public final String idUsuari;
	public final String nom;
	public final String text;
	public final int favorits;
	public final int retweets;
	public final Date dataPublicacio;
	public final URL url;
	public final String foto;
	public final String media;

	/**
	 * Construeix el Tweet a partir de les dades pasades com argument
	 * 
	 * @param idUsuari
	 *            identificador del usuari a twitter
	 * @param nom
	 *            nom complet del usuari
	 * @param text
	 *            contingut del Tweet
	 * @param favorits
	 *            quantitat de favorits
	 * @param retweets
	 *            quantitat de retweets
	 * @param dataPublicacio
	 *            data de publicaci�
	 * @param url
	 *            que forma part del Tweet
	 * @param foto
	 *            nom del fitxer que cont� la foto del perfil
	 * 
	 */
	public Tweet(String idUsuari, String nom, String text, int favorits,
			int retweets, Date dataPublicacio, URL url, String foto, String media) {
		this.idUsuari = idUsuari;
		this.nom = nom;
		this.text = text;
		this.favorits = favorits;
		this.retweets = retweets;
		this.dataPublicacio = dataPublicacio;
		this.url = url;
		this.foto = foto;
		this.media = media;
	}

	/**
	 * Construeix el Tweet a partir del objecte Status pasat com argument.
	 * 
	 * @param status
	 *            Status del que s'extreu la informaci�.
	 */
	public Tweet(Status status) {
		this.idUsuari = status.getUser().getScreenName();
		this.nom = status.getUser().getName();
		this.text = status.getText();
		this.favorits = status.getFavoriteCount();
		this.retweets = status.getRetweetCount();
		this.dataPublicacio = status.getCreatedAt();

		// Comprovem si hi ha cap media
		String media="";
		if (status.getMediaEntities().length > 0) {
			try {
				media = obtenirNomFitxer(new URL(status.getMediaEntities()[0].getMediaURL()));
			} catch (MalformedURLException e) {
				// Si la URL est� mal formada no fem res
			}
		}
		this.media = media;

		// Si hi ha alguna url, intentem recuperar-la
		URL tempURL = null;		
		if (status.getURLEntities().length > 0 ) {
			try {
				tempURL = new URL(status.getURLEntities()[0].getURL());
			} catch (MalformedURLException e) {
				// Si la URL est� mal formada no fem res. 
			}
		}		
		this.url = tempURL;

		// Profile
		String foto = "";
		try {
			foto = obtenirNomFitxer(new URL(status.getUser()
					.getBiggerProfileImageURL()));
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error al obtenir el nom de la foto del tweet", e);
		}
		this.foto = foto;

	}

	public static String obtenirNomFitxer(String url) {
		return Uri.parse(url).getLastPathSegment();
	}

	public static String obtenirNomFitxer(URL url) {
		return obtenirNomFitxer(url.getFile());
	}

	@Override
	public String toString() {
		return nom + " - " + text;
	}
}
