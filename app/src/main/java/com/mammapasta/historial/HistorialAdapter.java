package com.mammapasta.historial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<String> pedidos;

    public HistorialAdapter(List<String> pedidos) {
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String pedido = pedidos.get(position);

        holder.txtPedidoTitulo.setText("Pedido #" + (position + 1));
        holder.txtPedidoDetalles.setText(pedido.replace("$", "S/"));
    }

    @Override
    public int getItemCount() {
        return pedidos != null ? pedidos.size() : 0;
    }

    public void updatePedidos(List<String> nuevosPedidos) {
        this.pedidos = nuevosPedidos;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPedidoTitulo;
        TextView txtPedidoDetalles;

        public ViewHolder(View itemView) {
            super(itemView);
            txtPedidoTitulo = itemView.findViewById(R.id.txtPedidoTitulo);
            txtPedidoDetalles = itemView.findViewById(R.id.txtPedidoDetalles);
        }
    }
}
