package fr.cned.emdsgil.suividevosfrais.Vue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fr.cned.emdsgil.suividevosfrais.Modele.AccesDistant;
import fr.cned.emdsgil.suividevosfrais.Modele.FraisMois;
import fr.cned.emdsgil.suividevosfrais.Outils.Global;
import fr.cned.emdsgil.suividevosfrais.R;
import fr.cned.emdsgil.suividevosfrais.Outils.Serializer;

public class MainActivity extends AppCompatActivity {

  // propriétés pour l'état de l'enregistrement sur la BDD distante
  private static final int STATE_ERREUR = 3;
  private static final int STATE_ENREGISTRE = 4;
  private static final int STATE_NON_ENREGISTRE = 5;
  private static final int STATE_NON_FRAIS = 6;

  // propriété pour l'envoi des informations sur la BDD
  private List list = new ArrayList();

  // propriétés qui va agir en fonction de l'état de la connexion reçu depuis l'AccesDistant
  public Handler handler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(@NonNull Message msg) {
      switch(msg.what){
        case STATE_ENREGISTRE:
          Toast.makeText(getBaseContext(), "Enregistré", Toast.LENGTH_SHORT).show();
          break;
        case STATE_NON_ENREGISTRE:
          Toast.makeText(getBaseContext(), "Impossible d'enregistré", Toast.LENGTH_SHORT).show();
          break;
        case STATE_ERREUR:
          Toast.makeText(getBaseContext(), "erreur", Toast.LENGTH_SHORT).show();
          break;
        case STATE_NON_FRAIS:
          Toast.makeText(getBaseContext(), "pas de frais a envoyer", Toast.LENGTH_SHORT).show();
          break;
      }
      return true;
    }
  });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("GSB : Suivi des frais");
        // récupération des informations sérialisées
        recupSerialize();
        // chargement des méthodes événementielles
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdKm)), KmActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdHf)), HfActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdHfRecap)), HfRecapActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdNuitee)), NuiteeActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdEtape)), EtapesActivity.class);

        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdRepas)), RepasActivity.class);

        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdTransfert)), AuthentificationActivity.class);
        cmdTransfert_clic();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Récupère la sérialisation si elle existe
     */
    private void recupSerialize() {
        /* Pour éviter le warning "Unchecked cast from Object to Hash" produit par un casting direct :
         * Global.listFraisMois = (Hashtable<Integer, FraisMois>) Serializer.deSerialize(Global.filename, MainActivity.this);
         * On créé un Hashtable générique <?,?> dans lequel on récupère l'Object retourné par la méthode deSerialize, puis
         * on cast chaque valeur dans le type attendu.
         * Seulement ensuite on affecte cet Hastable à Global.listFraisMois.
        */

        Hashtable<?, ?> monHash = (Hashtable<?, ?>) Serializer.deSerialize(MainActivity.this);
        if (monHash != null) {
            Hashtable<Integer, FraisMois> monHashCast = new Hashtable<>();
            for (Hashtable.Entry<?, ?> entry : monHash.entrySet()) {
                monHashCast.put((Integer) entry.getKey(), (FraisMois) entry.getValue());
            }
            Global.listFraisMois = monHashCast;
        }
        // si rien n'a été récupéré, il faut créer la liste
        if (Global.listFraisMois == null) {
            Global.listFraisMois = new Hashtable<>();
            /* Retrait du type de l'HashTable (Optimisation Android Studio)
			 * Original : Typage explicit =
			 * Global.listFraisMois = new Hashtable<Integer, FraisMois>();
			*/

        }
    }

    /**
     * Sur la sélection d'un bouton dans l'activité principale ouverture de l'activité correspondante
     */
    private void cmdMenu_clic(ImageButton button, final Class classe) {
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // ouvre l'activité
                Intent intent = new Intent(MainActivity.this, classe);
                startActivity(intent);
            }
        });
    }

    /**
     * Cas particulier du bouton pour le transfert d'informations vers le serveur
     */
    private void cmdTransfert_clic() {
        findViewById(R.id.cmdTransfert).setOnClickListener(new Button.OnClickListener() {
          public void onClick(View v) {
            // vérifi qu'il y a des frais a envoyer
            if (Global.listFraisMois.size() != 0) {
              // envoi les informations sérialisées vers le serveur
              // en construction
              //Log.d("enreg","****************"+Global.listFraisMois.get(2021*100+1).getLesFraisHf());
              donneesAEnvoyer();
              JSONArray jsonList = new JSONArray(list);
              AccesDistant accesDistant = new AccesDistant(handler);
              // envoi au serveur distant les information necessaire pour l'authentification
              accesDistant.envoi("enreg", jsonList);
            } else {
              // on envoit au handler l'état de liste de frais pour indiquer qu'on a rien a envoyer
              Message msg = new Message();
              msg.what = STATE_NON_FRAIS;
              handler.sendMessage(msg);
            }
          }
        });
      }


  /**
   * ajoute dans une liste toute les information à envoyer
   *
   */
    public void donneesAEnvoyer(){
      String frais ="";
      list.clear();
      // on ajoute les données sérialisées d'authentification à la liste
      list.add(Serializer.deSerialize("identifiant", this).toString());
      list.add(Serializer.deSerialize("mdp", this).toString());
      Set keys = Global.listFraisMois.keySet();
      //obtenir un iterator des clés
      Iterator itr = keys.iterator();
      Object key;
      //affichage des pairs clé-valeur
      while (itr.hasNext()) {
        frais="";
        // obtenir la clé
        key = itr.next();
          frais +="m"+Global.listFraisMois.get(key).getMois().toString()+
            "&KM"+Global.listFraisMois.get(key).getKm().toString()+
            "&REP"+Global.listFraisMois.get(key).getRepas().toString()+
            "&NUI"+Global.listFraisMois.get(key).getNuitee().toString()+
            "&ETP"+Global.listFraisMois.get(key).getEtape().toString();
        for (int k = 0; k<Global.listFraisMois.get(key).getLesFraisHf().size();k++) {
          frais +="&HF"+Global.listFraisMois.get(key).getLesFraisHf().get(k).getJour().toString()+
            "|A"+Global.listFraisMois.get(key).getAnnee().toString()+
            "|MOT"+ Global.listFraisMois.get(key).getLesFraisHf().get(k).getMotif().toString()+
            "|MON"+Global.listFraisMois.get(key).getLesFraisHf().get(k).getMontant().toString();
        }
        list.add(frais);
      }
    }




}
