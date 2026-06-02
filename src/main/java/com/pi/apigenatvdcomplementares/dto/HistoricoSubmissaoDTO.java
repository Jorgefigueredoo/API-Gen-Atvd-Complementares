package com.pi.apigenatvdcomplementares.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.pi.apigenatvdcomplementares.enums.StatusSubmissao;
import com.pi.apigenatvdcomplementares.models.Submissao;

import lombok.Getter;

/**
 * Representação enxuta de uma submissão para a tela de Histórico.
 *
 * Reúne os campos que a UI (web + mobile) precisa exibir:
 *  - identificação (id, título derivado do curso);
 *  - tipo da submissão (sempre "Atividade Complementar" no domínio atual,
 *    mas mantido como campo para permitir evolução sem quebrar o contrato);
 *  - data/hora da submissão;
 *  - usuário responsável (aluno);
 *  - status atual + histórico de status;
 *  - quantidade de certificados anexados (registros processados);
 *  - observação/feedback (nome do coordenador que avaliou, quando houver).
 */
@Getter
public class HistoricoSubmissaoDTO {

    private Long id;
    private String identificacao;
    private String tipo;
    private LocalDateTime dataSubmissao;
    private Long alunoId;
    private String alunoNome;
    private String cursoNome;
    private StatusSubmissao status;
    private Set<StatusSubmissao> historicoStatus;
    private int quantidadeRegistros;
    private String observacao;
    private Set<CertificadoDTO> certificados;

    public HistoricoSubmissaoDTO(Submissao s) {
        this.id = s.getId();
        this.dataSubmissao = s.getDataSubmissao();
        this.status = s.getStatus();
        this.historicoStatus = s.getHistoricoStatus();
        this.cursoNome = s.getCurso() != null ? s.getCurso().getNome() : null;
        this.alunoId = s.getAluno() != null ? s.getAluno().getUsuarioId() : null;
        this.alunoNome = s.getAluno() != null && s.getAluno().getUsuario() != null
                ? s.getAluno().getUsuario().getNome()
                : null;
        this.tipo = "Atividade Complementar";
        this.identificacao = (this.cursoNome != null ? this.cursoNome + " — " : "")
                + "Submissão #" + this.id;

        this.certificados = s.getCertificados() == null
                ? java.util.Collections.emptySet()
                : s.getCertificados().stream()
                        .map(CertificadoDTO::new)
                        .collect(Collectors.toSet());
        this.quantidadeRegistros = this.certificados.size();

        this.observacao = s.getCoordenador() != null
                ? "Avaliada por " + s.getCoordenador().getNome()
                : null;
    }
}
