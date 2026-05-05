// Yangilash uchun DTO (qisman update, null kelganlar o‘zgarmaydi)
package com.example.employeecheckingplatform.dto.imtihon;

import java.util.Set;

public record ImtihonUpdateDto(
        Long fanId,
        String nomi,
        Integer davomiylikDaqiqa,
        Integer maxUrinish,
        Integer savolSoni,
        Integer aloPct,
        Integer yaxshiPct,
        Integer qoniqarliPct,
        Integer tekshirishVaqti,
        Integer tekshirishSoni

) {}
