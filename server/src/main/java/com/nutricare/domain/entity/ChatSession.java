package com.nutricare.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Entity ChatSession — tabel "chat_sessions"
 *
 * Satu sesi chat terhubung ke SATU prediksi (one-to-one).
 * Semua pesan disimpan dalam satu kolom JSONB (messages)
 * sebagai array — lebih efisien dari menyimpan tiap pesan sebagai row.
 *
 * Struktur messages (JSONB array):
 * [
 *   {
 *     "role": "user",
 *     "content": "Anak saya susah makan...",
 *     "timestamp": "2025-07-24T10:00:00Z"
 *   },
 *   {
 *     "role": "assistant",
 *     "content": "Berdasarkan kondisi Andi...",
 *     "timestamp": "2025-07-24T10:00:05Z"
 *   }
 * ]
 *
 * updated_at diupdate setiap kali ada pesan baru ditambahkan.
 */
@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // One-to-One ke Prediction — satu sesi per prediksi
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id", nullable = false, unique = true)
    private Prediction prediction;

    /**
     * Array pesan dalam format JSONB.
     * Disimpan sebagai String JSON, di-parse di service layer.
     *
     * Contoh isi:
     * [{"role":"user","content":"...","timestamp":"..."},
     *  {"role":"assistant","content":"...","timestamp":"..."}]
     */
    @Column(nullable = false, columnDefinition = "JSONB DEFAULT '[]'")
    @JdbcTypeCode(SqlTypes.JSON)
    private String messages = "[]";

    // Diupdate setiap ada pesan baru
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
