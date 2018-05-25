/*
    Auteurs / Concepteurs :     BOUDREAULT, Alex
                                CHIASSON, Maxyme    -   www.linkedin.com/in/maxyme-chiasson/
                                LEBLANC, William    -   www.linkedin.com/in/william-leblanc/

    Dernière modification :     22 mai 2018

    Dans le cadre du cours :    420-W63-SI & 420-G64-SI
    Professeurs :               Guy Toutant - Yves Arsenault

    Projet remis à :            Institut Technologique de Maintenance Industrielle (ITMI)

    Copyright :                 Tous droits réservés. Le produit final est la propriété du Cégep de Sept-Îles,
                                de l'Institut Technologique de Maintenance Industrielle (ITMI) ainsi que les concepteurs
                                mentionnés ci-haut. Ceux-ci ont le droit d'alterér, distribuer, reproduire le produit final,
                                dans un éducatif et de recherche. Pour plus d'informations, veuillez contacter les concepteurs.

    Description :               Fichier noyau de l'application, dans lequel se retrouve toutes les fonctions de l'appli.

                                Lorsque l'usager lance l'application, l'écran principale s'affiche, contenant des informations
                                sur l'état de l'application et du GreenCube (s'il l'appareil est connecté au réseau sans-fil
                                du GreenCube) et un bouton permettant de télécharger les données à partir de l'ordinateur emmbarqué
                                du GreenCube.

                                Pour comprendre le fonctionnement et avoir une meilleure compréhension de l'appareil GreenCube,
                                pensez à consulter le guide d'utilisateur officiel sur Git.

    Liens GitHub :              //TODO - push le code sur le nouveau git et mettre l'url

    Note * :                    Notez que le code sur GitHub est affiché de façon publique, donc tout le monde
                                peut avoir accès au code. Cela est dû au compte gratuit de Git. Il faut débourser un montant
                                par mois afin de rendre le projet "privé".

*/

package com.Info420.ITMI.greencube;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

//Classe principale de l'application - Coeur de l'app
public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    //Déclare un bouton, qui servira à l'usager pour télécharger les données.
    Button buttonDownload;

    /*
        Déclare les préférences que les administrateurs de l'application (personnel de l'ITMI) vont pouvoir changer
        à leurs convenance.
    */
    SharedPreferences prefs;

    //Déclare un "Flag" qui détermine si le mode administrateur de l'application est activé ou non.
    private boolean modeAdmin = false;

    //Déclare un "Flag" qui détermine si l'envoie automatique des courriels se fait de façon automatique ou non.
    protected boolean envoieAuto = false;

    //Ce lance chaque fois que l'application est démarrée.
    @Override
    protected void onStart()
    {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(WifiStateChangedReceiver, intentFilter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(WifiStateChangedReceiver);
    }

    //Instancie un BroadcastReceiver à chaque détection d'un changement de connexion par l'appareil
    private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            final String action = intent.getAction();

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null)
            {
                //Wi-Fi activé
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //Affiche l'état de connexion à l'écran en vert
                            TextView wifiState = (TextView)findViewById(R.id.wifiState);
                            wifiState.setText(getApplicationContext().getString(R.string.wifiState));
                            wifiState.setTextColor(Color.GREEN);
                        }
                    });

                    //Détecte le nom du réseau
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    //Stocke le nom du réseau dans une variable pour un affichage à l'écran
                    final String wifiName = wifiManager.getConnectionInfo().getSSID();

                    /*
                        Teste si le réseau Wi-Fi auquel est connecté l'appareil n'est pas celui
                        du GreenCube (celui entrer dans les préférences) et si le nom du réseau
                        n'est pas détecté comme étant inconnue.
                    */
                    if (wifiName != null && !wifiName.contains("unknown ssid"))
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //Affiche le SSID du réseau à l'écran en blanc
                                TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                                nomssid.setText(wifiName);
                                nomssid.setTextColor(Color.WHITE);
                            }
                        });

                        //Si le Wi-Fi connecté est celui-ci du GreenCube (celui entrer dans les préférences).
                        if (wifiName.equals('"' + prefs.getString("SSID", "") + '"'))
                        {
                            //Active le bouton
                            buttonDownload.setEnabled(true);
                        }

                        //Si l'appareil n'est pas sur le Wi-Fi du GreenCube (celui des préférences).
                        else
                        {
                            //Désactive le bouton
                            buttonDownload.setEnabled(false);
                            File[] fichiers = getFilesDir().listFiles();

                            String choixPrefs = prefs.getString("delay", "c");

                            //Teste le choix de l'envoie automatique par Wi-Fi entrer dans les préférences.
                            //Si l'envoie automatique est autorisé, on entre dans la condition.
                            if (choixPrefs.equals("a") || choixPrefs.equals("b"))
                            {
                                //Déclare un "Flag" qui indique si l'envoie automatique a bel et bien procédé.
                                boolean confirmEnvoie = false;

                                //Boucle qui parcours la liste des fichiers.
                                for(int i = 0; i < fichiers.length; i++)
                                {
                                    //Test si le dernier caractère du fichier est la lettre "u", signifiant que le fichier n'as pas encore été envoyé.
                                    if(fichiers[i].getName().charAt(fichiers[i].getName().length() -1) == 'u')
                                    {
                                        //Appel de la fonction d'envoie automatique
                                        envoieAutomatique(fichiers[i]);
                                        //Change le "flag" d'envoie automatique pour être vrai.
                                        confirmEnvoie = true;
                                    }
                                }

                                //Test si l'envoie automatique à déjà été effectué.
                                if (confirmEnvoie)
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            //Affiche un message à l'usager pour lui confirmer que l'envoie automatique à bel et bien procédé.
                                            Toast message = Toast.makeText(getApplicationContext(), (getApplicationContext().getString(R.string.MessageConfirmationEnvoie1) +
                                                    " [" + prefs.getString("destinationUsername", "")) + "] " +
                                                    (getApplicationContext().getString(R.string.MessageConfirmationEnvoie2)), Toast.LENGTH_SHORT );
                                            message.show();
                                        }
                                    });
                                }
                            }
                        }
                    }
                    //Entre dans la condition si le SSID est reconnu comme étant "inconnu".
                    else
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //Affiche le SSID du réseau comme étant invalide, en jaune
                                TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                                nomssid.setText(getApplicationContext().getString(R.string.wifiInvalide));
                                nomssid.setTextColor(Color.YELLOW);
                            }
                        });
                    }
                }
                //Wi-Fi désactivé, mais connecté par LTE / 4G (données mobiles)
                             /*    Des frais peuvent s'appliquer      */
                else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //Désactive le bouton
                            buttonDownload.setEnabled(false);

                            //Affiche l'état de connexion à l'écran en jaune
                            TextView wifiState = (TextView)findViewById(R.id.wifiState);
                            wifiState.setText("LTE / 4G");
                            wifiState.setTextColor(Color.YELLOW);

                            //Efface le contenu des textview pour ne pas afficher de l'information erroné
                            TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                            nomssid.setText("");
                            TextView downState = (TextView)findViewById(R.id.downState);
                            downState.setText("");
                            TextView conxState = (TextView)findViewById(R.id.serState);
                            conxState.setText("");
                        }
                    });

                    //Récupère la liste des fichiers présents sur la mémoire interne de l'appareil
                    File[] fichiers = getFilesDir().listFiles();
                    //Récupère la préférence de l'envoie automatique et la stocke dans une variable
                    String choixPrefs = prefs.getString("delay", "c");

                    //Valide si l'admin à autorisé le transfert automatique par LTE
                    if (choixPrefs.equals("a"))
                    {
                        //Déclare un "flag" indiquant si l'envoie automatique à été effectué.
                        boolean confirmEnvoie = false;

                        //Boucle qui parcours la liste des fichiers
                        for(int i = 0; i < fichiers.length; i++)
                        {
                            //Test si le dernier caractère du fichier est la lettre "u", signifiant que le fichier n'as pas encore été envoyé.
                            if(fichiers[i].getName().charAt(fichiers[i].getName().length() -1) == 'u')
                            {
                                //Appel de la fonction d'envoie automatique
                                envoieAutomatique(fichiers[i]);
                                //Change le flag pour la valeur vrai.
                                confirmEnvoie = true;
                            }
                        }

                        //Test si l'envoie automatique s'est bel et bien effectué.
                        if (confirmEnvoie)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //Affiche un message à l'écran pour notifier à l'usager de la réussite de l'envoie automatique.
                                    Toast message = Toast.makeText(getApplicationContext(), (getApplicationContext().getString(R.string.MessageConfirmationEnvoie1) +
                                            " [" + prefs.getString("destinationUsername", "")) + "] " +
                                            (getApplicationContext().getString(R.string.MessageConfirmationEnvoie2)), Toast.LENGTH_SHORT );
                                    message.show();
                                }
                            });
                        }
                    }
                }
            }

            //Aucune connexion détecté, autant par Wi-Fi que par données mobiles.
            else
            {
                //Ouvre un thread
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Désactive le bouton
                        buttonDownload.setEnabled(false);

                        //Afiche l'état de connexion à l'écran en rouge
                        TextView wifiState = (TextView)findViewById(R.id.wifiState);
                        wifiState.setText("N/A");
                        wifiState.setTextColor(Color.RED);

                        //Efface le contenu des textview pour ne pas afficher de l'information erroné
                        TextView nomssid = (TextView)findViewById(R.id.nomSSID);
                        nomssid.setText("");
                        TextView downState = (TextView)findViewById(R.id.downState);
                        downState.setText("");
                        TextView conxState = (TextView)findViewById(R.id.serState);
                        conxState.setText("");

                        //Affiche un message à l'usager indiquant qu'aucune connexion n'a été détectée
                        Toast toast = Toast.makeText(getApplicationContext(),
                                (getApplicationContext().getString(R.string.NoConnection)), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.registerReceiver(this.WifiStateChangedReceiver,
                new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        buttonDownload = (Button)findViewById(R.id.button);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar myToolbar=(Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("");

        setSupportActionBar(myToolbar);

        //Definit le titre de la toolbar et sa couleur
        myToolbar.setTitle(R.string.app_name);
        myToolbar.setTitleTextColor(Color.WHITE);

        buttonDownload.setOnClickListener(this);
    }

    public String getDate(String timestamp)
    {
        //Definit le format de la date
        DateFormat date = new SimpleDateFormat("yyyy.MM.dd " + "HH:mm:ss"); //TODO - changer le format ?

        //Obtient la date
        Date chaine_date = (new Date(Long.parseLong(timestamp)));

        //Retourne la date dans le bon format
        return date.format(chaine_date);
    }

    @Override
    public void onClick (View view)
    {
        final Long time = System.currentTimeMillis();
        final String timestamp = time.toString();


        new Thread()
        {
            public void run()
            {
                //Obtient l'addresse du serveur FTP dans les préférences
                final String adresse = prefs.getString("adresse", "192.168.1.2");
                //Obtient le ssid du serveur dans les préférences
                final String ssid = prefs.getString("SSID", "Green Cube 2.4GHz");
                //Obtient le nom d'usager dans les préférences
                final String username = prefs.getString("username", "administrateur");
                //Obtient le mot de passe dans les préférences
                final String password = prefs.getString("password", "Ubuntu2018");
                //Obtient le nom du fichier dans les préférences
                final String filename = prefs.getString("filename", "test.csv");
                //Obtient le chemin vers le fichier dans les préférences
                final String Path = prefs.getString("filepath", "/administrateur/home/");

                BufferedInputStream buffIn;

                FTPClient ftp = new FTPClient();

                try
                {
                    //Connexion au server ftp avec le port 21
                    ftp.connect(adresse, 21);

                    //Login au serveur avec le mot de passe et le nom d'utilisateur
                    ftp.login(username, password);

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView conxState = (TextView)findViewById(R.id.serState);
                            conxState.setText("OK !");
                            conxState.setTextColor(Color.GREEN);
                        }
                    });

                    //On affecte le chemin au répertoire contenant le fichier
                    ftp.changeWorkingDirectory(Path);

                    //On affecte le type de fichier
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    //On fait passer le serveur FTP en mode passif
                    ftp.enterLocalPassiveMode();

                    //On déclare un OutputStream pour téléchargé le fichier
                    OutputStream os = new FileOutputStream(new File(getFilesDir(), filename));

                    //On télécharge le fichier
                    boolean resultat = ftp.retrieveFile(filename, os);

                    if(resultat == true)
                    {
                        //On va chercher le fichier
                        File original = new File(getFilesDir(),filename);
                        //On rajoute la lettre "u" à la fin du fichier pour "unsend", le fichier n'étant pas envoyé.
                        final File renommer = new File(getFilesDir(), getDate(timestamp) + "u");

                        //On renomme le fichier
                        original.renameTo(renommer);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                TextView downState = (TextView)findViewById(R.id.downState);
                                downState.setText("OK !");
                                downState.setTextColor(Color.GREEN);

                                //Affiche à l'usager un message indiquant que le fichier est bel et bien télécharger, en lui indiquant le nom du fichier en question
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        (getApplicationContext().getString(R.string.MessageTelecharger1)) +
                                                " ['" + renommer.getName().substring(0, renommer.getName().length() - 1) + "'] " +
                                                (getApplicationContext().getString(R.string.MessageTelecharger2)), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }

                    //Si le fichier n'a pas été téléchargé
                    else
                    {
                        //On indique à l'utilisateur que le fichier n'a pas été téléchargé
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                TextView downState = (TextView)findViewById(R.id.downState);
                                downState.setText("fail !");
                                downState.setTextColor(Color.RED);

                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getApplicationContext().getString(R.string.ErrorDownload), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
                    //Logout du ftp
                    ftp.logout();
                    //On se déconnecte du serveur
                    ftp.disconnect();
                }

                //S'il y a une erreur de connexion
                catch (SocketException e)
                {
                    //On indique l'échec à l'utilisateur
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView conxState = (TextView)findViewById(R.id.serState);
                            conxState.setText("fail !");
                            conxState.setTextColor(Color.RED);
                        }
                    });
                    e.printStackTrace();
                }

                //S'il y a une erreur de fichier
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        return true;
    }

    @Override
    //Fonction qui permet de gérer l'appuie sur le menu par l'usager
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //Instancie les fenêtres de préférences et de téléchargement, afin de pouvoir les ouvrir selon le bouton appuyé.
        final Intent intentPrefsActivity = new Intent(this, PrefsActivity.class);
        Intent intentTelechargementActivity = new Intent(this, Telechargement.class);

        //Selon le cas...
        switch (item.getItemId())
        {
            //L'usager appuie sur préférences
            case R.id.itemPreference:
                //Si le mode administrateur n'est pas activé
                if (modeAdmin == false)
                {
                    //Créer une fenêtre, invitant l'usager à entrer le mot de passe de l'administrateur, afin d'accèder à la fenêtre des préférences
                    View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.user_input, null);
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this).setMessage(getApplicationContext().getString(R.string.MessageMDP));
                    alertBuilder.setView(view);
                    final EditText userInput = (EditText) view.findViewById(R.id.userinput);
                    userInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    //Entre dans la fonction lorsque l'usager appuie sur "Ok" dans la fenêtre
                    alertBuilder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Stock le mot de passe des préférences et celui entrer par l'usager dans des variables
                            final String mdp =  prefs.getString("adminPassword", "");
                            final String saisie = (userInput.getText().toString());

                            //Test si le mot de passe entrer correspond à celui des préférences
                            if (saisie.equals(mdp))
                            {
                                //Active le mode administrateur
                                modeAdmin = true;

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //Affiche un message informant l'usager que le mode adminitrateur est activé.
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                getApplicationContext().getString(R.string.BonMDP), Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                                //Lance la fenêtre des préférences
                                startActivity(intentPrefsActivity);
                            }
                            //Mauvais mot de passe entrer
                            else
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //Affiche un message informant l'usager de l'échec de sa tentative de connexion
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                getApplicationContext().getString(R.string.MauvaisMDP), Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                            }
                        }
                    });

                    Dialog dialog = alertBuilder.create();
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    dialog.show();
                }

                //Test si le mode administrateur est activée
                if (modeAdmin == true)
                {
                    //Si oui, on appel la fenêtre des préférences
                    startActivity(intentPrefsActivity);
                }

                return true;

            //L'usager appuie sur la touche de téléchargement.
            case R.id.itemTelechargement:
                startActivity(intentTelechargementActivity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Fonction permettant l'envoie des mail automatique.
    private void envoieAutomatique(File tempo)
    {
        //Récupère les préférences et les stockes dans des variables
        final String username = prefs.getString("sourceUsername", "");
        final String password = prefs.getString("sourcePassword", "");
        final String file = tempo.getName();

        new Thread()
        {
            public void run()
            {
                //Déclare les propriétés pour l'envoie de mail
                Properties props = new Properties();

                //Définie les propriétés pour l'envoie
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                //Déclare une session pour la connexion au compte de du destinataire.
                Session session = Session.getInstance(props, new Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        //Envoie les informations des préférences
                        return new PasswordAuthentication(username, password);
                    }
                });

                //Try/Catch qui gère l'envoie du message
                try
                {

                    //Créer le message et lui définie son récepteur et son objet
                    Message message = new MimeMessage(session);
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress("testitmi2@gmail.com"));
                    message.setSubject((getApplicationContext().getString(R.string.ObjetMessage)) + " [" + file.substring(0, file.length() - 1) + "]");

                    //Affecte le corps du message (le texte) --- !!! ---
                    //TODO - Pour l'instant, il est impossible d'ajouter un corps de message si une pièce jointe est déjà attachée à celui-ci. Si nous le faisons,  l'envoie ne fonctionne pas.
                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    Multipart multipart = new MimeMultipart();

                    //Déclare un nouveau fichiers
                    File fichier = new File(getFilesDir(), file);

                    //Procédure qui va chercher le fichier CSV dans la mémoire interne de l'appareil
                    String file = getApplicationContext().getFilesDir() + "/" + fichier.getName();
                    String attachement = fichier.getName().substring(0, fichier.getName().length() - 1) +".csv";

                    //Procédure qui permet d'attacher notre fichier CSV en pièce jointe dans le courriel
                    FileDataSource source = new FileDataSource(file);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(attachement);
                    multipart.addBodyPart(messageBodyPart);
                    message.setContent(multipart);

                    //Fonction qui envoie le message
                    Transport.send(message);

                    //Change le flag pour la valeur vrai
                    envoieAuto = true;

                    //Procédure qui renomme le fichier avec un "e" à la fin, indiquant qu'il à été envoyé.
                    File fichierNouveau = new File(getFilesDir(), fichier.getName().substring(0, fichier.getName().length() - 1) + "e");
                    fichier.renameTo(fichierNouveau);
                }

                //Dans le cas d'une erreur
                catch(MessagingException e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //Affiche un message à l'usager, le notifiant que l'envoie automatique à échoué
                            Toast mailFail = Toast.makeText(getApplicationContext(),
                                    getApplicationContext().getString(R.string.mailFail), Toast.LENGTH_SHORT);
                            mailFail.show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
