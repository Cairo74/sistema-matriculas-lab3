package br.edu.matriculas.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.matriculas.dto.Dtos.CursoRequest;
import br.edu.matriculas.dto.Dtos.DisciplinaRequest;
import br.edu.matriculas.dto.Dtos.UsuarioRequest;
import br.edu.matriculas.exception.NaoEncontradoException;
import br.edu.matriculas.exception.RegraNegocioException;
import br.edu.matriculas.model.Curso;
import br.edu.matriculas.model.Disciplina;
import br.edu.matriculas.model.Papel;
import br.edu.matriculas.model.Usuario;
import br.edu.matriculas.repository.CursoRepository;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.MatriculaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.UsuarioRepository;

/** Cadastros mantidos pela secretaria: cursos, disciplinas, professores e alunos. */
@Service
public class CadastroService {

    private final CursoRepository cursos;
    private final DisciplinaRepository disciplinas;
    private final UsuarioRepository usuarios;
    private final OfertaRepository ofertas;
    private final MatriculaRepository matriculas;
    private final PasswordEncoder encoder;

    public CadastroService(CursoRepository cursos, DisciplinaRepository disciplinas, UsuarioRepository usuarios,
            OfertaRepository ofertas, MatriculaRepository matriculas, PasswordEncoder encoder) {
        this.cursos = cursos;
        this.disciplinas = disciplinas;
        this.usuarios = usuarios;
        this.ofertas = ofertas;
        this.matriculas = matriculas;
        this.encoder = encoder;
    }

    // ---------- cursos ----------

    public Curso criarCurso(CursoRequest r) {
        return cursos.save(new Curso(r.nome().trim(), r.creditos()));
    }

    public Curso atualizarCurso(String id, CursoRequest r) {
        Curso c = cursos.findById(id).orElseThrow(() -> new NaoEncontradoException("Curso"));
        c.setNome(r.nome().trim());
        c.setCreditos(r.creditos());
        return cursos.save(c);
    }

    public void excluirCurso(String id) {
        if (disciplinas.countByCursoId(id) > 0 || usuarios.countByCursoId(id) > 0) {
            throw new RegraNegocioException("O curso possui disciplinas ou alunos vinculados.");
        }
        if (!cursos.existsById(id)) {
            throw new NaoEncontradoException("Curso");
        }
        cursos.deleteById(id);
    }

    // ---------- disciplinas ----------

    public Disciplina criarDisciplina(DisciplinaRequest r) {
        if (disciplinas.existsByCodigo(r.codigo().trim())) {
            throw new RegraNegocioException("Já existe uma disciplina com o código " + r.codigo().trim() + ".");
        }
        return disciplinas.save(preencher(new Disciplina(), r));
    }

    public Disciplina atualizarDisciplina(String id, DisciplinaRequest r) {
        Disciplina d = disciplinas.findById(id).orElseThrow(() -> new NaoEncontradoException("Disciplina"));
        if (!d.getCodigo().equals(r.codigo().trim()) && disciplinas.existsByCodigo(r.codigo().trim())) {
            throw new RegraNegocioException("Já existe uma disciplina com o código " + r.codigo().trim() + ".");
        }
        return disciplinas.save(preencher(d, r));
    }

    public void excluirDisciplina(String id) {
        if (!ofertas.findByDisciplinaIdIn(List.of(id)).isEmpty()) {
            throw new RegraNegocioException("A disciplina já foi ofertada em algum semestre.");
        }
        if (!disciplinas.existsById(id)) {
            throw new NaoEncontradoException("Disciplina");
        }
        disciplinas.deleteById(id);
    }

    private Disciplina preencher(Disciplina d, DisciplinaRequest r) {
        cursos.findById(r.cursoId()).orElseThrow(() -> new NaoEncontradoException("Curso"));
        String professorId = r.professorId() == null || r.professorId().isBlank() ? null : r.professorId();
        if (professorId != null) {
            usuarios.findById(professorId).filter(u -> u.getPapel() == Papel.PROFESSOR)
                    .orElseThrow(() -> new NaoEncontradoException("Professor"));
        }
        d.setCodigo(r.codigo().trim().toUpperCase());
        d.setNome(r.nome().trim());
        d.setCreditos(r.creditos());
        d.setCursoId(r.cursoId());
        d.setProfessorId(professorId);
        d.setObrigatoria(r.obrigatoria());
        return d;
    }

    // ---------- professores e alunos ----------

    public Usuario criarUsuario(Papel papel, UsuarioRequest r) {
        if (papel == Papel.SECRETARIA) {
            throw new RegraNegocioException("Perfil não permitido neste cadastro.");
        }
        if (r.senha() == null || r.senha().length() < 6) {
            throw new RegraNegocioException("Informe uma senha com ao menos 6 caracteres.");
        }
        String login = r.login().trim().toLowerCase();
        if (usuarios.existsByLogin(login)) {
            throw new RegraNegocioException("O login " + login + " já está em uso.");
        }
        Usuario u = new Usuario(r.nome().trim(), login, encoder.encode(r.senha()), papel, cursoDe(papel, r));
        return usuarios.save(u);
    }

    public Usuario atualizarUsuario(Papel papel, String id, UsuarioRequest r) {
        Usuario u = usuarios.findById(id).filter(x -> x.getPapel() == papel)
                .orElseThrow(() -> new NaoEncontradoException(papel == Papel.ALUNO ? "Aluno" : "Professor"));
        String login = r.login().trim().toLowerCase();
        if (!u.getLogin().equals(login) && usuarios.existsByLogin(login)) {
            throw new RegraNegocioException("O login " + login + " já está em uso.");
        }
        u.setNome(r.nome().trim());
        u.setLogin(login);
        u.setCursoId(cursoDe(papel, r));
        if (r.senha() != null && !r.senha().isBlank()) {
            if (r.senha().length() < 6) {
                throw new RegraNegocioException("A senha deve ter ao menos 6 caracteres.");
            }
            u.setSenhaHash(encoder.encode(r.senha()));
        }
        return usuarios.save(u);
    }

    public void excluirUsuario(Papel papel, String id) {
        Usuario u = usuarios.findById(id).filter(x -> x.getPapel() == papel)
                .orElseThrow(() -> new NaoEncontradoException(papel == Papel.ALUNO ? "Aluno" : "Professor"));
        if (papel == Papel.PROFESSOR && !disciplinas.findByProfessorId(id).isEmpty()) {
            throw new RegraNegocioException("O professor ainda ministra disciplinas.");
        }
        if (papel == Papel.ALUNO && !matriculas.findByAlunoId(id).isEmpty()) {
            throw new RegraNegocioException("O aluno possui histórico de matrículas.");
        }
        usuarios.delete(u);
    }

    private String cursoDe(Papel papel, UsuarioRequest r) {
        if (papel != Papel.ALUNO) {
            return null;
        }
        if (r.cursoId() == null || r.cursoId().isBlank()) {
            throw new RegraNegocioException("Informe o curso do aluno.");
        }
        cursos.findById(r.cursoId()).orElseThrow(() -> new NaoEncontradoException("Curso"));
        return r.cursoId();
    }
}
