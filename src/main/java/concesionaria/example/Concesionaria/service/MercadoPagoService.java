package concesionaria.example.Concesionaria.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import concesionaria.example.Concesionaria.entity.Publicacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${frontend.url}")
    private String frontendURL;
    @Value("${backend.base-url}")
    private String backendURL;
    @Value("${mercadopago.access-token}")
    private String accessToken;

    public String crearPreferenciaDePago(Publicacion publicacion, Long reservaId, double monto){

        try{
            MercadoPagoConfig.setAccessToken(accessToken);
            //1) Definimos el item que va a pagar el usuario (la reserva)
            PreferenceItemRequest preferenceItemRequest = PreferenceItemRequest.builder()
                    .title("Reserva del auto: " + publicacion.getAuto().getMarca() + " " + publicacion.getAuto().getModelo())
                    .quantity(1)
                    .unitPrice(new BigDecimal(monto))
                    .build();

            //2) Definimos URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendURL + "/pago-exitoso")
                    .failure(frontendURL + "/pago-fallido")
                    .pending(frontendURL + "/pago-pendiente")
                    .build();

            //3)Configuramos la URK de notificacion (el webhook)
            String notificacionURL = backendURL + "/notificacion/mercadopago?reserva_id=" + reservaId;

            //4)Creamos la solicitud de preferencia
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(preferenceItemRequest);

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    // external_reference es clave para identificar la reserva después del pago
                    .externalReference(reservaId.toString())
                    .backUrls(backUrls)
                    .notificationUrl(notificacionURL) // Aquí configuramos el Webhook
                    .autoReturn("approved")
                    .build();

            // 5. Crear la preferencia usando el cliente (SDK)
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 6. Devolver el link de redirección (init_point)
            return preference.getInitPoint();

        } catch (Exception e) {
            System.err.println("Error al crear la preferencia de pago en Mercado Pago para Reserva ID " + reservaId + ": " + e.getMessage());
            throw new RuntimeException("Fallo al iniciar el pago con Mercado Pago", e);
        }

    }
    public Payment obtenerDetallesDePago(String paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            // El ID debe convertirse a Long para la consulta
            return client.get(Long.valueOf(paymentId));
        } catch (Exception e) {
            System.err.println("Error al obtener detalles del pago " + paymentId + ": " + e.getMessage());
            throw new RuntimeException("Fallo al consultar el estado de pago en MP", e);
        }
    }
}

