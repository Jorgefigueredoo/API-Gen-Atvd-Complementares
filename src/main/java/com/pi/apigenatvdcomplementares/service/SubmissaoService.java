package com.pi.apigenatvdcomplementares.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.pi.apigenatvdcomplementares.enums.PerfilUsuario;
import com.pi.apigenatvdcomplementares.enums.StatusSubmissao;
import com.pi.apigenatvdcomplementares.models.Certificado;
import com.pi.apigenatvdcomplementares.models.Submissao;
import com.pi.apigenatvdcomplementares.models.Usuario;
import com.pi.apigenatvdcomplementares.repository.SubmissaoRepository;
import com.pi.apigenatvdcomplementares.repository.UsuarioRepository;

@Service
public class SubmissaoService {

    @Autowired
    private SubmissaoRepository submissaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Submissao criarSubmissao(Submissao submissao) {

        validarSubmissao(submissao);

        submissao.setStatus(StatusSubmissao.PENDENTE);
        submissao.setDataSubmissao(LocalDateTime.now());
        submissao.getHistoricoStatus().add(StatusSubmissao.PENDENTE);

        for (Certificado certificado : submissao.getCertificados()) {
            certificado.setSubmissao(submissao);
        }

        return submissaoRepository.save(submissao);
    }

    private void validarSubmissao(Submissao submissao) {
        if (submissao.getCertificados() == null || submissao.getCertificados().isEmpty()) {
            throw new IllegalArgumentException("A submissão deve conter pelo menos um certificado");
        }

        if (submissao.getCurso() == null) {
            throw new IllegalArgumentException("O campo de curso é obrigatório.");
        }

        if (submissao.getAluno() == null || submissao.getAluno().getUsuarioId() == null) {
            throw new IllegalArgumentException("O campo do aluno é obrigatório");
        }

        if (submissao.getHistoricoStatus() == null) {
            submissao.setHistoricoStatus(new HashSet<>());
        }
    }

    public List<Submissao> listarSubmissoes() {
        return submissaoRepository.findAll();
    }

    public Submissao buscarPorId(Long id) {
        return submissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submissão não encontrada"));
    }

    public List<Submissao> listarPorAlunoMatricula(String matricula) {
        return submissaoRepository.findAllByAlunoMatricula(matricula);
    }

    public List<Submissao> listarPendentes() {
        return submissaoRepository.findByStatus(StatusSubmissao.PENDENTE);
    }

    public Submissao aprovarSubmissao(Long id) {
        return alterarStatusSubmissao(id, StatusSubmissao.APROVADA);
    }

    public Submissao rejeitarSubmissao(Long id) {
        return alterarStatusSubmissao(id, StatusSubmissao.REPROVADA);
    }

    public Submissao alterarStatusSubmissao(Long id, StatusSubmissao novoStatus) {

        Submissao submissao = buscarPorId(id);

        if (submissao.getStatus() != StatusSubmissao.PENDENTE) {
            throw new IllegalStateException("Essa submissão já foi analisada");
        }

        submissao.setStatus(novoStatus);
        submissao.getHistoricoStatus().add(novoStatus);

        return submissaoRepository.save(submissao);
    }

    public void deletarSubmissao(Long id, Usuario usuario) {
        Submissao submissaoExistente = buscarPorId(id);

        boolean naoEstaPendente = submissaoExistente.getStatus() != StatusSubmissao.PENDENTE;
        boolean naoAdmin = usuario.getPerfil() != PerfilUsuario.SUPER_ADMIN;

        if (naoEstaPendente && naoAdmin) {
            throw new IllegalStateException("Não é possível excluir uma submissão já analisada");
        }

        submissaoRepository.delete(submissaoExistente);
    }

    // ────────────────────────────────────────────────────────────────
    // Histórico de Submissões — listagem e detalhe por usuário logado
    // ────────────────────────────────────────────────────────────────

    /**
     * Retorna o histórico de submissões visível para o usuário autenticado.
     *
     * Regras de visibilidade (isolamento por usuário):
     *  - ALUNO        → apenas as próprias submissões;
     *  - COORDENADOR  → submissões que avaliou (campo coordenador);
     *  - SUPER_ADMIN  → todas as submissões.
     *
     * Em todos os casos a lista é ordenada por dataSubmissao DESC.
     */
    public List<Submissao> listarHistoricoDoUsuario(String email) {
        Usuario usuario = buscarUsuarioOuFalhar(email);

        return switch (usuario.getPerfil()) {
            case ALUNO -> submissaoRepository
                    .findAllByAlunoUsuarioIdOrderByDataSubmissaoDesc(usuario.getId());
            case COORDENADOR -> submissaoRepository
                    .findAllByCoordenadorIdOrderByDataSubmissaoDesc(usuario.getId());
            case SUPER_ADMIN -> submissaoRepository.findAllByOrderByDataSubmissaoDesc();
        };
    }

    /**
     * Retorna o detalhe de uma submissão garantindo que o usuário autenticado
     * tem permissão para visualizá-la.
     */
    public Submissao buscarHistoricoPorId(Long id, String email) {
        Usuario usuario = buscarUsuarioOuFalhar(email);
        Submissao submissao = buscarPorId(id);

        boolean autorizado = switch (usuario.getPerfil()) {
            case SUPER_ADMIN -> true;
            case COORDENADOR -> submissao.getCoordenador() != null
                    && submissao.getCoordenador().getId().equals(usuario.getId());
            case ALUNO -> submissao.getAluno() != null
                    && usuario.getId().equals(submissao.getAluno().getUsuarioId());
        };

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Você não tem permissão para visualizar esta submissão.");
        }

        return submissao;
    }

    private Usuario buscarUsuarioOuFalhar(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
    }
}
