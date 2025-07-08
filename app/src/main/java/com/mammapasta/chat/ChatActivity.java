package com.mammapasta.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mammapasta.R;
import com.mammapasta.adapters.ChatAdapter;
import com.mammapasta.builder.ConstruirPizzaActivity;
import com.mammapasta.historial.HistorialActivity;
import com.mammapasta.models.Message;
import com.mammapasta.home.HomeActivity;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerView);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Mensaje de bienvenida del "bot"
        addBotMessage("🍝 ¡Bienvenido al soporte de Mamá Pasta! ¿En qué te puedo ayudar hoy?");

        btnSend.setOnClickListener(v -> {
            String userText = edtMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                addUserMessage(userText);
                edtMessage.setText("");
                simulateBotResponse(userText);
            }
        });
    }

    private void addUserMessage(String text) {
        messageList.add(new Message(text, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String text) {
        messageList.add(new Message(text, false));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void simulateBotResponse(String userInput) {
        String lower = userInput.toLowerCase();

        if (lower.contains("historial") || lower.contains("pedidos anteriores")) {
            addBotMessage("📜 Abriendo tu historial de pedidos...");
            startActivity(new Intent(this, HistorialActivity.class));

        } else if (lower.contains("pizza") || lower.contains("construir") || lower.contains("pedido nuevo")) {
            addBotMessage("🛠️ ¡Vamos a construir tu pizza perfecta!");
            startActivity(new Intent(this, ConstruirPizzaActivity.class));

        } else if (lower.contains("inicio") || lower.contains("menú") || lower.contains("volver")) {
            addBotMessage("🏠 Regresando a la pantalla principal...");
            startActivity(new Intent(this, HomeActivity.class));

        } else if (lower.contains("hola") || lower.contains("buenas") || lower.contains("saludos")) {
            addBotMessage("👋 ¡Hola! Puedes pedirme hacer un pedido, construir una pizza o revisar tu historial.");

        } else if (lower.contains("gracias") || lower.contains("thank you")) {
            addBotMessage("🍕 ¡Gracias a ti! Mamá Pasta siempre está para ayudarte.");

        } else if (lower.contains("adiós") || lower.contains("salir") || lower.contains("chau")) {
            addBotMessage("👋 ¡Hasta luego! No olvides que aquí estaremos cuando necesites tu dosis de pasta.");

        } else {
            addBotMessage("🤔 Lo siento, no entendí eso. Puedes decirme: 'historial', 'construir pizza', 'ver menú' o 'volver al inicio'.");
        }
    }
}
