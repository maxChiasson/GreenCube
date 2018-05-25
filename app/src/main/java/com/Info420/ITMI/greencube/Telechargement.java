/*
    Auteurs / Concepteurs :     BOUDREAULT, Alex
                                CHIASSON, Maxyme    -   www.linkedin.com/in/maxyme-chiasson/
                                LEBLANC, William    -   www.linkedin.com/in/william-leblanc/

    Dernière modification :     16 mai 2018

    Dans le cadre du cours :    420-W63-SI & 420-G64-SI
    Professeurs :               Guy Toutant - Yves Arsenault

    Projet remis à :            Institut Technologique de Maintenance Industrielle (ITMI)

    Copyright :                 Tous droits réservés. Le produit final est la propriété du Cégep de Sept-Îles,
                                de l'Institut Technologique de Maintenance Industrielle (ITMI) ainsi que les concepteurs
                                mentionnés ci-haut. Ceux-ci ont le droit d'alterér, distribuer, reproduire le produit final,
                                dans un éducatif et de recherche. Pour plus d'informations, veuillez contacter les concepteurs.

    Description :               Fichier contenant les fonctions permettant de s'authentifier
                                au serveur FTP, de récupérer le fichier de données, le copier sur la
                                mémoire interne de l'appareil mobile et de fermer la connexion.

    Note * :                    Notez que le code sur GitHub est affiché de façon publique, donc tout le monde
                                peut avoir accès au code. Cela est dû au compte gratuit de Git. Il faut débourser un montant
                                par mois afin de rendre le projet "privé".

*/

package com.Info420.ITMI.greencube;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
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

public class Telechargement extends AppCompatActivity
{
    //Liste qui contiendra les noms des fichiers de la mémoire interne de l'appareil
    ArrayList<String> nom_partager = new ArrayList<String>();

    //Variable contenant les préférences
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //On affecte le layout à l'activité
        setContentView(R.layout.liste_telechargement);

        //On affecte le layout de la barre d'outils à l'activité
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);

        //On donne un titre à la barre d'outils
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        //On va chercher les préférences
        myToolbar.setTitle(R.string.titleTelechargement);
        myToolbar.setTitleTextColor(Color.WHITE);

        //On va chercher les préférences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //On va chercher les fichiers qui se trouvent dans le dossier de la mémoire interne de l'application
        File[] fichiers = getApplicationContext().getFilesDir().listFiles();

        //On déclare une liste de fichier pour le custom Adapter
        ArrayList<Nom_Fichier> liste_fichier = new ArrayList<>();

        //On déclare un adaptateur pour bien affiché la liste de fichier
        Fichier_Adapteur adaptateur = new Fichier_Adapteur(this, liste_fichier);

        //On se déclare un ListView pour affiché la liste de fichier
        ListView list_view = (ListView) findViewById(R.id.outputTelechargement);

        //On affecte l'adaptateur au ListView
        list_view.setAdapter(adaptateur);

        //On vide la liste de nom de fichiers
        nom_partager.clear();

        //Pour chaque élément de la liste de fichier
        for(int i = 0; i < fichiers.length; i++)
        {
                //On va chercher le nom du fichier dans la liste
                String tempo = fichiers[i].getName();

                //On ajoute le nom de fichier à la liste
                nom_partager.add(tempo);

                //On enlève le dernier caractère du nom du fichier pour ne pas que les 'e' ou les 'u' soit afficher
                tempo = tempo.substring(0, tempo.length() - 1);

                //On déclare un nouvel objet Nom_Fichier
                Nom_Fichier fichier = new Nom_Fichier(tempo);

                //On ajoute l'objet Nom_Fichier dans l'adaptateur
                adaptateur.add(fichier);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    //Classe utilisée pour contenir le nom des fichiers dans la liste
    private class Nom_Fichier
    {
        //Variable qui contiendra le nom du fichier
        public String fichier;

        //Variable utilisée pour savoir s'il faut renommer le fichier après l'envoi
        public int compteur;

        //Constructeur qui reçoit une chaîne contenant un nom de fichier en paramètre
        public Nom_Fichier(String fichier)
        {
            //On affecte le nom du fichier
            this.fichier = fichier;

            //On met le compteur à 0
            this.compteur = 0;
        }
    }

    //Custom Adapter utilisé pour afficher les éléments dans le ListView selon le layout de rangée
    private class Fichier_Adapteur extends ArrayAdapter<Nom_Fichier>
    {
        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            //On se déclare un Nom_Fichier pour aller écrire à la bonne position dans la liste
            final Nom_Fichier nom_fichier = getItem(position);

            if(convertView == null)
            {
                //On déclare le layout de rangée
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.row, parent, false);
            }

            //On déclare le TextView qui contient le nom du fichier
            final TextView txt_fichier = (TextView) convertView.findViewById(R.id.nom_telechargement);

            //On déclare le TextView qui contient le status envoyé ou non-envoyé
            final TextView txt_envoie = (TextView) convertView.findViewById(R.id.envoieView);

            //On assgine le nom du fichier au TextView
            txt_fichier.setText(nom_fichier.fichier);

            //Si le fichier est enovyé
            if(nom_partager.get(position).charAt(nom_partager.get(position).length() - 1) == 'e')
            {
                txt_envoie.setText(getApplicationContext().getString(R.string.messageEnvoye));
            }
            //Si le fichier est non-envoyé
            else if(nom_partager.get(position).charAt(nom_partager.get(position).length() - 1) == 'u')
            {
                txt_envoie.setText(getApplicationContext().getString(R.string.messageNonEnvoye));
            }

            //On déclare le bouton pour envoyer les fichiers par courriel
            Button bouton_email = (Button) convertView.findViewById(R.id.bt_envoyer);

            //On déclare le bouton pour supprimer les fichiers
            Button bouton_supprimer = (Button) convertView.findViewById(R.id.bt_delete);

            //Listener utilisé pour envoyer les fichiers par courriel
            bouton_email.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //Va chercher le nom d'usager du couriel source dans les préférences
                    final String username = prefs.getString("sourceUsername", "");
                    //Va chercher le mdp du courriel source dans les préférences
                    final String password = prefs.getString("sourcePassword", "");
                    //Va chercher l'addresse courriel de destination dans les préférences
                    final String usernameDest = prefs.getString("destinationUsername", "");

                    new Thread()
                    {
                        public void run()
                        {
                            //On déclare une variable qui contient les propriétés
                            Properties props = new Properties();

                            //On crée des propriétés pour se connecter aux serveurs Gmail
                            props.put("mail.smtp.auth", "true");
                            props.put("mail.smtp.starttls.enable", "true");
                            props.put("mail.smtp.host", "smtp.gmail.com");
                            props.put("mail.smtp.port", "587");

                            //On ouvre une session avec le courriel source et le mot de passe du courriel source
                            Session session = Session.getInstance(props, new Authenticator()
                            {
                                protected PasswordAuthentication getPasswordAuthentication()
                                {
                                    return new PasswordAuthentication(username, password);
                                }
                            });

                            try
                            {
                                //On crée un nouveau message
                                Message message = new MimeMessage(session);

                                //On assigne le récipiendaire du message à l'aide du courriel de destination
                                message.setRecipient(Message.RecipientType.TO, new InternetAddress(usernameDest));

                                //On assigne le sujet du message
                                message.setSubject(getApplicationContext().getString(R.string.ObjetMessage) + "[" + txt_fichier.getText() + "]");

                                //On déclare une nouvelle partie de message pour y mettre le fichier à joindre
                                MimeBodyPart messageBodyPart = new MimeBodyPart();
                                Multipart multipart = new MimeMultipart();

                                //Variable utilisée pour contenir le chemin d'accès au fichier
                                String file;

                                //Variable utilisée pour contenir le nom du fichier à joindre dans le message
                                String attachement;

                                //Si le fichier n'a jamais été envoyé
                                if (nom_partager.get(position).charAt(nom_partager.get(position).length() - 1) == 'u')
                                {
                                    //On va chercher le nom du fichier avec la lettre u à la fin
                                    file = getApplicationContext().getFilesDir() + "/" + txt_fichier.getText() + 'u';

                                    //On assigne le nom du fichier qui sera joint au message
                                    attachement = txt_fichier.getText() +".csv";

                                    //On incrémente le compteur
                                    nom_fichier.compteur++;
                                }
                                //Si le fichier a déja été envoyé
                                else
                                {
                                    //On va chercher le nom du fichier avec la lettre e à la fin
                                    file = getApplicationContext().getFilesDir() + "/" + txt_fichier.getText() + "e";

                                    //On assigne le nom du fichier qui sera joint au message
                                    attachement = txt_fichier.getText() +".csv";
                                }


                                //On va chercher le fichier à joindre au message
                                FileDataSource source = new FileDataSource(file);
                                messageBodyPart.setDataHandler(new DataHandler(source));

                                //On assigne le nom au fichier
                                messageBodyPart.setFileName(attachement);

                                //On ajoute la partie du message au message
                                multipart.addBodyPart(messageBodyPart);

                                //On met toute les parties dans le message
                                message.setContent(multipart);

                                //On envoie le message
                                Transport.send(message);

                                //Si le fichier est envoyé pour la première fois
                                if (nom_fichier.compteur == 1)
                                {
                                    //On renomme le fichier pour qu'il y ait un 'e' au lieu d'un 'u' à la fin du nom du fichier
                                    File fichier = new File(getFilesDir(), nom_fichier.fichier + 'u');
                                    File fichierNouveau = new File(getFilesDir(), nom_fichier.fichier + 'e');

                                    fichier.renameTo(fichierNouveau);
                                }

                                //On envoi un toast à l'utilisateur pour lui dire que l'envoi a été un succès
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast mailOK = Toast.makeText(getApplicationContext(),
                                                (getApplicationContext().getString(R.string.MessageConfirmationEnvoie1) + " [" + usernameDest + "] "
                                                 + (getApplicationContext().getString(R.string.MessageConfirmationEnvoie2))), Toast.LENGTH_SHORT);
                                        mailOK.show();

                                        txt_envoie.setText(getApplicationContext().getString(R.string.messageEnvoye));
                                    }
                                });
                            }

                            //S'il y a erreur lors de l'envoi
                            catch(MessagingException e)
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //On envoi un message à l'utilisateur pour lui dire que l'envoie a échoué
                                        Toast mailFail = Toast.makeText(getApplicationContext(),
                                        getApplicationContext().getString(R.string.MessageErreur), Toast.LENGTH_SHORT);
                                        mailFail.show();
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            });

            //Listener utilisé pour supprimer le fichier de la rangée du bouton
            bouton_supprimer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //On créé une alerte pour avertir l'utilisateur qu'il va supprimer un fichier
                    AlertDialog.Builder alert = new AlertDialog.Builder(Telechargement.this);

                    //On donne un titre à l'alerte
                    alert.setTitle(getApplicationContext().getString(R.string.PopUpTitre));

                    //On assigne un message pour l'utilisateur
                    alert.setMessage(getApplicationContext().getString(R.string.PopUpConfirm));

                    //L'alerte n'est pas cancellable. Il faut répondre par oui ou non
                    alert.setCancelable(false);

                    //Ce qui se produit lorsqu'on appuie sur Oui
                    alert.setPositiveButton(getApplicationContext().getString(R.string.PopUpTrue), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int l)
                        {
                            //On va chercher le fichier à supprimer
                            File fichier_supprimer = new File(getFilesDir(), nom_partager.get(position));

                            //On supprime le fichier
                            fichier_supprimer.delete();

                            //On envoi un toast à l'utilisateur pour l'informer du succès de la suppression
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getApplicationContext().getString(R.string.PopUpValide), Toast.LENGTH_SHORT);
                            toast.show();

                            //On va chercher la liste de fichiers dans le dossier de mémoire interne de l'application
                            File[] fichiers = getFilesDir().listFiles();

                            //On déclare une liste de Nom_Fichier pour l'adaptateur
                            ArrayList<Nom_Fichier> liste_fichier = new ArrayList<>();

                            //On déclare l'adaptateur
                            Fichier_Adapteur adaptateur = new Fichier_Adapteur(getApplicationContext(), liste_fichier);

                            //On déclare la ListView
                            ListView list_view = (ListView) findViewById(R.id.outputTelechargement);

                            //On assigne l'adaptateur à la ListView
                            list_view.setAdapter(adaptateur);

                            //On vide la liste de nom de fichier
                            nom_partager.clear();

                            for(int i = 0; i < fichiers.length; i++)
                            {
                                //On va chercher le nom du fichier
                                String tempo = fichiers[i].getName();

                                //On ajoute le nom de fichier dans la liste
                                nom_partager.add(tempo);

                                //On enlève le dernier caractère du nom du fichier
                                tempo = tempo.substring(0, tempo.length() - 1);

                                //On créé un objet Nom_Fichier avec la chaîne tempo
                                Nom_Fichier fichier = new Nom_Fichier(tempo);

                                //On ajoute le Nom_Fichier à l'adaptateur
                                adaptateur.add(fichier);
                            }
                        }
                    });

                    //Si l'utilisateur clique sur non
                    alert.setNegativeButton(getApplicationContext().getString(R.string.PopUpFalse), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            //On cancel le dialogue
                            dialogInterface.cancel();
                        }
                    });

                    //On créé l'alerte
                    alert.create();

                    //On affiche l'alerte à l'utilisateur
                    alert.show();
                }
            });

            //On retourne la View
            return convertView;
        }

        //Constructeur pour le Custom Adapter
        public Fichier_Adapteur(Context context, ArrayList<Nom_Fichier> nom_fichier)
        {
            super(context, 0, nom_fichier);
        }
    }
}

