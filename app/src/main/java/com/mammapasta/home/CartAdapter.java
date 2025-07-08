package com.mammapasta.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;
import com.mammapasta.models.CartItem;
import com.mammapasta.utils.CartManager;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final int MAX_QUANTITY = 10;

    private List<CartItem> cartItems;
    private Context context;
    private CartManager cartManager;
    private OnCartUpdateListener listener;

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(List<CartItem> cartItems, Context context, OnCartUpdateListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
        this.cartManager = CartManager.getInstance(context);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.txtNombrePizza.setText(item.getNombrePizza());
        holder.txtIngredientes.setText(item.getIngredientes());
        holder.txtPrecio.setText("S/." + String.format("%.2f", item.getPrecio()));
        holder.txtCantidad.setText(String.valueOf(item.getCantidad()));
        holder.txtPrecioTotal.setText("S/." + String.format("%.2f", item.getPrecioTotal()));

        if (item.getImagenResource() != null && !item.getImagenResource().isEmpty()) {
            int resId = context.getResources().getIdentifier(item.getImagenResource(), "drawable", context.getPackageName());
            if (resId != 0) {
                holder.imgPizza.setImageResource(resId);
            }
        }

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getCantidad() + 1;
            if (newQuantity <= MAX_QUANTITY) {
                cartManager.updateQuantity(position, newQuantity);
                item.setCantidad(newQuantity);
                notifyItemChanged(position);
                if (listener != null) listener.onCartUpdated();
            } else {
                Toast.makeText(context, "Cantidad máxima permitida: " + MAX_QUANTITY, Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = item.getCantidad() - 1;
            if (newQuantity > 0) {
                cartManager.updateQuantity(position, newQuantity);
                item.setCantidad(newQuantity);
                notifyItemChanged(position);
            } else {
                cartManager.removeFromCart(position);
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
            }
            if (listener != null) listener.onCartUpdated();
        });

        holder.btnRemove.setOnClickListener(v -> {
            cartManager.removeFromCart(position);
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
            if (listener != null) listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPizza;
        TextView txtNombrePizza, txtIngredientes, txtPrecio, txtCantidad, txtPrecioTotal;
        Button btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPizza = itemView.findViewById(R.id.imgPizzaCart);
            txtNombrePizza = itemView.findViewById(R.id.txtNombrePizzaCart);
            txtIngredientes = itemView.findViewById(R.id.txtIngredientesCart);
            txtPrecio = itemView.findViewById(R.id.txtPrecioCart);
            txtCantidad = itemView.findViewById(R.id.txtCantidadCart);
            txtPrecioTotal = itemView.findViewById(R.id.txtPrecioTotalCart);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseCart);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseCart);
            btnRemove = itemView.findViewById(R.id.btnRemoveCart);
        }
    }
}
