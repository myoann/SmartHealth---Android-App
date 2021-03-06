package website.watchmyhealth.watchmyhealth.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import website.watchmyhealth.watchmyhealth.ConnectionChangeReceiver;
import website.watchmyhealth.watchmyhealth.NavigationDrawerFragment;
import website.watchmyhealth.watchmyhealth.R;
import website.watchmyhealth.watchmyhealth.fragment.FragmentAPropos;
import website.watchmyhealth.watchmyhealth.fragment.FragmentHome;
import website.watchmyhealth.watchmyhealth.fragment.FragmentMap;
import website.watchmyhealth.watchmyhealth.fragment.FragmentProfil;
import website.watchmyhealth.watchmyhealth.service.ServiceSync;


/**
 * Created by Yoann on 26/04/2015.
 */

public class Home extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private TextView mTextViewHeart;
    private TextView mTextViewStep;
    private String activiteEnCours = "marche";
    Fragment fragment;
    private ConnectionChangeReceiver packageReceiver;
    FragmentManager fragmentManager;
    private int start_nbPas = 0;
    private int end_nbPas = 0;
    private boolean firstPas = false;

    private BroadcastReceiver uiUpdated= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("fragment.toString() ====="+fragment.toString());
            if(fragment.getClass().toString().endsWith("FragmentHome")){
                TextView distance = (TextView) findViewById(R.id.tvDistance);
                TextView vitesse = (TextView) findViewById(R.id.tvVitesse);
                vitesse.setText(intent.getExtras().getString("vitesse")+" km/h");
                distance.setText(intent.getExtras().getString("distance")+" m");
            }
        }
    };


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mTextViewStep!=null){
                if(firstPas == false) {
                    start_nbPas = DataLayerListenerService.getCurrentValueStep();
                    if(start_nbPas != 0) {
                        firstPas = true;
                    }
                }
                System.out.println(start_nbPas);
                mTextViewStep.setText(Integer.toString(DataLayerListenerService.getCurrentValueStep()-start_nbPas));
            }

            if(mTextViewHeart!=null) {
                mTextViewHeart.setText(Integer.toString(DataLayerListenerService.getCurrentValueHeart()));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent intent = getIntent();
        if( intent.getExtras()!= null){
            System.out.println("intent.getExtras() != null");

            if(intent.getExtras().containsKey("GO_TO_FRAGMENT_PROFIL") ) {
                System.out.println("GO_TO_FRAGMENT_PROFIL existe");
                //Si on est sur l'activity ProfilModif et qu'on sauvegarde les modifications on doit retourner sur le FragmentProfil et non dans la page Home , on transmet donc cette info via cet Intent
                onNavigationDrawerItemSelected(intent.getIntExtra("GO_TO_FRAGMENT_PROFIL", 2));
                intent.removeExtra("GO_TO_FRAGMENT_PROFIL");
            }
        }
        else{
            packageReceiver= new ConnectionChangeReceiver();
            registerReceiver(packageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
        registerReceiver(uiUpdated, new IntentFilter("DISTANCE_UPDATED"));

        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        //si il y a une connexion internet alors on envoi les donnees sauvegarde dans les fichiers

    }
// l'utilisateur devra faire un double clique pour sortir de l'application
// probleme : il devient impossible de cliquer sur le bouton back pour retrourner dans des fragments visites precedemment
//    private Boolean exit = false;
//    @Override
//    public void onBackPressed() {
//        if(fragment.getClass().toString().endsWith("FragmentHome")){
//            if (exit) {
//                finish(); // finish activity
//            } else {
//                Toast.makeText(this, "Appuyez encore pour quitter",
//                        Toast.LENGTH_SHORT).show();
//                exit = true;
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        exit = false;
//                    }
//                }, 3 * 1000);
//            }
//        }
//        else{
//            fragmentManager.popBackStack();
//        }
//    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        fragment = (Fragment) new FragmentHome();
        fragmentManager = getSupportFragmentManager();

        //fragmentManager.beginTransaction()
        //.replace(R.id.container, PlaceholderFragment.newInstance(position + 1)).commit();
        switch (position) {
            case 0:
                fragment = new FragmentHome();
                mTitle = getString(R.string.navigation_accueil);
                break;
            case 1:
                fragment = new FragmentMap();
                mTitle = getString(R.string.navigation_map);
                break;
            case 2: //attention, si on change de "case" pour FragmentProfil, le mappage ne se fera plus entre ProfilModif et FragmentProfil ctrl+f = GO_TO_FRAGMENT_PROFIL
                fragment = new FragmentProfil();
                mTitle = getString(R.string.navigation_profil);
                break;
            case 3: //attention, si on change de "case" pour FragmentProfil, le mappage ne se fera plus entre ProfilModif et FragmentProfil ctrl+f = GO_TO_FRAGMENT_PROFIL
                fragment = new FragmentAPropos();
                mTitle = getString(R.string.navigation_a_propos);
                break;

        }
        fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack("tag").commit();
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked)
                    // Marche
                    activiteEnCours="marche";
                    break;
            case R.id.radioButton2:
                if (checked)
                    // Course
                    activiteEnCours="course";
                    break;
            case R.id.radioButton3:
                if (checked)
                    // Velo
                    activiteEnCours="velo";
                    break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    long timeWhenStopped = 0;
    public void startChronometer(View view) {
        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        //Permet de demarrer le service qui va recuperer la geolocalisation
        if(packageReceiver == null){
            packageReceiver= new ConnectionChangeReceiver();
            registerReceiver(packageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }

        if(!manager.isProviderEnabled( LocationManager.GPS_PROVIDER )){
            createGpsDisabledAlert();
            packageReceiver.onReceive(this, new Intent().setAction("activityStarted"));

        }
        else{
            this.startService(new Intent(Home.this, ServiceSync.class));
            packageReceiver.onReceive(this, new Intent().setAction("activityStarted"));

        }
        resetChronometer(view);
        ((Chronometer) findViewById(R.id.chronometer1)).setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        ((Chronometer) findViewById(R.id.chronometer1)).start();

        Toast.makeText(this,"Debut de la seance de sport",Toast.LENGTH_LONG).show();
        mTextViewHeart = (TextView) findViewById(R.id.heartbeat);
        mTextViewStep = (TextView) findViewById(R.id.stepcount);
        DataLayerListenerService.setHandler(handler);
    }

    public void stopChronometer(View view) {

        timeWhenStopped = ((Chronometer) findViewById(R.id.chronometer1)).getBase() - SystemClock.elapsedRealtime();
        ((Chronometer) findViewById(R.id.chronometer1)).stop();
        //Permet d'arreter le service qui recupere la geolocalisation

        this.stopService(new Intent(this, ServiceSync.class));
        Intent intentStop= new Intent();
        intentStop.setAction("activityFinished");
        intentStop.putExtra("typeActivity", activiteEnCours);
        this.end_nbPas= Integer.parseInt(mTextViewStep.getText().toString());
        packageReceiver.setStartPasEndPas(this.start_nbPas,this.end_nbPas);
        //Si la connexion internet est activee quand on arrete le service, on envoi les donnees et on supprime le fichier de geolocalisation sur le mobile
        if(this.isNetworkAvailable()){
            packageReceiver.onReceive(this, intentStop);
        }
        else{
            //permet d'indiquer au receiver que l'activite est finie, a la prochaine connexion, le serveur recevra les donnees
            packageReceiver.onReceive(this,intentStop);
        }
        Toast.makeText(this, "Fin de la seance de sport", Toast.LENGTH_LONG).show();
        firstPas = false;
        DataLayerListenerService.setHandler(null);
    }

    public void resetChronometer(View view) {
        ((Chronometer) findViewById(R.id.chronometer1)).setBase(SystemClock.elapsedRealtime());
        timeWhenStopped = 0;
    }

    private void createGpsDisabledAlert() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setMessage("Activer le GPS avant de commencer votre seance").setCancelable(false).setPositiveButton("Activer GPS ",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                        /** Test si le gps est active on lance le service sinon on lance un Toast pour lui indiquer d'activer le GPS*/
                        ((Chronometer) findViewById(R.id.chronometer1)).start();
                        Home.this.startService(new Intent(Home.this, ServiceSync.class));
                    }
                }
        );
        localBuilder.create().show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

