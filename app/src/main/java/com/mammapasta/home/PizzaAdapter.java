package com.mammapasta.home;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;
import com.mammapasta.detail.DetallePizzaActivity;
import com.mammapasta.models.Pizza;

import java.util.List;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder> {

    private List<Pizza> pizzaList;
    private Context context;

    public PizzaAdapter(List<Pizza> pizzaList, Context context) {
        this.pizzaList = pizzaList;
        this.context = context;
    }

    @NonNull
    @Override
    public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pizza, parent, false);
        return new PizzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
        Pizza pizza = pizzaList.get(position);

        holder.txtName.setText(pizza.getNombre());
        holder.txtDesc.setText(pizza.getDescripcion());
        holder.txtPrice.setText("S/." + String.format("%.2f", pizza.getPrecioBase()));

        // Obtener el resourceId de la imagen a partir del nombre almacenado
        int imageResId = context.getResources().getIdentifier(
                pizza.getImagenResource(), "drawable", context.getPackageName()
        );

        if (imageResId != 0) {
            holder.imgPizza.setImageResource(imageResId);
        } else {
            holder.imgPizza.setImageResource(R.drawable.ic_mammapasta_logo);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetallePizzaActivity.class);
            intent.putExtra("nombre", pizza.getNombre());
            intent.putExtra("descripcion", pizza.getDescripcion());
            intent.putExtra("precio_base", pizza.getPrecioBase());
            intent.putExtra("imagen_resource", pizza.getImagenResource());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pizzaList.size();
    }

    public static class PizzaViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDesc, txtPrice;
        ImageView imgPizza;

        public PizzaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPizzaName);
            txtDesc = itemView.findViewById(R.id.txtPizzaDesc);
            txtPrice = itemView.findViewById(R.id.txtPizzaPrice);
            imgPizza = itemView.findViewById(R.id.imgPizza);
        }
    }
}
