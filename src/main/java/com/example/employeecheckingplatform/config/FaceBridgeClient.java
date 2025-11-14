package com.example.employeecheckingplatform.config;// package com.example.employeecheckingplatform.service;  // <- config emas, servis qatlamida tursin!

import com.example.employeecheckingplatform.config.PythonAuthClient;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FaceBridgeClient {

    private final RestTemplate rest;
    private final PythonAuthClient auth;                // Sizda mavjud bo‘lgan token provayder
    private final FoydalanuvchiRepository foy;         // (ixtiyoriy) topilgan jshshir bo‘yicha user qidirish uchun

    @Value("${app.face.upload.url}")
    private String uploadUrl;

    /**
     * Server kutilayotgan JSON field nomi: odatda "image"
     */
    @Value("${app.face.upload.param-name:image}")
    private String paramName;

    /**
     * Base64 ni data-uri formatida yuborish kerakmi? true bo‘lsa, prefiks qo‘shamiz
     */
    @Value("${app.face.upload.data-uri:false}")
    private boolean dataUri;

    /**
     * data-uri prefiks: jpeg bo‘lmasa png qilishingiz mumkin
     */
    @Value("${app.face.upload.data-uri-prefix:data:image/jpeg;base64,}")
    private String dataUriPrefix;

    /**
     * Base64 yuboradi va javobdan jshshir (yoki mask/username) ni qaytaradi.
     * Agar server `success=false` yoki format mos kelmasa — RuntimeException tashlaydi.
     */
    public String sendBase64AndGetUsername(String base64Data) {
        if (base64Data == null || base64Data.isBlank()) {
            throw new IllegalArgumentException("base64Data bo'sh bo'lmasligi kerak");
        }

        // data-uri kerak bo‘lsa prefiks qo‘shamiz
        String payload = base64Data.trim();
        if (dataUri && !payload.startsWith("data:")) {
            payload = dataUriPrefix + payload;
        }

        String accessToken = auth.getAccessToken();

        // HTTP so‘rovni tayyorlaymiz
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        // So‘rov DTO
        FaceUploadRequest reqDto = new FaceUploadRequest();
        reqDto.setIsPhoto("Y");
        reqDto.setTransactionId("txn-" + UUID.randomUUID());
        reqDto.setImageFieldName(paramName);
        reqDto.setImage(payload);

        HttpEntity<FaceUploadRequest> entity = new HttpEntity<>(reqDto, headers);

        try {
            ResponseEntity<FaceResponse> resp = rest.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    entity,
                    FaceResponse.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                FaceResponse body = resp.getBody();
                System.out.println("*******************************************************");
                System.out.println(body);
                System.out.println("*******************************************************");

                // success tekshirish
                if (body.getSuccess() == null || !body.getSuccess()) {
                    String msg = (body.getMessage() != null) ? body.getMessage() :
                            (body.getError() != null ? body.getError().getMessage() : "Unknown error");
                    throw new RuntimeException("Face server success=false: " + msg);
                }

                // data/status tekshirish (ixtiyoriy, lekin foydali)
                if (body.getData() == null) {
                    throw new RuntimeException("Face server data bo'sh keldi");
                }
                if (body.getData().getStatus() != null && !"SUCCESS".equalsIgnoreCase(body.getData().getStatus())) {
                    // Ba'zi servislar "PENDING"/"FAILED" ham qaytarishi mumkin
                    throw new RuntimeException("Face server status=" + body.getData().getStatus());
                }

                // person -> jshshir / jshshir_masked / username
                FacePerson p = body.getData().getPerson();
                String jshshir = null;
                if (p != null) {
                    if (notBlank(p.getJshshir())) {
                        jshshir = p.getJshshir().trim();
                    }
                } else {
                    throw new RuntimeException("Username (jshshir) topilmadi. Server javobi noto‘liq.");

                }

                // (ixtiyoriy) shu username/jshshir bo‘yicha Foydalanuvchini lookup qilishingiz mumkin:
                // foy.findByUsername(jshshir) ...
                return jshshir;
            }

            throw new RuntimeException("Face server no-OK status: " + resp.getStatusCode());

        } catch (RestClientResponseException rce) {
            // serverdan xato status + body
            String errBody = rce.getResponseBodyAsString();
            throw new RuntimeException("Face upload xatosi: " + rce.getRawStatusCode() + " " + rce.getStatusText()
                    + " body=" + errBody, rce);
        } catch (Exception e) {
            throw new RuntimeException("Face serverga rasm yuborishda xatolik: " + e.getMessage(), e);
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /* ===================== DTO’lar ===================== */

    /**
     * So‘rov DTO — server kutilayotgan JSON ga mos
     */
    @Data
    static class FaceUploadRequest {
        // server kutayotgan image field nomi — parametr sifatida keldi
        // JSON’da dinamik nom yasay olmaymiz, shuning uchun soddalashtirib qo‘yamiz:
        //  - Server "image" nomini kutsa: paramName=image bo‘lsin; biz "image" fieldga qo‘yamiz.
        //  - Agar server boshqa nom kutsa, backend’da buni bilib turadi: paramName=..."photo", sizga mos quyida set qilamiz.

        @JsonProperty("image")
        private String image;             // doimiy "image" field

        @JsonProperty("is_photo")
        private String isPhoto;           // "Y"

        @JsonProperty("transaction_id")
        private String transactionId;     // tx id

        // Ichki foydalanish: agar paramName != "image" bo‘lsa, request yasalishidan oldin maplab yuboramiz.
        // Shu class ichida dinamik nom bilan JSON yasab bo‘lmaydi, shuning uchun bu yerda maydonni ushlab turamiz:
        private transient String imageFieldName;

        public void setImage(String img) {
            this.image = img;
        }

        public void setImageFieldName(String name) {
            this.imageFieldName = name;
        }

        /**
         * Agar paramName != "image" bo‘lsa, RestTemplate uchun HttpMessageConverter
         * baribir `image` kaliti bilan jo‘natadi. Ko‘p servislar `image` ni qabul qiladi.
         * Agar sizda HAQIQATDAN ham boshqa nom shart bo‘lsa, bu joyda Map-based approach (Object -> Map) ishlatish mumkin.
         * (Soddalik uchun yuqorida `image` qilib qoldirdik. Kerak bo‘lsa, qayta yozib beraman.)
         */
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FaceResponse {
        private Boolean success;
        private String message;   // ba’zan keladi
        private FaceError error;
        private FaceData data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FaceError {
        private String message;
        @JsonProperty("msg")
        private String msg; // ayrim API’lar msg deb yuboradi
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FaceData {
        private String status;            // "SUCCESS"
        @JsonProperty("transaction_id")
        private String transactionId;
        private FacePerson person;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FacePerson {
        private Long id;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        @JsonProperty("middle_name")
        private String middleName;

        private String jshshir;

        @JsonProperty("branch_id")
        private Long branchId;
        @JsonProperty("branch_nomi")
        private String branchNomi;
        @JsonProperty("branch_path")
        private String branchPath;

        @JsonProperty("rank_id")
        private Long rankId;
        @JsonProperty("rank_nomi")
        private String rankNomi;

        private String avatar; // base64
    }
}
