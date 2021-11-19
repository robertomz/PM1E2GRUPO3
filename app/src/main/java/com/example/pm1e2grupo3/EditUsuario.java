package com.example.pm1e2grupo3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditUsuario extends AppCompatActivity {


    TextView txtid;
    EditText txtNombreA, txtTelefonoA, txtLatitudA, txtLongitudA;
    ImageView imgA;
    Bitmap bitmap;
    String encodeImage;

    LocationManager locationManager;

    String urlEdit = "https://pm1e2grupo3.alzir.hn/editar_cliente.php";
    String urlDelete = "https://pm1e2grupo3.alzir.hn/eliminar_cliente.php";

    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_usuario);

        txtid = findViewById(R.id.txtID);
        txtNombreA = findViewById(R.id.txtNombreA);
        txtTelefonoA = findViewById(R.id.txtTelefonoA);
        txtLatitudA = findViewById(R.id.txtLatitudA);
        txtLongitudA = findViewById(R.id.txtLongitudA);
        imgA = findViewById(R.id.imgClienteA);

        Intent intent = getIntent();

        String id = intent.getStringExtra("id");
        String nombre = intent.getStringExtra("nombre");
        String telefono = intent.getStringExtra("telefono");
        String latitud = intent.getStringExtra("latitud");
        String longitud = intent.getStringExtra("longitud");
        String foto = intent.getStringExtra("foto");

        if (intent != null){
            txtid.setText(id);
            txtNombreA.setText(nombre);
            txtTelefonoA.setText(telefono);
            txtLatitudA.setText(latitud);
            txtLongitudA.setText(longitud);
            Glide.with(EditUsuario.this).load("https://pm1e2grupo3.alzir.hn/" + foto).into(imgA);
        }

        //ELIMINAR CLIENTE
        Button btnEliminar = (Button) findViewById(R.id.btnEliminar);

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EliminarCliente(Integer.parseInt(id));
            }
        });

        //EDITAR IMAGEN / CARGAR IMAGEN
        imgA = (ImageView) findViewById(R.id.imgClienteA);

        imgA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(EditUsuario.this)
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
            }
        });

        //PERMISO DE UBICACION
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(EditUsuario.this);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(EditUsuario.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
        else {
            ActivityCompat.requestPermissions(EditUsuario.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }

        //EDITAR CLIENTE
        Button btnEditar = (Button) findViewById(R.id.btnEditar);

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditCliente();
            }
        });

        //IR AL MAPA
        Button btnMapa = (Button) findViewById(R.id.btnMapa);

        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


    public void EditCliente() {
        final String idC = txtid.getText().toString().trim();
        final String nombre = txtNombreA.getText().toString().trim();
        final String telefono = txtTelefonoA.getText().toString().trim();
        final String latitud = txtLatitudA.getText().toString().trim();
        final String longitud = txtLongitudA.getText().toString().trim();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");

        if (nombre.isEmpty() || telefono.isEmpty() || latitud.isEmpty() || longitud.isEmpty() || encodeImage.isEmpty()) {
            Toast.makeText(EditUsuario.this, "LLene todos los campos!", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, urlEdit,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(EditUsuario.this, "Usuario Editado", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(EditUsuario.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("id", idC);
                    params.put("nombre", nombre);
                    params.put("telefono", telefono);
                    params.put("latitud", latitud);
                    params.put("longitud", longitud);
                    params.put("upload", encodeImage); //encodeImage es el valor que se guarda en la bdd

                    return params;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(EditUsuario.this);
            requestQueue.add(request);;
        }
    }

    private void EliminarCliente(int id) {
        StringRequest request = new StringRequest(Request.Method.POST, urlDelete,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(EditUsuario.this, "Usuario Eliminado", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditUsuario.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("id", String.valueOf(id));

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    //CODIGO DE IMAGEN
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri filepath = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imgA.setImageBitmap(bitmap);
                encodeBitmapImage(bitmap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void encodeBitmapImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytesofimage = byteArrayOutputStream.toByteArray();
        encodeImage = android.util.Base64.encodeToString(bytesofimage, Base64.DEFAULT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
                        txtLatitudA.setText(String.valueOf(location.getLatitude()));
                        txtLongitudA.setText(String.valueOf(location.getLongitude()));
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

                                txtLatitudA.setText(String.valueOf(location1.getLatitude()));
                                txtLongitudA.setText(String.valueOf(location1.getLongitude()));
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
}