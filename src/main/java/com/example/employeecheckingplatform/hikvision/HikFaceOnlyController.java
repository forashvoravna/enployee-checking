package com.example.employeecheckingplatform.hikvision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/hikvision")
public class HikFaceOnlyController {

    private final ObjectMapper om = new ObjectMapper();

    @PostMapping(path = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> onEvent(HttpServletRequest req) throws Exception {
        for (Part part : req.getParts()) {
            String text = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (text.isEmpty()) continue;

            // part ichida bitta yoki ketma-ket JSON ob'ektlar bo'lishi mumkin
            parseJsons(text).forEach(this::processJsonNode);
        }
        return ResponseEntity.ok("ok");
    }

    // text ichidagi bitta yoki bir nechta JSON ob'ektlarni oqimga aylantiradi
    private Stream<JsonNode> parseJsons(String text) {
        try {
            // jackson MappingIterator ketma-ket JSONlarni o'qiy oladi (yaxshi va oddiy)
            MappingIterator<JsonNode> it = om.readerFor(JsonNode.class).readValues(new StringReader(text));
            Iterable<JsonNode> iterable = () -> it;
            return StreamSupport.stream(iterable.spliterator(), false);
        } catch (Exception e) {
            // fallback: oddiy split (} { kabi ketma-ketliklar uchun)
            String[] parts = text.replaceAll("\\}\\s*\\{", "}<<<S>>>\\{").split("<<<S>>>");
            return Stream.of(parts)
                    .map(p -> {
                        try {
                            return om.readTree(p);
                        } catch (Exception ex) {
                            return null;
                        }
                    })
                    .filter(node -> node != null);
        }
    }

    // minimal, aniq va ixcham ishlov beruvchi metod
    private void processJsonNode(JsonNode root) {
        if (root == null) return;
        if (!"AccessControllerEvent".equalsIgnoreCase(root.path("eventType").asText(""))) return;

        JsonNode ace = root.path("AccessControllerEvent");
        int major = ace.path("majorEventType").asInt(-1);
        int sub = ace.path("subEventType").asInt(-1);
        if (!(major == 5 && sub == 75)) return; // faqat face-success

        String ip = root.path("ipAddress").asText("");
        String dateTime = root.path("dateTime").asText("");
        String name = ace.path("name").asText("");
        String empNo = ace.path("employeeNoString").asText("");
        Integer serial = ace.path("serialNo").isInt() ? ace.path("serialNo").asInt() : null;

        // bu yerga DB/queue qo'yish mumkin
        System.out.printf("FACE SUCCESS -> ip=%s time=%s name=%s emp=%s serial=%s%n",
                ip, dateTime, name, empNo, serial);
    }
}



