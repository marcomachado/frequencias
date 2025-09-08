package com.petsaudedigital.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.petsaudedigital.backend.api.dto.TimesheetDtos;
import com.petsaudedigital.backend.domain.Attendance;
import com.petsaudedigital.backend.domain.enums.AttendanceStatus;
import com.petsaudedigital.backend.repository.AttendanceRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimesheetService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public TimesheetDtos.Summary compute(Long userId, String from, String to) {
        userRepository.findById(userId).orElseThrow();
        List<Attendance> all = attendanceRepository.findByUserAndDateRange(userId, from, to);
        List<Attendance> valids = all.stream().filter(a -> a.getStatus() == AttendanceStatus.validada).toList();
        List<TimesheetDtos.Item> itens = valids.stream()
                .sorted(Comparator.comparing(a -> a.getActivity().getData()))
                .map(a -> {
                    long min = minutes(a);
                    return new TimesheetDtos.Item(a.getActivity().getId(), a.getActivity().getData(), a.getActivity().getInicio(), a.getActivity().getFim(), min);
                }).toList();
        long totalMin = itens.stream().mapToLong(TimesheetDtos.Item::minutos).sum();
        double horas = Math.round((totalMin / 60.0) * 100.0) / 100.0;
        boolean insuf = horas < 32.0; // padrão do SPEC
        return new TimesheetDtos.Summary(totalMin, horas, insuf, itens);
    }

    public byte[] generatePdf(Long userId, String from, String to) {
        var user = userRepository.findById(userId).orElseThrow();
        TimesheetDtos.Summary s = compute(userId, from, to);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();
            doc.add(new Paragraph("Folha de Frequência"));
            doc.add(new Paragraph("Usuário: " + user.getNome() + " (" + user.getEmail() + ")"));
            doc.add(new Paragraph("Período: " + from + " a " + to));
            doc.add(new Paragraph(String.format("Total: %.2f horas (insuficiente: %s)", s.totalHoras(), s.horasInsuficientes())));
            doc.add(new Paragraph("Itens:"));
            for (var it : s.itens()) {
                doc.add(new Paragraph(String.format("#%d %s %s-%s (%d min)", it.activityId(), it.data(), it.inicio(), it.fim(), it.minutos())));
            }
            doc.add(new Paragraph("\nAssinatura do Bolsista: ________________________________"));
            doc.add(new Paragraph("Assinatura do Coordenador/Tutor: _______________________"));
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar PDF", e);
        }
    }

    private long minutes(Attendance a) {
        LocalTime i = LocalTime.parse(a.getActivity().getInicio());
        LocalTime f = LocalTime.parse(a.getActivity().getFim());
        return Duration.between(i, f).toMinutes();
    }
}

