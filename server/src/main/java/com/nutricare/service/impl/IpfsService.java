package com.nutricare.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricare.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Service untuk menyimpan dan mengambil data dari IPFS melalui Pinata.
 * Digunakan untuk menyimpan dokumen Verifiable Credential (VC)
 * agar data tetap terdesentralisasi dan tidak dapat diubah.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpfsService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${pinata.api-key}")
    private String pinataApiKey;

    @Value("${pinata.secret-key}")
    private String pinataSecretKey;

    @Value("${pinata.api-url}")
    private String pinataApiUrl;

    @Value("${pinata.gateway-url}")
    private String pinataGatewayUrl;

    /**
     * Mengupload data JSON ke IPFS via Pinata dan mengembalikan CID.
     * Data akan di-pin agar tidak hilang dari IPFS.
     *
     * @param data objek yang akan diupload (akan di-serialize ke JSON)
     * @return CID (Content Identifier) dari file yang terupload
     * @throws StorageException jika upload gagal
     */
    public String uploadJson(Object data) {
        try {
            Map<String, Object> requestBody = Map.of(
                "pinataContent", data,
                "pinataMetadata", Map.of(
                    "name", "vc-" + System.currentTimeMillis()
                )
            );

            String response = webClient.post()
                .uri(pinataApiUrl + "/pinning/pinJSONToIPFS")
                .header("pinata_api_key", pinataApiKey)
                .header("pinata_secret_api_key", pinataSecretKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            return root.get("IpfsHash").asText();
        } catch (Exception e) {
            log.error("Gagal upload ke IPFS via Pinata: {}", e.getMessage());
            throw new StorageException("Gagal menyimpan data ke IPFS: " + e.getMessage());
        }
    }

    /**
     * Mengambil data JSON dari IPFS melalui Pinata gateway.
     *
     * @param cid Content Identifier file yang akan diambil
     * @return data JSON dalam bentuk String
     * @throws StorageException jika pengambilan gagal
     */
    public String getJson(String cid) {
        try {
            return webClient.get()
                .uri(pinataGatewayUrl + "/" + cid)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            log.error("Gagal mengambil data dari IPFS: {}", e.getMessage());
            throw new StorageException("Gagal mengambil data dari IPFS: " + e.getMessage());
        }
    }
}
