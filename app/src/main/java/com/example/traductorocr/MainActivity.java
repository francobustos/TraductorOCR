package com.example.traductorocr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.mannan.translateapi.Language;
import com.mannan.translateapi.TranslateAPI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    EditText etResult, etTranslated;
    ImageView ivImage;
    Spinner spinner;
    Boolean replace;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private static final String TAG = "MainActivity";

    String[] cameraPermission;
    String[] storagePermission;

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etResult = findViewById(R.id.etResult);
        etTranslated = findViewById(R.id.etTranslated);
        ivImage = findViewById(R.id.ivImage);
        spinner = findViewById(R.id.spinner);
        replace = true;

        cameraPermission = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        String[] languages = {"Español", "English", "Português", "Français", "Italiano", "Deutsche",
                "简体中文", "русский", "日本語"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_custom, languages);
        spinner.setAdapter(adapter);
    }

    // Método para el botón Traducir
    public void traduce(View view){
        String text = etResult.getText().toString();
        TranslateAPI translateAPI;

        String language = spinner.getSelectedItem().toString();

        switch (language){
            case "Español":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,
                        Language.SPANISH,
                        text);
                break;

            case "English":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.ENGLISH,         //Target Language
                        text);
                break;

            case "Português":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.PORTUGUESE,         //Target Language
                        text);
                break;

            case "Français":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.FRENCH,         //Target Language
                        text);
                break;

            case "Italiano":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.ITALIAN,         //Target Language
                        text);
                break;

            case "Deutsche":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.GERMAN,         //Target Language
                        text);
                break;

            case "简体中文":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.CHINESE_SIMPLIFIED,         //Target Language
                        text);
                break;

            case "русский":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.RUSSIAN,         //Target Language
                        text);
                break;

            case "日本語":
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,   //Source Language
                        Language.JAPANESE,         //Target Language
                        text);
                break;
            default:
                Toast.makeText(this,
                        "Error al encontrar el idioma, se traducirá al español",
                        Toast.LENGTH_LONG).show();
                translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT,
                        Language.SPANISH,
                        text);
        }

        translateAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
                                              @Override
                                              public void onSuccess(String translatedText) {
                                                  Log.d(TAG, "onSuccess: "+translatedText);
                                                  etTranslated.setText(translatedText);
                                              }

                                              @Override
                                              public void onFailure(String ErrorText) {
                                                  Log.d(TAG, "onFailure: "+ErrorText);
                                              }
        });

    }

    // Método para el botón Limpiar
    public void clear(View view){
        etTranslated.setText("");
        etResult.setText("");
        ivImage.setImageDrawable(null);
    }

    // Menú ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Métodos para los botones del ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addImage){
            showImageImportDialog();
        }
        if (id == R.id.settings){
            item.setChecked(!item.isChecked());
            replace = item.isChecked();
        }
        return super.onOptionsItemSelected(item);
    }

    // Díalogo tras presionar el botón imagen
    private void showImageImportDialog() {
        String [] items = {" Cámara", " Galería"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Seleccionar imagen");
        dialog.setItems(items, (dialog1, which) -> {
            if (which == 0){
                //Cámara
                if (!checkCameraPermission()){
                    requestCameraPermission();
                } else {
                    pickCamera();
                }
            }
            if (which == 1){
                //Galería
                if (!checkStoragePermission()){
                    requestStoragePermission();
                } else {
                    pickGallery();
                }
            }

        });
        dialog.create().show();
    }

    // Activa la galería
    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    // Activa la cámara
    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nueva foto"); // Título de la foto
        values.put(MediaStore.Images.Media.DESCRIPTION, "Imagén a Texto"); // Descripción
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    // Pide permisos de almacenamiento
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    // Verifica permisos de almacenamiento
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    // Pide permisos de cámara
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    // Verifica permisos de cámara
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        /* Es necesario tener permisos de almacenamiento con la cámara para que la foto salga en
        mejor calidad */
        return result && result1;
    }

    // Manejador de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted){
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    // Manejador de imagenes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Recibe imagen de galería para recortar
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON) //Activa las pautas de la imagen
                        .start(this);

            }

            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Recibe imagen de cámera para recortar
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON) //Activa las pautas de la imagen
                        .start(this);

            }
        }

        // Conseguir imagen recortada y convertirla en texto
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data); //Imagén recortada

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri(); //Ruta de la imagén
                ivImage.setImageURI(resultUri);

                //Consigue la imagen en un bitmap
                BitmapDrawable bitmapDrawable = (BitmapDrawable) ivImage.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                //Crea el reconocedor de texto
                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                //Verifica si no hay error
                if (!recognizer.isOperational()) {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                } else {
                    // Crea el frame con la imagen a reconocer
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();

                    // Reconoce el texto
                    SparseArray<TextBlock> items = recognizer.detect(frame);

                    StringBuilder sb = new StringBuilder();

                    //Extrae el sb hasta que no haya mas texto
                    if (replace){
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myItem = items.valueAt(i);
                            sb.append(myItem.getValue());
                            sb.append(" ");
                        }
                    } else {
                            for (int i = 0; i < items.size(); i++) {
                                TextBlock myItem = items.valueAt(i);
                                sb.append(myItem.getValue());
                                sb.append("\n");
                                }
                            }

                    etResult.setText(sb.toString());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // Muestra esto si hay algún error
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}