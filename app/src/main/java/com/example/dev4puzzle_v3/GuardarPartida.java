package com.example.dev4puzzle_v3;

import static com.example.dev4puzzle_v3.AdminSQLiteOpenHelper.TABLE_JUGADORES;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.CalendarContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuardarPartida extends AppCompatActivity {

    AdminSQLiteOpenHelper db;
    private TextView nombreText;
    private TextView tiempoText;
    public String nombre;
    public String tiempo;
    long longTime;
    Button btnGuardar;
    HomeWatcher mHomeWatcher;
    private boolean isChecked = false;
    private static final int READ_REQUEST_CODE = 42;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.dev4puzzle_v3";

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseFirestore dbFire = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardar_partida);
        btnGuardar = findViewById(R.id.btnGuardar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String nombrePartida = getString(R.string.nombre);
        String Tiempo = getString(R.string.tiempoDePartida);

        nombreText = (TextView) findViewById(R.id.nombreJugador);
        tiempoText = (TextView) findViewById(R.id.tiempoJugador);

        nombre = getIntent().getStringExtra("nombre");
        nombreText.setText(nombrePartida + nombre);
        tiempo = getIntent().getStringExtra("tiempo");
        tiempoText.setText(Tiempo + tiempo);
        longTime = getIntent().getLongExtra("longTime", 0);
        Log.d("CRONOMETRO", String.valueOf(longTime));

        // Vinculamos el servicio de música
        doBindService();
        Intent music = new Intent();
        music.setClass(this, ServicioMusica.class);
        startService(music);

        inicializarFireBase();

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Guardamos datos en Firebase.
                Jugador jugador = new Jugador();
                jugador.setUid(UUID.randomUUID().toString());
                jugador.nombreJugador = nombre;
                jugador.tiempoPartida = tiempo;
                jugador.tiempoLongPartida = longTime;
                databaseReference.child("Jugador").child(jugador.getNombreJugador()).setValue(jugador);


                //Creación o actualización de la BBDD.
                AdminSQLiteOpenHelper adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(GuardarPartida.this);
                SQLiteDatabase db = adminSQLiteOpenHelper.getWritableDatabase();

                //Cargar datos de la partida en la BBDD.
                ContentValues values = new ContentValues();
                values.put("nombre", nombre);
                values.put("tiempo", tiempo);
                values.put("puntuacion", longTime);
                db.insert(TABLE_JUGADORES, null, values);

                //Añadimos el calendario y su funcionalidad a nuestro botón de "Guardar" partida.
                if(!nombreText.getText().toString().isEmpty() && !tiempoText.getText().toString().isEmpty()){
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setData(CalendarContract.Events.CONTENT_URI);
                    intent.putExtra(CalendarContract.Events.TITLE, nombreText.getText().toString());
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, tiempoText.getText().toString());
                    intent.putExtra(CalendarContract.Events.ALL_DAY, true);

                    if(intent.resolveActivity(getPackageManager()) !=null){
                        startActivity(intent);
                    } else {
                        Toast.makeText(GuardarPartida.this, "La app no soporta esta acción", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(GuardarPartida.this, "Por favor complete los datos de partida", Toast.LENGTH_SHORT).show();
                }

                AlertDialogGuardarPartida();
            }

        });

        // Iniciamos el HomeWatcher
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }

            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

    }

    // Vinculamos el servicio de música
    private boolean mIsBound = false;
    private ServicioMusica mServ;
    private ServiceConnection Scon = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder binder) {
            mServ = ((ServicioMusica.ServiceBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    // Vincular servicio
    void doBindService() {
        bindService(new Intent(this, ServicioMusica.class), Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    // Desvincular servicio
    void doUnbindService() {
        if (mIsBound) {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    // Este método reanuda la música
    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    // Este método pone la música en pausa
    @Override
    protected void onPause() {
        super.onPause();

        // Detectamos la pausa de la pantalla
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }

    // Este método desvincula el servicio de música cuando no lo necesitamos
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Desvinculamos el servicio de música
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this, ServicioMusica.class);
        stopService(music);
    }

    public void AlertDialogGuardarPartida() {
        String guardadoConexito = getString(R.string.jugadorGuardadoConExito);
        String tiempoPartida = getString(R.string.tiempoDePartida);
        String nombreJugador = getString(R.string.nombre);
        Intent intent = new Intent(this, HallOfFame.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.guardarPartida);
        builder.setMessage(guardadoConexito + "\n\n" + tiempoPartida + tiempo + "\n" + nombreJugador + nombre);

        builder.setPositiveButton(R.string.aceptarMinusculas, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(intent);
            }
        });;

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void inicializarFireBase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu miMenu){
        getMenuInflater().inflate(R.menu.overflow, miMenu);
        return true;
    }


    // Este método dispara la acción correspondiente al elegir cada opción del menú.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ayuda:
                // Se abre la WebView con la ayuda
                Intent ayuda = new Intent(this, Ayuda.class);
                startActivity(ayuda);
                return true;
            case R.id.selector_musica:
                // Se abre el selector de música
                buscarPistaAudio();
                return true;
            case R.id.checkable_menu:
                isChecked = !item.isChecked();
                item.setChecked(isChecked);
                if (isChecked) {
                    AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                } else {
                    AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Este método permite acceder al selector de archivos para que podamos elegir un tema de música.
    public void buscarPistaAudio() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filtramos para que solo muestre los archivos que se pueden abrir.
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri;

        if (data != null) {
            uri = data.getData();
            ServicioMusica.audioUri = uri;
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }


}