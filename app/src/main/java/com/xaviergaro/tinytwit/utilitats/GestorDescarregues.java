package com.xaviergaro.tinytwit.utilitats;

public interface GestorDescarregues {

	/**
	 * Es crida al comen�ar i finalitzar una descarrega.
	 * 
	 * @param descarrega
	 *            cert al iniciar la descarrega i false al finaltizar.
	 * @see DownloadImageAsyncTask
	 */
	void descarregaEnCurs(boolean descarrega);
}
