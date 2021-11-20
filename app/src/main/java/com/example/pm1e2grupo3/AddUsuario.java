package com.example.pm1e2grupo3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddUsuario extends Activity {

    RequestQueue requestQueue;
    EditText txtNombre, txtTelefono, txtLatitud, txtLongitud;
    ImageView img;
    Bitmap bitmap;
    String encodeImage;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_CAN = 101;
    String currentPhotoPath;
    Uri filepath;
    Boolean tomarFoto;

    LocationManager locationManager;

    //DIRECCION API
    String domain = "http://192.168.0.17/";

    //RUTA DEL ARCHIVO
    String path = "pm1e2grupo3/api/guardarDatos.php";

    String url = "https://pm1e2grupo3.alzir.hn/guardar_datos.php";

    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_usuario);

        txtNombre = (EditText) findViewById(R.id.txtNombre);
        txtTelefono = (EditText) findViewById(R.id.txtTelefono);
        txtLatitud = (EditText) findViewById(R.id.txtLatitud);
        txtLongitud = (EditText) findViewById(R.id.txtLongitud);
        img = (ImageView) findViewById(R.id.imgCliente);

        edicionCampos(false);
        txtLatitud.setEnabled(false);
        txtLongitud.setEnabled(false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AddUsuario.this);

        //PERMISO DE UBICACION
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(AddUsuario.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
        else {
            ActivityCompat.requestPermissions(AddUsuario.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }

        //CARGAR IMAGEN
        /*img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //OBTENER IMAGEN DE GALERIA
                Dexter.withContext(AddUsuario.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "Buscar Imagen"), 1);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
                //FIN OBTENER IMAGEN DE GALERIA
            }
        });*/

        //GUARDAR
        Button btnGuardar = (Button) findViewById(R.id.btnGuardar);

        //btnGuardar.setOnClickListener(view -> saveUsuario());

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUsuario();
            }
        });

        Button btnAdd = (Button) findViewById(R.id.btnAddImagen);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomarFoto();
                edicionCampos(true);
            }
        });
    }

    private void tomarFoto() {
        final CharSequence[] options = { "Tomar Foto", "Seleccionar de galeria","Cancelar" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar una Imagen!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Tomar Foto")) {
                    permisos();
                    tomarFoto = true;
                } else if (options[item].equals("Seleccionar de galeria")) {
                    //OBTENER IMAGEN DE GALERIA
                    Dexter.withContext(AddUsuario.this)
                            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    startActivityForResult(Intent.createChooser(intent, "Buscar Imagen"), 1);
                                    tomarFoto = false;
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                    permissionToken.continuePermissionRequest();
                                }
                            }).check();
                    //FIN OBTENER IMAGEN DE GALERIA
                } else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();
                    edicionCampos(false);
                }
            }
        });
        builder.show();
    }

    private void permisos() {
        //VALIDAR PERMISO QUE ESTA OTORGADO
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //otorga el permiso si no lo tengo
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PETICION_ACCESO_CAN);
        }else{
            TakePhotoDir();
        }
    }

    private void TakePhotoDir() {
        Intent Intenttakephoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Intenttakephoto.resolveActivity(getPackageManager()) != null){
            File foto = null;
            try {
                foto = createImageFile();
            }
            catch (Exception ex){
                ex.toString();
            }
            if (foto!= null){
                filepath = FileProvider.getUriForFile(getApplicationContext(), "com.example.pm1e2grupo3.fileprovider",foto);
                Intenttakephoto.putExtra(MediaStore.EXTRA_OUTPUT, filepath);
                startActivityForResult(Intenttakephoto, REQUEST_IMAGE_CAPTURE);
            }
        }
    }




    //INICIO DE OBTENER IMAGEN GALERIA Y OBTENER NOMBRE PARA ALMACENARLA
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(tomarFoto){
            if (requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) {
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                    encodeBitmapImage(bitmap);
                    img.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            if (requestCode == 1 && resultCode == RESULT_OK) {
                filepath = data.getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(filepath);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    img.setImageBitmap(bitmap);
                    encodeBitmapImage(bitmap);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

    }


    private void encodeBitmapImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytesofimage = byteArrayOutputStream.toByteArray();
        encodeImage = android.util.Base64.encodeToString(bytesofimage, Base64.DEFAULT);
    }
    //FINAL DE OBTENER IMAGEN GALERIA Y OBTENER NOMBRE PARA ALMACENARLA


    //GUARDAR USUARIO
    private void saveUsuario() {
         String nombre = txtNombre.getText().toString().trim();
         String telefono = txtTelefono.getText().toString().trim();
         String latitud = txtLatitud.getText().toString().trim();
         String longitud = txtLongitud.getText().toString().trim();

         boolean pError;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");

        if (nombre.isEmpty() || telefono.isEmpty() || latitud.isEmpty() || longitud.isEmpty() || (encodeImage.isEmpty()||encodeImage == null)) {
            Toast.makeText(AddUsuario.this, "LLene todos los campos!", Toast.LENGTH_SHORT).show();
            pError = true;
        } else {
            pError = false;
            progressDialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                if (response.length() > 0) {
                    Toast.makeText(AddUsuario.this, "Datos Insertados", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(AddUsuario.this, "Datos NO Insertados", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }, error -> {
                Toast.makeText(AddUsuario.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("nombre", nombre);
                    params.put("telefono", telefono);
                    params.put("latitud", latitud);
                    params.put("longitud", longitud);
                    params.put("upload", encodeImage); //encodeImage es el valor que se guarda en la bdd

                    return params;
                }
            };

            if(!pError){
                RequestQueue requestQueue = Volley.newRequestQueue(AddUsuario.this);
                requestQueue.add(request);
            }

            edicionCampos(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void edicionCampos(boolean param){
        txtNombre.setEnabled(param);
        txtTelefono.setEnabled(param);
    }

    //METODOS PARA OBTENER LA UBICACION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1]
        == PackageManager.PERMISSION_GRANTED)) {
            getLocation();
        }
        else {
            Toast.makeText(getApplicationContext(), "Permiso de Ubicacion no Autorizado", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE
        );

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();

                    if (location != null) {
                        txtLatitud.setText(String.valueOf(location.getLatitude()));
                        txtLongitud.setText(String.valueOf(location.getLongitude()));
                    }
                    else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        //Initialize location call back
                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                Location location1 = locationResult.getLastLocation();

                                txtLatitud.setText(String.valueOf(location1.getLatitude()));
                                txtLongitud.setText(String.valueOf(location1.getLongitude()));
                            }
                        };

                        //Request Location Updates
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.myLooper());

                    }

                }
            });
        }
        else {
            //Cuando el servicio de ubicacion no esta habilitado
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */);
        // Save a file: path for use with ACTION_VIEW
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}