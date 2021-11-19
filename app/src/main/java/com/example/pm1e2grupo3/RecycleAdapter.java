package com.example.pm1e2grupo3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyViewHolder> {

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Usuarios usuario = usuarios.get(position);

        holder.mName.setText(usuario.getNombre());
        holder.mPhone.setText(usuario.getTelefono());
        holder.mGPS.setText("Latitud: " + usuario.getLatitud() + " \nLongitud: " + usuario.getLongitud());
        Glide.with(mContext).load(usuario.getFoto()).into(holder.mImageView);

        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(mContext,EditarCliente.class);

                intent.putExtra("id",cliente.getId());
                intent.putExtra("primer_nombre",cliente.getPrimerNombre());
                intent.putExtra("segundo_nombre",cliente.getSegundoNombre());
                intent.putExtra("primer_apellido",cliente.getPrimerApellido());
                intent.putExtra("segundo_apellido",cliente.getSegundoApellido());
                intent.putExtra("correo",cliente.getCorreo());
                intent.putExtra("telefono",cliente.getTelefono());
                intent.putExtra("imagen",cliente.getImagen());

                mContext.startActivity(intent);*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }


}
