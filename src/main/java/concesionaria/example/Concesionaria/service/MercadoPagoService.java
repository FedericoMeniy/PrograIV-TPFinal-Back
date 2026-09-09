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
import java.util.List;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class MercadoPagoService {

    @Value("${frontend.url}")
    private String frontendURL;

    @Value("${backend.base-url}")
    private String backendURL;

    @Value("${mercadopago.access-token.private}")
    private String accessToken;

    public String crearPreferenciaDePago(Publicacion publicacion, Long reservaId, double monto) {

        MercadoPagoConfig.setAccessToken(accessToken);
        System.out.println(accessToken);

        try {
            PreferenceItemRequest preferenceItemRequest = PreferenceItemRequest.builder()
                    .title("Reserva del auto: " + publicacion.getAuto().getMarca() + " " + publicacion.getAuto().getModelo())
                    .quantity(1)
                    .unitPrice(new BigDecimal(monto))
                    .currencyId("ARS")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendURL + "pago-exitoso")
                    .failure(frontendURL + "pago-fallido")
                    .pending(frontendURL + "pago-pendiente")
                    .build();

            String notificacionURL = backendURL + "notificacion/mercadopago?reserva_id=" + reservaId;

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(preferenceItemRequest);

            OffsetDateTime fechaExpiracion = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(2);

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(reservaId.toString())
                    .backUrls(backUrls)
                    .notificationUrl(notificacionURL)
                    .expires(true)
                    .dateOfExpiration(fechaExpiracion)
                    .build();

            PreferenceClient client = new PreferenceClient();

            try {
                Preference preference = client.create(preferenceRequest);
                return preference.getInitPoint();

            } catch (Exception e) {
                System.err.println(">>> ERROR AL CREAR PREFERENCIA <<<");
                e.printStackTrace();

                if (e instanceof com.mercadopago.exceptions.MPApiException apiEx) {
                    System.err.println(">>> RESPONSE MP <<<");
                    System.err.println(apiEx.getApiResponse().getContent());
                }

                throw new RuntimeException("Error MercadoPago", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Fallo al crear la preferencia", e);
        }
    }

    public Payment obtenerDetallesDePago(String paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            return client.get(Long.valueOf(paymentId));
        } catch (Exception e) {
            System.err.println("Error al obtener detalles del pago " + paymentId + ": " + e.getMessage());
            throw new RuntimeException("Fallo al consultar el estado de pago en MP", e);
        }
    }
}