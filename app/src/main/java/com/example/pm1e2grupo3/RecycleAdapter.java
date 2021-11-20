package com.example.pm1e2grupo3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyViewHolder> {

    private static final String TAG = "Adapter";

    private Context mContext;
    private List<Usuarios> usuarios = new ArrayList<>();

    public RecycleAdapter(Context context, List<Usuarios> usuarios) {
        this.mContext = context;
        this.usuarios = usuarios;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mName, mGPS, mPhone;
        private ImageView mImageView;
        private ConstraintLayout mContainer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.userName);
            mGPS = itemView.findViewById(R.id.userGPS);
            mPhone = itemView.findViewById(R.id.userPhone);
            mImageView = itemView.findViewById(R.id.profileImage);
            mContainer = itemView.findViewById(R.id.usuario_container);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_clientes,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Usuarios usuario = usuarios.get(position);

        holder.mName.setText(usuario.getNombre());
        holder.mPhone.setText(usuario.getTelefono());
        holder.mGPS.setText("Latitud: " + usuario.getLatitud() + " \nLongitud: " + usuario.getLongitud());
        Glide.with(this.mContext).load("https://pm1e2grupo3.alzir.hn/" + usuario.getFoto()).into(holder.mImageView);

        holder.mContainer.setOnClickListener(view -> {
            final CharSequence[] options = { "Editar Usuario", "Ver Ubicación","Cancelar" };
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Seleccione una opción");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("Editar Usuario")){
                        Log.d(TAG, "Clicked: " + usuario.getId());
                        Intent intent = new Intent(mContext, EditUsuario.class);

                        intent.putExtra("id", usuario.getId());
                        intent.putExtra("nombre", usuario.getNombre());
                        intent.putExtra("telefono", usuario.getTelefono());
                        intent.putExtra("latitud", usuario.getLatitud());
                        intent.putExtra("longitud", usuario.getLongitud());
                        intent.putExtra("foto", usuario.getFoto());

                        mContext.startActivity(intent);
                    }
                    else if (options[item].equals("Ver Ubicación")){
                        final CharSequence[] optionsUbicaciones = { "Ver Ubicacion", "Ver inDrive","Cancelar" };
                        AlertDialog.Builder builderUbicaciones = new AlertDialog.Builder(mContext);
                        builderUbicaciones.setTitle("Seleccione una opción");
                        builderUbicaciones.setItems(optionsUbicaciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                Intent intent = new Intent(mContext, MapsActivity.class);
                                intent.putExtra("nombre", usuario.getNombre());
                                intent.putExtra("latitud", usuario.getLatitud());
                                intent.putExtra("longitud", usuario.getLongitud());
                                if (optionsUbicaciones[item].equals("Ver Ubicacion")){
                                    Log.d(TAG, "Clicked: " + usuario.getId());
                                    intent.putExtra("inDriver", "false");
                                    mContext.startActivity(intent);
                                }
                                else if (optionsUbicaciones[item].equals("Ver inDrive")){
                                    Log.d(TAG, "Clicked: " + usuario.getId());
                                    intent.putExtra("inDriver", "true");

                                    mContext.startActivity(intent);
                                }
                                else if (optionsUbicaciones[item].equals("Cancelar")){
                                    dialog.dismiss();
                                }
                            }
                        });
                        builderUbicaciones.show();

                    }
                    else if (options[item].equals("Cancelar")){
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }
}
