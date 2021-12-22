package com.example.dev4puzzle_v3;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.abs;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

public class MainActivity extends AppCompatActivity {

    int facil = 0;
    int intermedio = 0;
    int dificil = 0;

    private static final int READ_REQUEST_CODE = 42;
    private boolean isChecked = false;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.dev4puzzle_v3";

    private ArrayList<String> imagesList = new ArrayList<>();
    private ArrayList<ImageFirebase> imageFirebaseArrayList = new ArrayList<>();

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bloqueo de rotación de pantalla. Añadido en todas las Activity.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        progressBar = findViewById(R.id.progress);

        getImages();
    }

    private void getImages() {
        BuildersKt.launch(
                GlobalScope.INSTANCE,
                (CoroutineContext) Dispatchers.getMain(),
                CoroutineStart.DEFAULT,
                (Function2<CoroutineScope, Continuation<? super Unit>, Unit>) (coroutineScope, continuation) -> {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("img");

                    storageReference.listAll().addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            //List
                            List<StorageReference> list = task.getResult().getItems();


                            //runningTask = new GetImagesTask().execute(list);
                            for (int i = 0; i < list.size(); i++) {
                                final int finalI = i;
                                boolean isLast = (finalI == list.size() - 1);

                                getImage(list.get(i), finalI, isLast);
                            }
                        } else {
                            Toast.makeText(this, "Ocurrió un error al cargar las imágenes", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return Unit.INSTANCE;
                }
        );
    }

    private void getImage(StorageReference ref, int finalI, boolean isLast) {
        BuildersKt.launch(
                GlobalScope.INSTANCE,
                (CoroutineContext) Dispatchers.getMain(),
                CoroutineStart.DEFAULT,
                (Function2<CoroutineScope, Continuation<? super Unit>, Unit>) (coroutineScope, continuation) -> {

                    ref.getDownloadUrl().addOnCompleteListener(uriTask -> {
                        if (uriTask.isSuccessful()) {
                            String url = "https://firebasestorage.googleapis.com" + uriTask.getResult().getEncodedPath() + "?alt=media";

                            Log.v("MainActivity", "Image " + finalI + " url1: " + url);

                            imageFirebaseArrayList.add(new ImageFirebase(finalI, url));
                        }

                        if (isLast) {
                            //Remove progress
                            progressBar.setVisibility(View.GONE);
                            Log.v("MainActivity", "END LOOP imagesList: " + imagesList.size());
                            //Call New Adapter
                            loadAdapter();
                        }
                    });

                    return Unit.INSTANCE;
                });
    }


    private void loadAdapter() {

        Log.v("MainActivity", "loadAdapter: " + imagesList.size());

        Collections.sort(imageFirebaseArrayList, (a1, a2) -> a1.position - a2.position);

        imagesList.clear();
        for(int i=0; i<imageFirebaseArrayList.size(); i++){
            Log.v("MainActivity", "sort: i: "+i + " - "+ imageFirebaseArrayList.get(i).url + " - position: "+ imageFirebaseArrayList.get(i).position);
            imagesList.add(imageFirebaseArrayList.get(i).url);
        }

        ImageFirebaseAdapter adapter = new ImageFirebaseAdapter(this, imagesList);

        GridView grid = findViewById(R.id.grid);
        grid.setVisibility(View.VISIBLE);
        grid.setAdapter(adapter);

        grid.setOnItemClickListener((adapterView, view, i, l) -> {

            Intent intent = new Intent(getApplicationContext(), PuzzleActivity.class);
            //intent.putExtra("assetName", files[i % files.length]);
            intent.putExtra("url", imagesList.get(i % imagesList.size()));

            //Declaramos AlerDialog para que aparezca ventana emergente
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.eligeNivel);

            String nivelFacil = getString(R.string.nivelFacil);
            String nivelIntermedio = getString(R.string.nivelIntermedio);
            String nivelDificil = getString(R.string.nivelDificil);

            //Declaramos valor de los botones.
            String[] niveles = {nivelFacil, nivelIntermedio, nivelDificil};
            int checkedItem = 3; // Marca que opción aparece señalada por defecto. Ninguna
            builder.setSingleChoiceItems(niveles, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Menú que se ejecuta al pulsar la opción del nivel
                    switch (which) {
                        case 0:
                            facil = 1;
                            intent.putExtra("facil", facil);
                            break;
                        case 1:
                            intermedio = 1;
                            intent.putExtra("intermedio", intermedio);
                            break;
                        case 2:
                            dificil = 1;
                            intent.putExtra("dificil", dificil);
                            break;
                    }
                }
            });

            // añadir Aceptar y cancelar.
            builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Al pulsar en "Aceptar" se inicia la partida.
                    startActivity(intent);
                    //Imprime en la consola de Android studio el valor de las variables.
                    Log.d("NIVEL FACIL", String.valueOf(facil));
                    Log.d("NIVEL INTERMEDIO", String.valueOf(intermedio));
                    Log.d("NIVEL DIFICL", String.valueOf(dificil));
                }
            });
            builder.setNegativeButton(R.string.cancelarMinusculas, null);

            // Crea y visualiza la ventana con el menú
            AlertDialog dialog = builder.create();
            dialog.show();
        });
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


    // Este método comprueba si la aplicación tiene permisos
    // para acceder al almacenamiento externo del dispositivo.
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    // Este método muestra un diálogo indicando
    // que se necesita dar permiso a la aplicación.
    public void showDialog(final String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    // Este método crea el menú selección de la barra de acción
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow, menu);
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

    private final class GetImagesTask extends AsyncTask<List<StorageReference>, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(List<StorageReference>... params) {
            List<StorageReference> list = params[0];

            ArrayList<String> images = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                int finalI = i;


                list.get(i).getDownloadUrl().addOnCompleteListener(uriTask -> {
                    if (uriTask.isSuccessful()) {
                        //String url1 = "https://firebasestorage.googleapis.com" + uriTask.getResult().getPath() + "?alt=media";
                        String url1 = "https://firebasestorage.googleapis.com" + uriTask.getResult().getEncodedPath() + "?alt=media";
                        //String url = url1.replace("/img/", "/img%2F");
                        String url = url1;//.replace("/img/", "/img%2F");

                        Log.v("MainActivity", "Image " + finalI + " url1: " + url);

                        Log.v("MainActivity", "Image " + finalI + ": " + uriTask.getResult().getPath() + " - uriTask.getResult().getEncodedPath(): " + uriTask.getResult().getEncodedPath());

                        images.add(url);
                    }

                    if (finalI == list.size() - 1) {
                        //Remove progress
                        Log.v("MainActivity", "END LOOP imagesList: " + images.size());
                        //Call New Adapter
                        imagesList = images;
                        progressBar.setVisibility(View.GONE);
                        loadAdapter();
                    }
                });
            }

            return images;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            Log.v("MainActivity", "onPostExecute: " + result.size());
            imagesList = result;
            progressBar.setVisibility(View.GONE);
            loadAdapter();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ImageFirebase{
        private int position;
        private String url;

        public ImageFirebase(int position, String url) {
            this.position = position;
            this.url = url;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}