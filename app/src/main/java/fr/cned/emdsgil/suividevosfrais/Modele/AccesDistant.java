package fr.cned.emdsgil.suividevosfrais.Modele;

import android.os.Handler;
import android.os.Message;
import android.util.Log;



import org.json.JSONArray;

import fr.cned.emdsgil.suividevosfrais.Outils.AccesHTTP;
import fr.cned.emdsgil.suividevosfrais.Outils.AsyncResponse;

/**
 * Created by emds on 25/02/2018.
 */

public class AccesDistant implements AsyncResponse {

    // constante
      private static final String SERVERADDR ="https://saisiedevosfrais.alwaysdata.net/serveursuividevosfrais.php";
    //private static final String SERVERADDR = "http://192.168.43.190/Suividevosfrais/serveursuividevosfrais.php";

    private static final int STATE_VALIDE = 1;
    private static final int STATE_NON_VALIDE = 2;
    private static final int STATE_ERREUR = 3;
    private static final int STATE_ENREGISTRE = 4;
    private static final int STATE_NON_ENREGISTRE = 5;
    private Handler mHandler;


    /**
     * Constructeur
     */
    public AccesDistant(Handler handler){
      this.mHandler = handler;
      //super();
    }

    /**
     * Retour du serveur HTTP
     * @param output
     */
    @Override
    public void processFinish(String output) {

        // pour vérification, affiche le contenu du retour dans la console
        Log.d("serveur", "************" + output);
        // découpage du message reçu
        String[] message = output.split("%");
        // contrôle si le retour est correct (au moins 2 cases)
        if(message.length>0){
            if(message[0].equals("authentification")){
                Log.d("enreg","****************"+message[1]);
                if(message[1].equals("non valide !")) {
                  Message msg = new Message();
                  msg.what = STATE_NON_VALIDE;
                  mHandler.sendMessage(msg);
                }
                if (message[1].equals("valide !")){
                  Message msg = new Message();
                  msg.what = STATE_VALIDE;
                  mHandler.sendMessage(msg);
                }
                if (message[1].equals("Erreur !")){
                  Message msg = new Message();
                  msg.what = STATE_ERREUR;
                  mHandler.sendMessage(msg);
              }
            }else{
                if(message[0].equals("enreg")){
                  //ici code
                  Log.d("dernier","****************"+message[0]);
                  //Message msg = new Message();
                  //msg.what = STATE_ENREGISTRE;
                  //mHandler.sendMessage(msg);
                  if(message.length>1){
                    Message msg = new Message();
                    msg.what = STATE_ERREUR;
                    mHandler.sendMessage(msg);
                  }else {
                    Message msg = new Message();
                    msg.what = STATE_ENREGISTRE;
                    mHandler.sendMessage(msg);
                  }
                }else{
                  //if(message[0].equals("")) {
                    Message msg = new Message();
                    msg.what = STATE_ERREUR;
                    mHandler.sendMessage(msg);
                  //}
                }
            }
        }else{
          Message msg = new Message();
          msg.what = STATE_ERREUR;
          mHandler.sendMessage(msg);
        }
    }

    /**
     * Envoi de données vers le serveur distant
     * @param operation information précisant au serveur l'opération à exécuter
     * @param lesDonneesJSON les données à traiter par le serveur
     */

    public void envoi(String operation, JSONArray lesDonneesJSON){
        AccesHTTP accesDonnees = new AccesHTTP();
        // lien avec AccesHTTP pour permettre à delegate d'appeler la méthode processFinish
        // au retour du serveur
        accesDonnees.delegate = this;
        // ajout de paramètres dans l'enveloppe HTTP
        accesDonnees.addParam("operation", operation);
        accesDonnees.addParam("lesdonnees", lesDonneesJSON.toString());
        // envoi en post des paramètres, à l'adresse SERVERADDR
        accesDonnees.execute(SERVERADDR);
    }

}
