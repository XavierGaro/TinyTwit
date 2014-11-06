package com.xaviergaro.tinytwit.utilitats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xaviergaro.tinytwit.R;
import com.xaviergaro.tinytwit.ui.SimpleToast;

/**
 * Aquesta classe s'encarrega de controlar si est� disponible o no la connexi� a
 * internet, enregistrar-se i desenregistrar-se. Quan l'estat de la connexi�
 * canvia ho comunica a la aplicaci�.
 * 
 * Si la classe que l'instancia implementa UpdatableStatus
 * 
 * 
 * @author Javier Garc�a
 * @see UpdatableStatus
 * 
 */
public class ReceptorXarxa extends BroadcastReceiver {
	private static final String TAG = "ReceptorXarxa";

	private static final int MISSATGE_PER_DEFECTE = R.string.error_connexio;

	private Context context;
	private ConnectivityManager connMgr;
	private NetworkInfo networkInfo;

	private SimpleToast toaster;

	public ReceptorXarxa(Context context) {
		this.context = context;
		toaster = new SimpleToast(context);
	}

	/**
	 * Retorna l'estat de la connexi�.
	 * 
	 * @return true si est� connectat o false si no ho est�
	 */
	public boolean comprovarConnexioSilencios() {
		return comprovarConnexio("");
	}

	/**
	 * Retorna l'estat de la connexi� i mostra el toast amb el missatge per
	 * defecte.
	 * 
	 * @return true si est� connectat o false si no ho est�
	 */
	public boolean comprovarConnexio() {
		return comprovarConnexio(MISSATGE_PER_DEFECTE);
	}

	/**
	 * Retorna l'estat de la connexi� i mostra el toast amb el missatge per
	 * defecte.
	 * 
	 * @param id
	 *            id del missatge d'error que es mostrar� si no hi ha connexi�.
	 * @return true si est� connectat o false si no ho est�
	 */
	boolean comprovarConnexio(int id) {
		return comprovarConnexio(context.getResources().getString(id));
	}

	/**
	 * Retorna l'estat de la connexi� i mostra el toast amb el missatge per
	 * defecte.
	 * 
	 * @param missatge
	 *            missatge d'error que es mostrar� si no hi ha connexi�.
	 * @return
	 */
	boolean comprovarConnexio(String missatge) {
		// Obtenim un gestor de les conexions de xarxa del context
		connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		// Obtenim l'estat de la xarxa
		networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			return true;

		} else {
			if (missatge != null && missatge.length() > 0) {
				toaster.toast(missatge);
			}
			return false;
		}
	}

	/**
	 * Enregistra el receptor a la activitat que l'ha creat.
	 */
	public void enregistrar() {
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(this, filter);
	}

	/**
	 * Dona de baixa al receptor broadcast de la activitat que l'ha creat.
	 */
	public void desenregistrar() {
		try {
			context.unregisterReceiver(this);
		} catch (IllegalArgumentException e) {
			// Afegit per evitar possibles errors de sincronitzaci�
		}
	}

	@Override
	public void onReceive(Context c, Intent i) {
		boolean estat = comprovarConnexioSilencios();

		// Si la classe que l'ha creat implementa UpdateableStatus, crida al
		// metode updateStatus al rebre canvis.
		if (context instanceof UpdatableStatus) {
			((UpdatableStatus) context).onUpdateStatus(estat);
		}
	}
}
