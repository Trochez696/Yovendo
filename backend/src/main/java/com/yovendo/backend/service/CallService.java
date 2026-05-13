package com.yovendo.backend.service;

import com.yovendo.backend.dto.CallDTO;
import com.yovendo.backend.entity.Call;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.CallRepository;
import com.yovendo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final UserRepository userRepository;

    public List<CallDTO> getCallsForUser(User user) {
        // El consultor solo consulta sus registros; los demas perfiles autorizados consultan todo.
        boolean isConsultor = user.getRoles().stream().anyMatch(r -> "CONSULTOR".equalsIgnoreCase(r.getName()));
        List<Call> calls = isConsultor ? callRepository.findByConsultantIdOrderByCallDateDesc(user.getId()) : callRepository.findAll();
        return calls.stream().map(this::toDTO).toList();
    }

    @Transactional
    public CallDTO createCall(CallDTO dto, String username) {
        // La relacion con el consultor se toma del usuario autenticado, no del cuerpo recibido.
        User consultant = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario consultor no encontrado"));

        Call call = Call.builder()
                .clientName(dto.getClientName())
                .clientEmail(dto.getClientEmail())
                .clientPhone(dto.getClientPhone())
                .notes(dto.getNotes())
                .callType(dto.getCallType())
                .durationMinutes(dto.getDurationMinutes())
                .consultant(consultant)
                .build();

        call = callRepository.save(call);
        return toDTO(call);
    }

    private CallDTO toDTO(Call call) {
        return CallDTO.builder()
                .id(call.getId())
                .clientName(call.getClientName())
                .clientEmail(call.getClientEmail())
                .clientPhone(call.getClientPhone())
                .callDate(call.getCallDate())
                .notes(call.getNotes())
                .callType(call.getCallType())
                .durationMinutes(call.getDurationMinutes())
                .consultantId(call.getConsultant() != null ? call.getConsultant().getId() : null)
                .consultantName(call.getConsultant() != null ? call.getConsultant().getUsername() : null)
                .build();
    }
}
