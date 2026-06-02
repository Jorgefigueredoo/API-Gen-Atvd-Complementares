package com.pi.apigenatvdcomplementares.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pi.apigenatvdcomplementares.dto.HistoricoSubmissaoDTO;
import com.pi.apigenatvdcomplementares.service.SubmissaoService;

/**
 * Endpoints relacionados a submissões.
 *
 * Por enquanto expõe apenas os endpoints de Histórico de Submissões.
 * Outras operações (criar, aprovar, rejeitar) continuam disponíveis via
 * SubmissaoService e podem ser expostas posteriormente sem conflitar
 * com este controller, pois usam paths distintos.
 */
@RestController
@RequestMapping("/submissoes")
public class SubmissaoController {

    @Autowired
    private SubmissaoService submissaoService;

    /**
     * Lista o histórico de submissões do usuário autenticado.
     *
     * - ALUNO        → as próprias submissões;
     * - COORDENADOR  → as submissões que avaliou;
     * - SUPER_ADMIN  → todas as submissões.
     */
    @GetMapping("/historico")
    public ResponseEntity<List<HistoricoSubmissaoDTO>> listarHistorico(Authentication authentication) {
        String email = authentication.getName();

        List<HistoricoSubmissaoDTO> historico = submissaoService
                .listarHistoricoDoUsuario(email)
                .stream()
                .map(HistoricoSubmissaoDTO::new)
                .toList();

        return ResponseEntity.ok(historico);
    }

    /**
     * Retorna o detalhe de uma submissão do histórico.
     * A autorização por perfil é feita no service e propaga 403 quando negado.
     */
    @GetMapping("/historico/{id}")
    public ResponseEntity<HistoricoSubmissaoDTO> buscarHistoricoPorId(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        HistoricoSubmissaoDTO dto = new HistoricoSubmissaoDTO(
                submissaoService.buscarHistoricoPorId(id, email));

        return ResponseEntity.ok(dto);
    }
}
