package com.example.rocketsms;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rocketsms.Models.Envio;
import com.example.rocketsms.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 1001;
    private ActivityMainBinding binding;
    private String pendingPhoneNumber;
    private String pendingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<Envio> listaEnvio = new ArrayList<>();

        Envio envio  = new Envio();
        envio.telefone = "024992279743";
        envio.mensagem = "Esta é a mensagem do teste 01";

        Envio envio2 = new Envio();
        envio2.telefone = "024992279743";
        envio2.mensagem = "Esta é a mensagem do teste 02";

        Envio envio3 = new Envio();
        envio3.telefone = "024992279743";
        envio3.mensagem = "Esta é a mensagem do teste 03";

        listaEnvio.add(envio);
        listaEnvio.add(envio2);
        listaEnvio.add(envio3);

        binding.buttonSendSms.setOnClickListener(v -> {
            String phoneNumber = binding.editTextPhoneNumber.getText().toString().trim();
            String message = binding.editTextMessage.getText().toString().trim();

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, R.string.error_phone_required, Toast.LENGTH_SHORT).show();
                return;
            }

            if (message.isEmpty()) {
                Toast.makeText(this, R.string.error_message_required, Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasSmsPermission()) {
                for(Envio item : listaEnvio)
                {
                    sendSms(item.telefone, item.mensagem);
                }

            } else {
                pendingPhoneNumber = phoneNumber;
                pendingMessage = message;
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_REQUEST_CODE
                );
            }
        });
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void sendSms(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, R.string.success_sms_sent, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, R.string.error_sms_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingPhoneNumber != null && pendingMessage != null) {
                    sendSms(pendingPhoneNumber, pendingMessage);
                }
            } else {
                Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
