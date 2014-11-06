package com.xaviergaro.tinytwit.utilitats;

/**
 * Interficie per implementar a les classes que volen realitzar alguna acci�
 * especial al enregistrar el ReceptorXarxa.
 * 
 * @author Javier Garc�a
 * @see ReceptorXarxa
 */
public interface UpdatableStatus {

	/**
	 * Quan el ReceptorXarxa canvia d'estat crida a aquest m�tode del context
	 * que se li ha passat com argument al constructor.
	 * @param estat true si el estat es connectat, o false si est� desconnectat
	 */
	void onUpdateStatus(boolean estat);
}
