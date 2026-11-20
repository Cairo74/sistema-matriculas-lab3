# Referência da API

Base: `/api`. Autenticação por sessão (cookie `JSESSIONID`, `HttpOnly`). Corpo e respostas em JSON.
Erros: `{"mensagem": "..."}`.

| Status | Significado |
| :-: | :-- |
| 400 | Validação de entrada (`campo: motivo`) |
| 401 | Sem sessão ou credenciais inválidas |
| 403 | Perfil sem permissão para a rota |
| 404 | Recurso inexistente |
| 409 | Regra de negócio violada (limite, turma lotada, período fechado…) |

## Autenticação

| Método | Rota | Descrição |
| :-: | :-- | :-- |
| POST | `/auth/login` | `{login, senha}` → usuário logado; abre a sessão |
| POST | `/auth/logout` | Encerra a sessão (204) |
| GET | `/auth/me` | Usuário da sessão atual |
| GET | `/health` | Verificação de disponibilidade (pública) |

## Secretaria — perfil `SECRETARIA`

| Método | Rota | Corpo / descrição |
| :-: | :-- | :-- |
| GET, POST | `/secretaria/cursos` | `{nome, creditos}` |
| PUT, DELETE | `/secretaria/cursos/{id}` | Bloqueia exclusão se houver disciplinas ou alunos |
| GET, POST | `/secretaria/disciplinas` | `{codigo, nome, creditos, cursoId, professorId?, obrigatoria}` |
| PUT, DELETE | `/secretaria/disciplinas/{id}` | Bloqueia exclusão se já ofertada |
| GET, POST | `/secretaria/professores` | `{nome, login, senha}` |
| PUT, DELETE | `/secretaria/professores/{id}` | `senha` opcional na edição |
| GET, POST | `/secretaria/alunos` | `{nome, login, senha, cursoId}` |
| PUT, DELETE | `/secretaria/alunos/{id}` | Bloqueia exclusão se houver histórico |
| GET, POST | `/secretaria/semestres` | `{rotulo}` no formato `AAAA.S` (ex.: `2027.1`) |
| POST | `/secretaria/semestres/{rotulo}/curriculo` | `{cursoId, disciplinaIds[]}`; somente semestre `PLANEJADO` |
| POST | `/secretaria/semestres/{rotulo}/abrir` | `PLANEJADO` → `MATRICULAS_ABERTAS` (exige currículo) |
| POST | `/secretaria/semestres/{rotulo}/encerrar` | `MATRICULAS_ABERTAS` → `ENCERRADO`; cancela turmas com < 3 alunos |
| GET | `/secretaria/ofertas?semestre=` | Currículo do semestre com inscritos e situação |

## Aluno — perfil `ALUNO`

| Método | Rota | Descrição |
| :-: | :-- | :-- |
| GET | `/aluno/semestres` | Semestres com matrículas abertas |
| GET | `/aluno/ofertas` | Currículo do curso do aluno nos semestres abertos (inclui `matriculaId` se inscrito) |
| GET | `/aluno/matriculas` | Histórico de matrículas |
| POST | `/aluno/matriculas` | `{ofertaId}` → cria matrícula (201) |
| DELETE | `/aluno/matriculas/{id}` | Cancela a matrícula |
| GET | `/aluno/cobrancas` | Cobranças por semestre |

## Professor — perfil `PROFESSOR`

| Método | Rota | Descrição |
| :-: | :-- | :-- |
| GET | `/professor/ofertas` | Turmas das disciplinas que ministra |
| GET | `/professor/ofertas/{id}/alunos` | Alunos com matrícula ativa na turma |

## Exemplo

```bash
curl -c cookies.txt -H 'Content-Type: application/json' \
  -d '{"login":"ana","senha":"senha123"}' http://localhost:8080/api/auth/login

curl -b cookies.txt -H 'Content-Type: application/json' \
  -d '{"ofertaId":"<id>"}' http://localhost:8080/api/aluno/matriculas
```
