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

    Description :               Fichier contenant les fonctions permettant de gèrer les préférences

    Note * :                    Notez que le code sur GitHub est affiché de façon publique, donc tout le monde
                                peut avoir accès au code. Cela est dû au compte gratuit de Git. Il faut débourser un montant
                                par mois afin de rendre le projet "privé".

*/

package com.Info420.ITMI.greencube;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class PrefsActivity extends AppCompatActivity
{

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


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Ouvre le layout prefs_ui dans une view
        setContentView(R.layout.prefs_ui);

        //Defini la toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        //Defini le nom de la toolbar
        myToolbar.setTitle("");

        setSupportActionBar(myToolbar);

        //Défini le titre de la page
        myToolbar.setTitle(R.string.PrefsTitle);
        //Défini la couleur du titre
        myToolbar.setTitleTextColor(Color.WHITE);

        getFragmentManager().beginTransaction().add(R.id.fragmentContainer, new MyPreferenceFragment()).commit();
    }
    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            // Ajoute les préférence à la page
            addPreferencesFromResource(R.xml.prefs_items);
        }
    }
}


