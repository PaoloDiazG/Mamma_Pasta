package com.mammapasta.historial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<String> pedidos;

    public HistorialAdapter(List<String> pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String pedido = pedidos.get(position);
        holder.textViewPedido.setText(pedido);
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPedido;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewPedido = itemView.findViewById(R.id.textViewPedido);
        }
    }
}
