package com.pi.apigenatvdcomplementares.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pi.apigenatvdcomplementares.enums.StatusSubmissao;
import com.pi.apigenatvdcomplementares.models.Submissao;

@Repository
public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {
    List<Submissao> findAllByAlunoMatricula(String matricula); // Método para encontrar todas as submissões de um aluno pela matrícula

    boolean existsByAlunoMatricula(String matricula); // Método para verificar se existem submissões para um aluno com a matrícula fornecida

    List<Submissao> findByStatus(StatusSubmissao pendente); // Método para encontrar todas as submissões com um status específico

    // Histórico — filtra pelo usuário responsável (aluno) usando o id do usuário
    List<Submissao> findAllByAlunoUsuarioIdOrderByDataSubmissaoDesc(Long usuarioId);

    // Histórico — filtra pelas submissões avaliadas por um coordenador
    List<Submissao> findAllByCoordenadorIdOrderByDataSubmissaoDesc(Long coordenadorId);

    // Histórico — listagem completa ordenada (perfil SUPER_ADMIN)
    List<Submissao> findAllByOrderByDataSubmissaoDesc();
}
