package com.mammapasta.home;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.detail.DetallePizzaActivity;
import com.mammapasta.models.Pizza;
import com.mammapasta.R;

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
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetallePizzaActivity.class);
            intent.putExtra("nombre", pizza.getNombre());
            intent.putExtra("descripcion", pizza.getDescripcion());
            intent.putExtra("precio_base", pizza.getPrecioBase());
            intent.putExtra("imagen_resource", pizza.getImagenResource());
            context.startActivity(intent);
        });


        // Placeholder: cargar imágenes con Glide si tienes recursos
        // Glide.with(context).load(R.drawable.pizza_napo).into(holder.imgPizza);
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
            txtName = itemView.findViewById(R.id.txtName);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgPizza = itemView.findViewById(R.id.imgPizza);
        }
    }
}
