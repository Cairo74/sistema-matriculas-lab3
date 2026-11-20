package br.edu.matriculas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Usuário autenticável do sistema (aluno, professor ou secretaria).
 * O campo {@code cursoId} só é preenchido para alunos.
 */
@Document("usuarios")
public class Usuario {

    @Id
    private String id;
    private String nome;
    @Indexed(unique = true)
    private String login;
    private String senhaHash;
    private Papel papel;
    private String cursoId;
    private boolean ativo = true;

    public Usuario() {
    }

    public Usuario(String nome, String login, String senhaHash, Papel papel, String cursoId) {
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.papel = papel;
        this.cursoId = cursoId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel; }
    public String getCursoId() { return cursoId; }
    public void setCursoId(String cursoId) { this.cursoId = cursoId; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
