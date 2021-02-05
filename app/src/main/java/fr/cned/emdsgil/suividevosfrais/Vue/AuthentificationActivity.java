package fr.cned.emdsgil.suividevosfrais.Vue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import fr.cned.emdsgil.suividevosfrais.Modele.AccesDistant;
import fr.cned.emdsgil.suividevosfrais.Outils.Serializer;
import fr.cned.emdsgil.suividevosfrais.R;

public class AuthentificationActivity extends AppCompatActivity {

  // propriétés pour l'authentification
  private String identifiant;
  private String motDePasse;
  private List list = new ArrayList();

  // propriétés pour l'état de l'authentification
  private static final int STATE_VALIDE = 1;
  private static final int STATE_NON_VALIDE = 2;
  private static final int STATE_ERREUR = 3;

  // propriétés qui va agir en fonction de l'état de la connexion reçu depuis l'AccesDistant
  public Handler handler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(@NonNull Message msg) {
      switch(msg.what){
        case STATE_VALIDE:
          Toast.makeText(getBaseContext(), "Connecté", Toast.LENGTH_SHORT).show();
          retourActivityPrincipale();
          break;
        case STATE_NON_VALIDE:
          Toast.makeText(getBaseContext(), "Identifiant non trouvé", Toast.LENGTH_SHORT).show();
          ((TextView)findViewById(R.id.connectionEnCours)).setText("");
          break;
        case STATE_ERREUR:
          Toast.makeText(getBaseContext(), "erreur de connexion", Toast.LENGTH_SHORT).show();
          ((TextView)findViewById(R.id.connectionEnCours)).setText("");
          break;
      }
      return true;
    }
  });


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_authentification);
    setTitle("GSB : Authentification");

    //deleteFile("save.fic");//efface la liste de frais sérialisée
    // préremplis les champs de connexion
    try {
      ((EditText) findViewById(R.id.txtSaisieIdentifiant)).setText((Serializer.deSerialize("identifiant", this).toString()));
      ((EditText) findViewById(R.id.txtSaisieMDP)).setText((Serializer.deSerialize("mdp", this).toString()));
    }catch (Exception e) { }

    cmdValider_clic();
  }

  /**
   * Sur le clic du bouton valider : authentification sur le serveur
   */
  private void cmdValider_clic() {
    findViewById(R.id.cmdAuthentificationValider).setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        // récupération de l'identifiant et du mot de passe
        identifiant = ((EditText)findViewById(R.id.txtSaisieIdentifiant)).getText().toString();
        motDePasse = ((EditText)findViewById(R.id.txtSaisieMDP)).getText().toString();
        // Sérialisation de l'identifiant et du MDP
        Serializer.serialize("identifiant",identifiant,getBaseContext());
        Serializer.serialize("mdp", motDePasse, getBaseContext());
        // ajout de l'identifiant et du mot de passe dans une liste
        list.clear();
        list.add(identifiant);
        list.add(motDePasse);
        JSONArray jsonList = new JSONArray(list);
        AccesDistant accesDistant = new AccesDistant(handler);
        // envoi au serveur distant les information necessaire pour l'authentification
        accesDistant.envoi("authentification",jsonList);
        // affichage du chargement
        ((TextView)findViewById(R.id.connectionEnCours)).setText("Connection en cours...");
      }
    }) ;
  }



  /**
   * Retour à l'activité principale (le menu)
   */
  private void retourActivityPrincipale() {
    Intent intent = new Intent(AuthentificationActivity.this, MainActivity.class) ;
    startActivity(intent) ;
    this.finish();
  }
}
