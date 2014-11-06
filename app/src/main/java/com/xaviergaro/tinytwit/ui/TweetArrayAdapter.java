package com.xaviergaro.tinytwit.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xaviergaro.tinytwit.R;
import com.xaviergaro.tinytwit.activitats.MainActivity;
import com.xaviergaro.tinytwit.dades.Tweet;

import java.io.File;
import java.util.List;

/**
 * Adaptador personalitzat per mostrar la informaci� d'un tweet. Les dades
 * mostrades son la imatge del usuari al que pertany el Tweet, el seu nom
 * complet, la seva id, i el text del Tweet.
 * 
 * @author Javier Garc�a
 * @see Tweet
 */
public class TweetArrayAdapter extends ArrayAdapter<Tweet> {

	private static final String TAG = "MobileArrayAdapter";
	private final Context context;
	private final List<Tweet> tweets;

	/**
	 * Constructor de la classe, requereix una llista de objectes Tweet i el
	 * context de la aplicaci�.
	 * 
	 * @param context
	 *            aplicaci� a la que s'aplicar� l'adaptador.
	 * @param tweets
	 *            llista de Tweet per mostrar.
	 */
	public TweetArrayAdapter(Context context, List<Tweet> tweets) {
		super(context, R.layout.list_tweet, tweets);
		this.context = context;
		this.tweets = tweets;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Obtenim el layout
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.list_tweet, parent, false);

		// Obtenim la referencia als widgets
		TextView textViewName = (TextView) rowView.findViewById(R.id.name);
		TextView textViewId = (TextView) rowView.findViewById(R.id.id);
		TextView textViewText = (TextView) rowView.findViewById(R.id.text);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);

		// Apliquem els valors del Tweet als widgets
		Tweet tweet = tweets.get(position);
		textViewName.setText(tweet.nom);
		textViewId.setText("@" + tweet.idUsuari);
		textViewText.setText(tweet.text);
		imageView.setImageBitmap(carregarImatgeProfile(tweet));

		// Retornem la vista d'aquesta fila
		return rowView;
	}

	@Override
	public void clear() {
		// Netegem la llista
		tweets.clear();
		notifyDataSetChanged();
	}

	/**
	 * Carrega la imatge del perfil d'un tweet passat com argument.
	 * 
	 * @param tweet
	 *            del que s'ha de carregar la imatge.
	 * @return la imatge carregada com a Bitmap
	 */
	private Bitmap carregarImatgeProfile(Tweet tweet) {
		String filename = tweet.foto;
		File file = new File(context.getFilesDir()
				+ MainActivity.PROFILES_FOLDER, filename);
		Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
		return image;
	}

}
