# Meridiano - Sistema de Matrículas

Laboratório 03 de Desenvolvimento e Integração de Aplicações Web (DIAW), Engenharia de Software. É um sistema de matrículas para uma universidade, com back-end em Spring Boot e front-end em React.

A secretaria cadastra cursos, disciplinas, professores e alunos, monta o currículo de cada semestre e abre e fecha o período de matrículas. Cada aluno se matricula em até 4 disciplinas obrigatórias e 2 optativas. Quando o período fecha, disciplina com menos de 3 alunos é cancelada, e cada turma aceita no máximo 60. O sistema de cobranças é avisado a cada matrícula ou cancelamento. Professores consultam os alunos das suas turmas. Todo acesso exige login e senha.

Stack: Java 25, Spring Boot 4, Maven, MongoDB Atlas, React 19 e Vite.

## Casos de uso

```mermaid
flowchart LR
    %% Atores
    SEC([Secretaria])
    ALU([Aluno])
    PRO([Professor])
    COB([Sistema de Cobranças])

    subgraph SIS [Sistema de Matrículas]
        direction TB
        UC1(Autenticar-se)
        UC2(Manter cursos)
        UC3(Manter disciplinas)
        UC4(Manter professores e alunos)
        UC5(Gerar currículo do semestre)
        UC6(Abrir período de matrículas)
        UC7(Encerrar período e<br/>cancelar disciplinas com poucos alunos)
        UC8(Consultar currículo aberto)
        UC9(Matricular-se em disciplina)
        UC10(Cancelar matrícula)
        UC11(Consultar histórico e cobrança)
        UC12(Consultar alunos matriculados)
        UC13(Notificar cobrança)
    end

    SEC --- UC1 & UC2 & UC3 & UC4 & UC5 & UC6 & UC7
    ALU --- UC1 & UC8 & UC9 & UC10 & UC11
    PRO --- UC1 & UC12
    UC9 -. include .-> UC13
    UC10 -. include .-> UC13
    UC7 -. include .-> UC13
    UC13 --- COB
```

## Histórias de usuário

**Autenticação**

- US01. Como usuário (aluno, professor ou secretaria), quero entrar com login e senha para acessar só as funções do meu perfil. Senha errada retorna erro sem dizer qual campo falhou; rota de outro perfil responde 403 e sem sessão responde 401.
- US02. Como usuário, quero encerrar minha sessão para que ninguém use minha conta.

**Secretaria**

- US03. Como secretária, quero cadastrar, editar e excluir cursos (nome e total de créditos). Curso com disciplinas ou alunos não pode ser excluído.
- US04. Como secretária, quero cadastrar disciplinas com código, créditos, curso, professor e tipo (obrigatória ou optativa). O código é único e disciplina já ofertada não pode ser excluída.
- US05. Como secretária, quero cadastrar professores e alunos e definir a senha inicial deles. O login é único, a senha tem no mínimo 6 caracteres e o aluno precisa de um curso.
- US06. Como secretária, quero escolher as disciplinas que serão ofertadas por um curso em um semestre. Só valem disciplinas do curso e só antes da abertura das matrículas.
- US07. Como secretária, quero abrir as matrículas de um semestre para os alunos se inscreverem. Exige currículo gerado.
- US08. Como secretária, quero encerrar o período para confirmar as turmas viáveis e cancelar as demais. Disciplina com menos de 3 alunos é cancelada junto com as matrículas, e a cobrança dos alunos afetados é atualizada.

**Aluno**

- US09. Como aluno, quero ver as disciplinas abertas do meu curso, com professor, créditos e vagas, para decidir onde me matricular.
- US10. Como aluno, quero me matricular em até 4 obrigatórias e 2 optativas por semestre. O sistema bloqueia excesso de disciplinas, turma com 60 inscritos, matrícula duplicada e período fechado.
- US11. Como aluno, quero cancelar uma matrícula enquanto o período estiver aberto, liberando a vaga.
- US12. Como aluno, quero ver meu histórico (ativas, canceladas e disciplinas canceladas) e o valor cobrado por semestre.

**Professor**

- US13. Como professor, quero ver minhas turmas e os alunos matriculados em cada uma. Só aparecem as disciplinas que eu ministro.

**Sistema de cobranças**

- US14. Como sistema de cobranças, quero ser notificado quando as matrículas de um aluno mudarem, para manter a cobrança do semestre correta.

### Regras de negócio

| # | Regra |
| :-: | :-- |
| RN01 | Por semestre, o aluno pode ter até 4 matrículas obrigatórias e até 2 optativas. |
| RN02 | Cada disciplina aceita no máximo 60 alunos. |
| RN03 | Só é possível matricular ou cancelar com o período aberto. |
| RN04 | Ao encerrar o período, disciplina com menos de 3 alunos é cancelada. |
| RN05 | Toda matrícula ou cancelamento notifica o sistema de cobranças. |
| RN06 | O aluno só se matricula em disciplinas do currículo do seu curso. |
| RN07 | Professor só vê os alunos das próprias disciplinas. |
| RN08 | Todo acesso exige login e senha válidos. |

Os limites (4, 2, 60 e 3) e o valor do crédito ficam em `backend/src/main/resources/application.yml`.

## Diagrama de classes

Reflete as classes de `backend/src/main/java/br/edu/matriculas`.

```mermaid
classDiagram
    direction LR

    class Papel {
        <<enumeration>>
        ALUNO
        PROFESSOR
        SECRETARIA
    }
    class Usuario {
        -String id
        -String nome
        -String login
        -String senhaHash
        -Papel papel
        -String cursoId
        -boolean ativo
    }
    class Curso {
        -String id
        -String nome
        -int creditos
    }
    class Disciplina {
        -String id
        -String codigo
        -String nome
        -int creditos
        -String cursoId
        -String professorId
        -boolean obrigatoria
    }
    class Semestre {
        -String rotulo
        -StatusSemestre status
    }
    class Oferta {
        -String id
        -String semestre
        -String cursoId
        -String disciplinaId
        -StatusOferta status
    }
    class Matricula {
        -String id
        -String alunoId
        -String ofertaId
        -String disciplinaId
        -String semestre
        -TipoMatricula tipo
        -StatusMatricula status
        -Instant criadaEm
    }
    class Cobranca {
        -String id
        -String alunoId
        -String semestre
        -int creditos
        -BigDecimal valor
        -Instant atualizadaEm
    }
    class StatusSemestre {
        <<enumeration>>
        PLANEJADO
        MATRICULAS_ABERTAS
        ENCERRADO
    }
    class StatusOferta {
        <<enumeration>>
        ABERTA
        ATIVA
        CANCELADA
    }
    class StatusMatricula {
        <<enumeration>>
        ATIVA
        CANCELADA
        DISCIPLINA_CANCELADA
    }
    class TipoMatricula {
        <<enumeration>>
        OBRIGATORIA
        OPTATIVA
    }

    class MatriculaService {
        +matricular(alunoId, ofertaId) Matricula
        +cancelar(matriculaId, solicitante) Matricula
    }
    class SemestreService {
        +criar(rotulo) Semestre
        +gerarCurriculo(rotulo, cursoId, disciplinaIds) List~Oferta~
        +abrirMatriculas(rotulo) Semestre
        +encerrarMatriculas(rotulo) Semestre
    }
    class CadastroService {
        +criarCurso() Curso
        +criarDisciplina() Disciplina
        +criarUsuario(papel) Usuario
    }
    class CobrancaGateway {
        <<interface>>
        +notificarAlteracao(alunoId, semestre)
    }
    class CobrancaStubService {
        +notificarAlteracao(alunoId, semestre)
    }

    Usuario --> Papel
    Usuario "0..*" --> "0..1" Curso : aluno pertence
    Disciplina "0..*" --> "1" Curso
    Disciplina "0..*" --> "0..1" Usuario : professor
    Oferta "0..*" --> "1" Semestre
    Oferta "0..*" --> "1" Disciplina
    Oferta --> StatusOferta
    Semestre --> StatusSemestre
    Matricula "0..*" --> "1" Usuario : aluno
    Matricula "0..*" --> "1" Oferta
    Matricula --> TipoMatricula
    Matricula --> StatusMatricula
    Cobranca "0..*" --> "1" Usuario : aluno

    MatriculaService ..> Matricula
    MatriculaService ..> CobrancaGateway
    SemestreService ..> Oferta
    SemestreService ..> CobrancaGateway
    CadastroService ..> Curso
    CobrancaStubService ..|> CobrancaGateway
```

O currículo de um semestre é o conjunto de `Oferta`s (disciplina + semestre + curso). A oferta guarda o estado da turma (aberta, ativa ou cancelada) e é a referência das matrículas.

## Arquitetura

```mermaid
flowchart LR
    UI["React + Vite<br/>(páginas, componentes)"] -- "fetch /api (cookie de sessão)" --> C
    subgraph BE [Spring Boot 4 · Java 25]
        direction LR
        SEC["Spring Security<br/>login + perfis"] --> C["Controllers REST"]
        C --> S["Services<br/>(regras de negócio)"]
        S --> R["Repositories<br/>(Spring Data MongoDB)"]
        S -. notifica .-> G["CobrancaGateway"]
    end
    R --> DB[("MongoDB Atlas")]
    G --> COB["Sistema de cobranças<br/>(stub)"]
```

- `controller`: recebe a requisição HTTP, valida a entrada e devolve DTOs.
- `service`: concentra as regras (limites, vagas, período, fechamento de turmas).
- `repository`: acesso ao MongoDB via Spring Data.
- `security`: `UserDetailsService` sobre o MongoDB, BCrypt e autorização por perfil em `/api/secretaria`, `/api/professor` e `/api/aluno`.
- `CobrancaGateway`: interface para o sistema de cobranças. A implementação atual (`CobrancaStubService`) só recalcula e grava a cobrança no próprio banco.

Fluxo de uma matrícula:

```mermaid
sequenceDiagram
    actor A as Aluno
    participant UI as React
    participant C as AlunoController
    participant S as MatriculaService
    participant DB as MongoDB
    participant G as CobrancaGateway

    A->>UI: clica em "Matricular"
    UI->>C: POST /api/aluno/matriculas {ofertaId}
    C->>S: matricular(alunoId, ofertaId)
    S->>DB: busca aluno, oferta e semestre
    S->>S: período aberto? curso correto? sem duplicidade?
    S->>DB: conta matrículas ativas do aluno (tipo) e da oferta
    alt limite do tipo ou 60 vagas excedidos
        S-->>C: RegraNegocioException
        C-->>UI: 409 {mensagem}
        UI-->>A: aviso de erro
    else dentro das regras
        S->>DB: salva Matricula(ATIVA)
        S->>G: notificarAlteracao(alunoId, semestre)
        S-->>C: Matricula
        C-->>UI: 201 MatriculaResponse
        UI-->>A: confirmação e contadores atualizados
    end
```

## Como rodar

Precisa de JDK 25, Node.js 20+ e um MongoDB (cluster no Atlas ou o `docker-compose.yml` do repositório).

```bash
git clone https://github.com/Cairo74/sistema-matriculas-lab3.git
cd sistema-matriculas-lab3

# MongoDB local, se não for usar o Atlas
docker compose up -d

# back-end em http://localhost:8080
cd backend
./mvnw spring-boot:run        # no Windows: mvnw.cmd spring-boot:run

# front-end em http://localhost:5173 (outro terminal)
cd frontend
npm install
npm run dev
```

Para usar o Atlas, defina a URI antes de subir o back-end e não coloque ela no repositório:

```bash
export MONGODB_URI='mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/matriculas'
```

No Atlas, libere seu IP em Network Access.

Variáveis do back-end:

| Variável | Para que serve | Padrão |
| :-- | :-- | :-- |
| `MONGODB_URI` | conexão com o MongoDB | `mongodb://127.0.0.1:27017/matriculas` |
| `SERVER_PORT` | porta da API | `8080` |
| `CORS_ORIGENS` | origens permitidas, se o front não usar o proxy | `http://localhost:5173` |
| `SEED_ENABLED` | cria dados de exemplo se o banco estiver vazio | `true` |
| `SEED_PASSWORD` | senha dos usuários de exemplo | `senha123` |

No front, `BACKEND_URL` muda o destino do proxy do Vite e `VITE_API_URL` define a URL da API em produção (veja `frontend/.env.example`).

### Usuários de exemplo

Criados em banco vazio, todos com a senha `senha123`.

| Perfil | Login |
| :-- | :-- |
| Secretaria | `secretaria` |
| Professores | `helena`, `ricardo`, `beatriz` |
| Alunos de Engenharia de Software | `ana`, `bruno`, `carla`, `diego` |
| Aluna de Sistemas de Informação | `elisa` |

O semestre `2026.2` já vem com currículo e matrículas abertas.

### Build

```bash
cd frontend && npm run build           # gera frontend/dist
cd ../backend && ./mvnw clean package  # gera o jar em target/
```

Em produção, sirva `frontend/dist` atrás de um servidor que encaminhe `/api` ao back-end. Se front e API ficarem em origens diferentes, o cookie de sessão precisa de `SameSite=None; Secure` (HTTPS).

## API

Erros vêm como `{"mensagem": "..."}`. A lista completa de rotas está em [docs/api.md](docs/api.md).

## Testes

```bash
cd backend
./mvnw test
```

Os testes (JUnit 5 e Mockito) cobrem os limites de obrigatórias e optativas, turma lotada, período fechado, matrícula duplicada, curso incorreto, cancelamento e o fechamento do período.

## Limitações

- A sessão é por cookie `HttpOnly` em vez de JWT.
- O sistema de cobranças é simulado.
- A checagem das 60 vagas não é atômica: duas matrículas simultâneas na última vaga podem passar do limite. Em produção seria preciso uma operação condicional ou transação no MongoDB.
- Não há recuperação de senha; a secretaria define as senhas.

## Autor

Cairo74 - [github.com/Cairo74](https://github.com/Cairo74)

Licença MIT, veja [LICENSE](LICENSE).
