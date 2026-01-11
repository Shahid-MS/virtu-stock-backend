// package com.virtu_stock.Mail.MailBoxLayer;

// import java.net.URI;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.ParameterizedTypeReference;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.web.util.UriComponentsBuilder;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class EmailVerificationService {
//     @Value("${mailboxlayer.access.key}")
//     private String mailBoxLayerAccessKey;
//     @Value("${mailboxlayer.url}")
//     private String mailBoxLayerURL;

//     private final RestTemplate restTemplate;

//     public boolean verifyEmail(String email) {
//         URI uri = UriComponentsBuilder.newInstance()
//                 .scheme("https")
//                 .host(mailBoxLayerURL.replace("https://", "").replace("http://", ""))
//                 .queryParam("access_key", mailBoxLayerAccessKey)
//                 .queryParam("email", email)
//                 .build()
//                 .toUri();

//         try {
//             ResponseEntity<Map<String, Object>> response = restTemplate.exchange(uri, HttpMethod.GET, null,
//                     new ParameterizedTypeReference<Map<String, Object>>() {
//                     });

//             Map<String, Object> body = response.getBody();

//             if (body == null) {
//                 return false;
//             }

//             boolean formatValid = (Boolean) body.getOrDefault("format_valid", false);
//             boolean mxFound = (Boolean) body.getOrDefault("mx_found", false);
//             boolean smtpCheck = (Boolean) body.getOrDefault("smtp_check", false);
//             boolean disposable = (Boolean) body.getOrDefault("disposable", true);
//             double score = Double.parseDouble(body.get("score").toString());
//             return formatValid && mxFound && smtpCheck && !disposable && score > 0.5;
//         } catch (Exception e) {
//             System.err.println("Email verification failed for " + email + ": " + e.getMessage());
//             return false;
//         }
//     }
// }
