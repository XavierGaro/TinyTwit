package com.xaviergaro.tinytwit.utilitats;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.xaviergaro.tinytwit.dades.Tweet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Aquesta classe s'encarrega de descarregar imatges i emmagatzemarles en el
 * disc en la carpeta indicada al constructor. La llista de imatges a
 * descarregar es passa com un array de Strings amb la URL de cada imatge.
 * Aquesta classe comunicar� al GestorDescarrgues passat com argument al
 * constructor quan inicia i quan finalitza les descarregues.
 * 
 * @author Javier Garc�a
 * 
 */
public class DownloadImageAsyncTask extends AsyncTask<String, Void, Void> {
	public static final String TAG = "DownloadImageTask";
	public static final int TIMEOUT = 1000;

	private File folder;
	private GestorDescarregues gestor;

	/**
	 * Aquest constructor accepta que la carpeta de dest� sigui una cadena de
	 * text.
	 * 
	 * @param gestor
	 *            Activitat que implementi la interficie GestroDescarregues.
	 * @param folder
	 *            cadena de text amb la carpeta de dest�.
	 * @see GestorDescarregues
	 */
	public DownloadImageAsyncTask(GestorDescarregues gestor, String folder) {
		this(gestor, new File(folder));
	}

	/**
	 * Aquest constructor requereix que la carpeta sigui un File
	 * 
	 * @param gestor
	 *            Activitat que implementi la interficie GestroDescarregues.
	 * @param folder
	 *            objecte File amb la carpeta de dest�.
	 * @see GestorDescarregues
	 */
	DownloadImageAsyncTask(GestorDescarregues gestor, File folder) {
		this.folder = folder;
		this.gestor = gestor;
		comprovarDirectori();
	}

	/**
	 * Comprova si existeix el directori emmagatzemat a folder, i si no existeix
	 * el crea.
	 */
	private void comprovarDirectori() {
		if (!folder.exists()) {
			folder.mkdir();
		}
	}

	@Override
	protected void onPreExecute() {
		gestor.descarregaEnCurs(true);
	}

	@Override
	protected Void doInBackground(String... urls) {
		Bitmap image;
		File file;
		String filename = null;
		InputStream in;
		FileOutputStream out = null;

		// Recorrem la llista d'urls i descarreguem cada una de les imatges
		for (String url : urls) {
			// Si hi ha cap error al descarregar una imatge la resta continua
			// descarregant.
			try {
				// Descarreguem la imatge, si triga massa en connectar passem a
				// la seg�ent
				URLConnection con = new URL(url).openConnection();
				con.setConnectTimeout(TIMEOUT);
				in = con.getInputStream();

				image = BitmapFactory.decodeStream(in);

				// Guardem la imatge en un fitcher
				filename = Tweet.obtenirNomFitxer(url);
				file = new File(folder, filename);
				out = new FileOutputStream(file);

				image.compress(CompressFormat.PNG, 100, out);
				out.flush();

			} catch (Exception e) {
				// Com que no podem fer res per solucionar-lo ho ignorem.
			} finally {
				tancar(out);
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		gestor.descarregaEnCurs(false);
	}

	/**
	 * Tanca un flux de sortida passat com argument.
	 * 
	 * @param out
	 *            flux a tancar.
	 */
	private void tancar(FileOutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				// Si hi ha cap error l'ignorem.
			}
		}
	}
}
