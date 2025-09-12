package com.sesac.solbid.upload;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 업로드/최종 저장 키 생성기
 */
@Component
public class KeyFactory {

    private static final String KEY_PREFIX = "products/";
    private static final String TMP_PREFIX  = KEY_PREFIX + "tmp/";
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    /** 임시 키: products/tmp/{yyyyMM}/{uuid}.{ext} */
    public String tmpKey(String ext) {
        String yyyymm = YearMonth.now(ZONE).format(YM);
        String uuid = UUID.randomUUID().toString();
        return TMP_PREFIX + yyyymm + "/" + uuid + "." + ext;
    }

    /** 최종 키: products/{productId}/{yyyyMM}/{uuid}.{ext} */
    public String finalKey(Long productId, String tmpKey) {
        // tmpKey에서 "products/tmp/" 이후 경로(yyyyMM/uuid.ext)를 그대로 보존
        String suffix = tmpKey.substring(TMP_PREFIX.length());
        return KEY_PREFIX + productId + "/" + suffix;
    }

}
