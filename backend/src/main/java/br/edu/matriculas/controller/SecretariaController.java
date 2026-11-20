package br.edu.matriculas.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.matriculas.dto.Dtos.CurriculoRequest;
import br.edu.matriculas.dto.Dtos.CursoRequest;
import br.edu.matriculas.dto.Dtos.CursoResponse;
import br.edu.matriculas.dto.Dtos.DisciplinaRequest;
import br.edu.matriculas.dto.Dtos.DisciplinaResponse;
import br.edu.matriculas.dto.Dtos.OfertaResponse;
import br.edu.matriculas.dto.Dtos.SemestreRequest;
import br.edu.matriculas.dto.Dtos.SemestreResponse;
import br.edu.matriculas.dto.Dtos.UsuarioRequest;
import br.edu.matriculas.dto.Dtos.UsuarioResponse;
import br.edu.matriculas.model.Papel;
import br.edu.matriculas.model.Semestre;
import br.edu.matriculas.repository.CursoRepository;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.SemestreRepository;
import br.edu.matriculas.repository.UsuarioRepository;
import br.edu.matriculas.service.CadastroService;
import br.edu.matriculas.service.Mapeador;
import br.edu.matriculas.service.SemestreService;
import jakarta.validation.Valid;

/** Funcionalidades da secretaria: cadastros, currículo e período de matrículas. */
@RestController
@RequestMapping("/api/secretaria")
public class SecretariaController {

    private final CadastroService cadastro;
    private final SemestreService semestreService;
    private final CursoRepository cursos;
    private final DisciplinaRepository disciplinas;
    private final UsuarioRepository usuarios;
    private final SemestreRepository semestres;
    private final OfertaRepository ofertas;
    private final Mapeador mapeador;

    public SecretariaController(CadastroService cadastro, SemestreService semestreService, CursoRepository cursos,
            DisciplinaRepository disciplinas, UsuarioRepository usuarios, SemestreRepository semestres,
            OfertaRepository ofertas, Mapeador mapeador) {
        this.cadastro = cadastro;
        this.semestreService = semestreService;
        this.cursos = cursos;
        this.disciplinas = disciplinas;
        this.usuarios = usuarios;
        this.semestres = semestres;
        this.ofertas = ofertas;
        this.mapeador = mapeador;
    }

    // ---------- cursos ----------

    @GetMapping("/cursos")
    List<CursoResponse> cursos() {
        return cursos.findAll().stream().sorted(Comparator.comparing(c -> c.getNome())).map(mapeador::curso).toList();
    }

    @PostMapping("/cursos")
    @ResponseStatus(HttpStatus.CREATED)
    CursoResponse criarCurso(@Valid @RequestBody CursoRequest r) {
        return mapeador.curso(cadastro.criarCurso(r));
    }

    @PutMapping("/cursos/{id}")
    CursoResponse atualizarCurso(@PathVariable String id, @Valid @RequestBody CursoRequest r) {
        return mapeador.curso(cadastro.atualizarCurso(id, r));
    }

    @DeleteMapping("/cursos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluirCurso(@PathVariable String id) {
        cadastro.excluirCurso(id);
    }

    // ---------- disciplinas ----------

    @GetMapping("/disciplinas")
    List<DisciplinaResponse> disciplinas() {
        return disciplinas.findAll().stream().sorted(Comparator.comparing(d -> d.getCodigo()))
                .map(mapeador::disciplina).toList();
    }

    @PostMapping("/disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    DisciplinaResponse criarDisciplina(@Valid @RequestBody DisciplinaRequest r) {
        return mapeador.disciplina(cadastro.criarDisciplina(r));
    }

    @PutMapping("/disciplinas/{id}")
    DisciplinaResponse atualizarDisciplina(@PathVariable String id, @Valid @RequestBody DisciplinaRequest r) {
        return mapeador.disciplina(cadastro.atualizarDisciplina(id, r));
    }

    @DeleteMapping("/disciplinas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluirDisciplina(@PathVariable String id) {
        cadastro.excluirDisciplina(id);
    }

    // ---------- professores ----------

    @GetMapping("/professores")
    List<UsuarioResponse> professores() {
        return listar(Papel.PROFESSOR);
    }

    @PostMapping("/professores")
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResponse criarProfessor(@Valid @RequestBody UsuarioRequest r) {
        return mapeador.usuario(cadastro.criarUsuario(Papel.PROFESSOR, r));
    }

    @PutMapping("/professores/{id}")
    UsuarioResponse atualizarProfessor(@PathVariable String id, @Valid @RequestBody UsuarioRequest r) {
        return mapeador.usuario(cadastro.atualizarUsuario(Papel.PROFESSOR, id, r));
    }

    @DeleteMapping("/professores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluirProfessor(@PathVariable String id) {
        cadastro.excluirUsuario(Papel.PROFESSOR, id);
    }

    // ---------- alunos ----------

    @GetMapping("/alunos")
    List<UsuarioResponse> alunos() {
        return listar(Papel.ALUNO);
    }

    @PostMapping("/alunos")
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResponse criarAluno(@Valid @RequestBody UsuarioRequest r) {
        return mapeador.usuario(cadastro.criarUsuario(Papel.ALUNO, r));
    }

    @PutMapping("/alunos/{id}")
    UsuarioResponse atualizarAluno(@PathVariable String id, @Valid @RequestBody UsuarioRequest r) {
        return mapeador.usuario(cadastro.atualizarUsuario(Papel.ALUNO, id, r));
    }

    @DeleteMapping("/alunos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluirAluno(@PathVariable String id) {
        cadastro.excluirUsuario(Papel.ALUNO, id);
    }

    private List<UsuarioResponse> listar(Papel papel) {
        return usuarios.findByPapel(papel).stream().sorted(Comparator.comparing(u -> u.getNome()))
                .map(mapeador::usuario).toList();
    }

    // ---------- semestres e currículo ----------

    @GetMapping("/semestres")
    List<SemestreResponse> semestres() {
        return semestres.findAll().stream().sorted(Comparator.comparing(Semestre::getRotulo).reversed())
                .map(mapeador::semestre).toList();
    }

    @PostMapping("/semestres")
    @ResponseStatus(HttpStatus.CREATED)
    SemestreResponse criarSemestre(@Valid @RequestBody SemestreRequest r) {
        return mapeador.semestre(semestreService.criar(r.rotulo()));
    }

    @PostMapping("/semestres/{rotulo}/curriculo")
    List<OfertaResponse> gerarCurriculo(@PathVariable String rotulo, @Valid @RequestBody CurriculoRequest r) {
        return semestreService.gerarCurriculo(rotulo, r.cursoId(), r.disciplinaIds()).stream()
                .map(o -> mapeador.oferta(o, null)).toList();
    }

    @PostMapping("/semestres/{rotulo}/abrir")
    SemestreResponse abrir(@PathVariable String rotulo) {
        return mapeador.semestre(semestreService.abrirMatriculas(rotulo));
    }

    @PostMapping("/semestres/{rotulo}/encerrar")
    SemestreResponse encerrar(@PathVariable String rotulo) {
        return mapeador.semestre(semestreService.encerrarMatriculas(rotulo));
    }

    @GetMapping("/ofertas")
    List<OfertaResponse> ofertas(@RequestParam String semestre) {
        return ofertas.findBySemestre(semestre).stream().map(o -> mapeador.oferta(o, null))
                .sorted(Comparator.comparing(o -> o.disciplina().codigo())).toList();
    }
}
