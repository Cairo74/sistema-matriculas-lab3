package br.edu.matriculas.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.edu.matriculas.model.Curso;
import br.edu.matriculas.model.Disciplina;
import br.edu.matriculas.model.Oferta;
import br.edu.matriculas.model.Papel;
import br.edu.matriculas.model.Semestre;
import br.edu.matriculas.model.StatusSemestre;
import br.edu.matriculas.model.Usuario;
import br.edu.matriculas.repository.CursoRepository;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.SemestreRepository;
import br.edu.matriculas.repository.UsuarioRepository;

/**
 * Popula um banco vazio com dados de demonstração. Executa uma única vez
 * (somente se não houver usuários) e pode ser desligado com {@code matriculas.seed.enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "matriculas.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarios;
    private final CursoRepository cursos;
    private final DisciplinaRepository disciplinas;
    private final SemestreRepository semestres;
    private final OfertaRepository ofertas;
    private final PasswordEncoder encoder;
    private final String senha;

    public DataSeeder(UsuarioRepository usuarios, CursoRepository cursos, DisciplinaRepository disciplinas,
            SemestreRepository semestres, OfertaRepository ofertas, PasswordEncoder encoder,
            @Value("${matriculas.seed.senha}") String senha) {
        this.usuarios = usuarios;
        this.cursos = cursos;
        this.disciplinas = disciplinas;
        this.semestres = semestres;
        this.ofertas = ofertas;
        this.encoder = encoder;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        String hash = encoder.encode(senha);

        Curso es = cursos.save(new Curso("Engenharia de Software", 240));
        Curso si = cursos.save(new Curso("Sistemas de Informação", 220));

        usuarios.save(new Usuario("Secretaria Acadêmica", "secretaria", hash, Papel.SECRETARIA, null));
        Usuario helena = usuarios.save(new Usuario("Helena Prado", "helena", hash, Papel.PROFESSOR, null));
        Usuario ricardo = usuarios.save(new Usuario("Ricardo Matos", "ricardo", hash, Papel.PROFESSOR, null));
        Usuario beatriz = usuarios.save(new Usuario("Beatriz Lima", "beatriz", hash, Papel.PROFESSOR, null));

        usuarios.saveAll(List.of(
                new Usuario("Ana Souza", "ana", hash, Papel.ALUNO, es.getId()),
                new Usuario("Bruno Carvalho", "bruno", hash, Papel.ALUNO, es.getId()),
                new Usuario("Carla Mendes", "carla", hash, Papel.ALUNO, es.getId()),
                new Usuario("Diego Ferreira", "diego", hash, Papel.ALUNO, es.getId()),
                new Usuario("Elisa Ramos", "elisa", hash, Papel.ALUNO, si.getId())));

        List<Disciplina> grade = List.of(
                disciplina("ES101", "Algoritmos e Estruturas de Dados", 6, es, helena, true),
                disciplina("ES102", "Engenharia de Requisitos", 4, es, ricardo, true),
                disciplina("ES103", "Banco de Dados", 4, es, beatriz, true),
                disciplina("ES104", "Desenvolvimento Web", 4, es, helena, true),
                disciplina("ES105", "Arquitetura de Software", 4, es, ricardo, true),
                disciplina("ES201", "Computação em Nuvem", 2, es, beatriz, false),
                disciplina("ES202", "Interação Humano-Computador", 2, es, ricardo, false),
                disciplina("ES203", "Aprendizado de Máquina", 4, es, helena, false),
                disciplina("ES204", "Segurança da Informação", 2, es, beatriz, false));
        disciplinas.saveAll(grade);

        Semestre semestre = new Semestre("2026.2");
        semestre.setStatus(StatusSemestre.MATRICULAS_ABERTAS);
        semestres.save(semestre);
        grade.forEach(d -> ofertas.save(new Oferta("2026.2", es.getId(), d.getId())));

        log.info("Dados de demonstração criados (logins: secretaria, helena, ana, ...).");
    }

    private Disciplina disciplina(String codigo, String nome, int creditos, Curso curso, Usuario professor,
            boolean obrigatoria) {
        Disciplina d = new Disciplina();
        d.setCodigo(codigo);
        d.setNome(nome);
        d.setCreditos(creditos);
        d.setCursoId(curso.getId());
        d.setProfessorId(professor.getId());
        d.setObrigatoria(obrigatoria);
        return d;
    }
}
