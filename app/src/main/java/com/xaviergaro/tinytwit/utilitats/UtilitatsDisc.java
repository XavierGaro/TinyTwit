package com.xaviergaro.tinytwit.utilitats;

import java.io.File;

/**
 * Classe amb utilitats de disc.
 * 
 * @author Javier Garc�a
 * 
 */
public class UtilitatsDisc {
	private static final String TAG = "UtilitatDisc";

	/**
	 * Elimina tots els fitxers del directori passat com argument.
	 * 
	 * @param folder
	 *            ruta completa del directori.
	 */
	public static void buidarDirectori(String folder) {
		buidarDirectori(new File(folder));
	}

	/**
	 * Elimina tots els fitxers del directori passat com argument.
	 * 
	 * @param folder
	 *            ruta completa del directori.
	 */
	public static void buidarDirectori(File folder) {
		// Si el directori no existeix no fem res.
		if (!folder.exists()) {
			return;
		}

		// Eliminem tots els arxius del directori
		String[] children = folder.list();
		for (int i = 0; i < children.length; i++) {
			new File(folder, children[i]).delete();
		}
	}
}
