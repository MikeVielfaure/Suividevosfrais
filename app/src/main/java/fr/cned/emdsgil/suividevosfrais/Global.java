package fr.cned.emdsgil.suividevosfrais;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.lang.reflect.Field;
import java.util.Hashtable;

abstract class Global {

    // tableau d'informations mémorisées
    public static Hashtable<Integer, FraisMois> listFraisMois = new Hashtable<>();
    /* Retrait du type de l'Hashtable (Optimisation Android Studio)
     * Original : Typage explicit =
	 * public static Hashtable<Integer, FraisMois> listFraisMois = new Hashtable<Integer, FraisMois>();
	*/

    // fichier contenant les informations sérialisées
    public static final String filename = "save.fic";

    /**
     * Modification de l'affichage de la date (juste le mois et l'année, sans le jour)
     */
    public static void changeAfficheDate(DatePicker datePicker, boolean afficheJours) {
        try {
            Field f[] = datePicker.getClass().getDeclaredFields();
            for (Field field : f) {
                int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
                datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), null);
                if (daySpinnerId != 0)
                {
                    View daySpinner = datePicker.findViewById(daySpinnerId);
                    if (!afficheJours)
                    {
                        daySpinner.setVisibility(View.GONE);
                    }
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    public static final void nonSaisieEditText(EditText editText) {
      editText.setFocusableInTouchMode(false);
      editText.setFocusable(false);
      editText.setLongClickable(false);
      editText.setCursorVisible(false);
      editText.setClickable(false);
      editText.setBackgroundColor(Color.TRANSPARENT);
    }


}
