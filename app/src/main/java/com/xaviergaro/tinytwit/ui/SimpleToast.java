package com.xaviergaro.tinytwit.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xaviergaro.tinytwit.R;

/**
 * Classe per mostrar un toast personalitzat. Per evitar tenir que indicar el
 * context en cada crida s'ha de instanciar aquesta classe i passar el context
 * al constructor.
 * 
 * @author Javier Garc�a
 * 
 */
public class SimpleToast {
	private static final String TAG = "SimpleToast";
	
	private Context context;

	/**
	 * Constructor de la classe on s'estableix el context al que s'aplicar� el
	 * toast.
	 * 
	 * @param context
	 *            activitat a la que s'aplicar� el toast.
	 */
	public SimpleToast(Context context) {
		this.context = context;
	}

	/**
	 * Mostra un toast amb el missatge corresponent al recurs String amb el id
	 * indicat.
	 * 
	 * @param text
	 *            id del recurs string a mostrar.
	 */
	public void toast(int text) {
		toast(context.getResources().getString(text));
	}

	/**
	 * Mostra un toast amb el missatge passat com argument.
	 * 
	 * @param text
	 *            missatge a mostrar.
	 */
	public void toast(String text) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.toast_layout,
				(ViewGroup) ((Activity) context)
						.findViewById(R.id.toast_layout_root));

		TextView textView = (TextView) layout.findViewById(R.id.text);
		textView.setText(text);
		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
}
