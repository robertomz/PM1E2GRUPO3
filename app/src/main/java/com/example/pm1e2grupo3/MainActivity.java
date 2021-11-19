package com.example.pm1e2grupo3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager manager;
    private RecyclerView.Adapter mAdapter;
    private List<Usuarios> usuarios;

    //DIRECCION API
    String domain = "http://192.168.0.17/";

    //RUTA DEL ARCHIVO
    String path = "pm1e2grupo3/api/obtener_datos.php";

    String url = "https://pm1e2grupo3.alzir.hn/usuarios/obtener_datos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //VENTANA NUEVO USUARIO
        FloatingActionButton btnNuevo = findViewById(R.id.btnNuevo);

        btnNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddUsuario.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.clientes_recyclerView);
        manager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(manager);
        usuarios = new ArrayList<>();

        getUsuarios();
    }

    private void getUsuarios() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);

                        String id = object.getString("id");
                        String nombre = object.getString("nombre");
                        String telefono = object.getString("telefono");
                        String latitud = object.getString("latitud");
                        String longitud = object.getString("longitud");
                        String foto = object.getString("foto");


                        Usuarios usuario = new Usuarios(id, nombre, telefono, latitud, longitud, foto);
                        usuarios.add(usuario);
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                mAdapter = new RecycleAdapter(MainActivity.this, usuarios);
                recyclerView.setAdapter(mAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        Volley.newRequestQueue(MainActivity.this).add(stringRequest);
    }
}