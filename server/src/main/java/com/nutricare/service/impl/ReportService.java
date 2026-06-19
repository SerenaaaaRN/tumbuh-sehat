package com.nutricare.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.nutricare.domain.entity.Assessment;
import com.nutricare.domain.entity.Child;
import com.nutricare.domain.entity.Prediction;
import com.nutricare.exception.ForbiddenException;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.repository.AssessmentRepository;
import com.nutricare.repository.ChildRepository;
import com.nutricare.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service untuk generate laporan PDF tumbuh kembang anak.
 * Laporan berisi data anak, riwayat assessment, hasil prediksi,
 * dan rekomendasi yang dapat diunduh oleh orang tua atau tenaga medis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ChildRepository childRepository;
    private final AssessmentRepository assessmentRepository;
    private final PredictionRepository predictionRepository;

    /**
     * Menghasilkan laporan PDF untuk seorang anak dalam rentang tanggal tertentu.
     *
     * @param childId ID anak
     * @param userId ID user yang meminta
     * @param from tanggal awal filter
     * @param to tanggal akhir filter
     * @return byte array file PDF
     */
    public byte[] generateChildReport(String childId, String userId, LocalDate from, LocalDate to) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new ResourceNotFoundException("Anak tidak ditemukan"));

        if (!child.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Anda tidak memiliki akses ke data ini");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            addTitle(document, child);
            addChildInfo(document, child);
            addAssessmentHistory(document, childId, from, to);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Gagal generate PDF laporan anak {}: {}", childId, e.getMessage());
            throw new RuntimeException("Gagal membuat laporan PDF", e);
        }
    }

    /**
     * Menambahkan judul laporan.
     */
    private void addTitle(Document document, Child child) {
        document.add(new Paragraph("Laporan Tumbuh Kembang Anak")
            .setFontSize(18).setBold());
        document.add(new Paragraph("Aplikasi Deteksi Stunting - GiziChain")
            .setFontSize(10));
        document.add(new Paragraph("\n"));
    }

    /**
     * Menambahkan informasi dasar anak.
     */
    private void addChildInfo(Document document, Child child) {
        int ageMonths = Period.between(child.getBirthDate(), LocalDate.now()).getMonths()
            + Period.between(child.getBirthDate(), LocalDate.now()).getYears() * 12;

        document.add(new Paragraph("Data Anak").setFontSize(14).setBold());

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Nama", child.getName());
        addTableRow(table, "Jenis Kelamin", child.getGender().name().equals("MALE") ? "Laki-laki" : "Perempuan");
        addTableRow(table, "Tanggal Lahir", child.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addTableRow(table, "Usia", ageMonths + " bulan");

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Menambahkan riwayat assessment dan prediksi.
     */
    private void addAssessmentHistory(Document document, String childId, LocalDate from, LocalDate to) {
        List<Assessment> assessments = assessmentRepository
            .findByChildIdAndDateRange(childId, from.atStartOfDay(), to.atTime(23, 59, 59));

        if (assessments.isEmpty()) {
            document.add(new Paragraph("Tidak ada data assessment dalam rentang tanggal yang dipilih."));
            return;
        }

        document.add(new Paragraph("Riwayat Assessment").setFontSize(14).setBold());

        for (Assessment assessment : assessments) {
            document.add(new Paragraph(
                "Tanggal: " + assessment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).setFontSize(12).setBold());

            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
            table.setWidth(UnitValue.createPercentValue(100));

            addTableRow(table, "Berat Badan", assessment.getWeight() + " kg");
            addTableRow(table, "Tinggi Badan", assessment.getHeight() + " cm");
            if (assessment.getHeadCircumference() != null) {
                addTableRow(table, "Lingkar Kepala", assessment.getHeadCircumference() + " cm");
            }
            addTableRow(table, "ASI Eksklusif", assessment.getBfExclusive() ? "Ya" : "Tidak");
            addTableRow(table, "Frekuensi Makan", assessment.getMealFreq() + " kali/hari");

            document.add(table);

            Prediction prediction = predictionRepository.findByAssessmentId(assessment.getId()).orElse(null);
            if (prediction != null && prediction.getSummary() != null) {
                document.add(new Paragraph("Hasil Prediksi:").setFontSize(11).setBold());
                document.add(new Paragraph(prediction.getSummary()).setFontSize(10));

                if (prediction.getRecommendations() != null && !prediction.getRecommendations().isEmpty()) {
                    document.add(new Paragraph("Rekomendasi:").setFontSize(11).setBold());
                    for (String rec : prediction.getRecommendations()) {
                        document.add(new Paragraph("- " + rec).setFontSize(10));
                    }
                }
            }

            document.add(new Paragraph("Disclaimer: Hasil ini bersifat skrining awal dan bukan diagnosis medis. Konsultasikan dengan dokter atau tenaga kesehatan.")
                .setFontSize(8).setItalic());
            document.add(new Paragraph("\n"));
        }
    }

    /**
     * Menambahkan baris ke tabel dengan key dan value.
     */
    private void addTableRow(Table table, String key, String value) {
        table.addCell(new Cell().add(new Paragraph(key).setBold().setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(10)));
    }
}
