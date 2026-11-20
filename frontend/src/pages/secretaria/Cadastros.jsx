import { GraduationCap, LibraryBig, Users, UsersRound } from 'lucide-react'
import { Badge } from '../../components/ui.jsx'
import CrudPage from './CrudPage.jsx'

const paraOpcoes = (lista, rotulo = (x) => x.nome) => lista.map((x) => ({ valor: x.id, rotulo: rotulo(x) }))

export function Cursos() {
  return (
    <CrudPage
      eyebrow="Cadastros"
      titulo="Cursos"
      singular="Curso"
      descricao="Cada curso tem um nome, um total de créditos e é composto por diversas disciplinas."
      icone={GraduationCap}
      endpoint="/secretaria/cursos"
      colunas={[
        { rotulo: 'Nome', chave: 'nome' },
        { rotulo: 'Créditos', chave: 'creditos' },
        { rotulo: 'Disciplinas', chave: 'totalDisciplinas' },
        { rotulo: 'Alunos', chave: 'totalAlunos' },
      ]}
      campos={[
        { nome: 'nome', rotulo: 'Nome do curso', obrigatorio: true, cheio: true },
        { nome: 'creditos', rotulo: 'Total de créditos', tipo: 'number', min: 1, obrigatorio: true },
      ]}
      inicial={{ nome: '', creditos: 200 }}
      paraFormulario={(c) => ({ nome: c.nome, creditos: c.creditos })}
      vazio={{ titulo: 'Nenhum curso cadastrado', texto: 'Cadastre o primeiro curso para começar a montar o currículo.' }}
    />
  )
}

export function Disciplinas() {
  return (
    <CrudPage
      eyebrow="Cadastros"
      titulo="Disciplinas"
      singular="Disciplina"
      descricao="Disciplinas obrigatórias são a 1ª opção de matrícula (até 4); as optativas são alternativas (até 2)."
      icone={LibraryBig}
      endpoint="/secretaria/disciplinas"
      opcoesEndpoints={{ cursos: '/secretaria/cursos', professores: '/secretaria/professores' }}
      colunas={[
        { rotulo: 'Código', chave: 'codigo', mono: true },
        { rotulo: 'Disciplina', chave: 'nome' },
        { rotulo: 'Curso', chave: 'cursoNome' },
        { rotulo: 'Professor', render: (d) => d.professorNome ?? '—' },
        { rotulo: 'Créd.', chave: 'creditos' },
        {
          rotulo: 'Tipo',
          render: (d) => <Badge tom={d.obrigatoria ? '' : 'neutral'}>{d.obrigatoria ? 'Obrigatória' : 'Optativa'}</Badge>,
        },
      ]}
      campos={(o) => [
        { nome: 'codigo', rotulo: 'Código', obrigatorio: true, dica: 'Ex.: ES101' },
        { nome: 'creditos', rotulo: 'Créditos', tipo: 'number', min: 1, obrigatorio: true },
        { nome: 'nome', rotulo: 'Nome da disciplina', obrigatorio: true, cheio: true },
        { nome: 'cursoId', rotulo: 'Curso', tipo: 'select', obrigatorio: true, opcoes: paraOpcoes(o.cursos) },
        {
          nome: 'professorId',
          rotulo: 'Professor',
          tipo: 'select',
          vazio: 'Sem professor',
          opcoes: paraOpcoes(o.professores),
        },
        { nome: 'obrigatoria', rotulo: 'Disciplina obrigatória', tipo: 'checkbox', cheio: true },
      ]}
      inicial={{ codigo: '', nome: '', creditos: 4, cursoId: '', professorId: '', obrigatoria: true }}
      paraFormulario={(d) => ({
        codigo: d.codigo,
        nome: d.nome,
        creditos: d.creditos,
        cursoId: d.cursoId,
        professorId: d.professorId ?? '',
        obrigatoria: d.obrigatoria,
      })}
      vazio={{ titulo: 'Nenhuma disciplina cadastrada', texto: 'Cadastre ao menos um curso e depois as suas disciplinas.' }}
    />
  )
}

export function Professores() {
  return (
    <CrudPage
      eyebrow="Pessoas"
      titulo="Professores"
      singular="Professor"
      descricao="Professores acessam o sistema para consultar os alunos matriculados em suas disciplinas."
      icone={UsersRound}
      endpoint="/secretaria/professores"
      colunas={[
        { rotulo: 'Nome', chave: 'nome' },
        { rotulo: 'Login', chave: 'login', mono: true },
      ]}
      campos={[
        { nome: 'nome', rotulo: 'Nome completo', obrigatorio: true, cheio: true },
        { nome: 'login', rotulo: 'Login', obrigatorio: true },
        {
          nome: 'senha',
          rotulo: 'Senha',
          tipo: 'password',
          obrigatorio: true,
          somenteNaCriacao: true,
          dica: 'Mínimo de 6 caracteres. Ao editar, deixe em branco para manter.',
        },
      ]}
      inicial={{ nome: '', login: '', senha: '' }}
      paraFormulario={(p) => ({ nome: p.nome, login: p.login, senha: '' })}
      vazio={{ titulo: 'Nenhum professor cadastrado', texto: 'Cadastre professores para vinculá-los às disciplinas.' }}
    />
  )
}

export function Alunos() {
  return (
    <CrudPage
      eyebrow="Pessoas"
      titulo="Alunos"
      singular="Aluno"
      descricao="Cada aluno pertence a um curso e só visualiza o currículo dele."
      icone={Users}
      endpoint="/secretaria/alunos"
      opcoesEndpoints={{ cursos: '/secretaria/cursos' }}
      colunas={[
        { rotulo: 'Nome', chave: 'nome' },
        { rotulo: 'Login', chave: 'login', mono: true },
        { rotulo: 'Curso', chave: 'cursoNome' },
      ]}
      campos={(o) => [
        { nome: 'nome', rotulo: 'Nome completo', obrigatorio: true, cheio: true },
        { nome: 'login', rotulo: 'Login', obrigatorio: true },
        { nome: 'cursoId', rotulo: 'Curso', tipo: 'select', obrigatorio: true, opcoes: paraOpcoes(o.cursos) },
        {
          nome: 'senha',
          rotulo: 'Senha',
          tipo: 'password',
          obrigatorio: true,
          somenteNaCriacao: true,
          cheio: true,
          dica: 'Mínimo de 6 caracteres. Ao editar, deixe em branco para manter.',
        },
      ]}
      inicial={{ nome: '', login: '', cursoId: '', senha: '' }}
      paraFormulario={(a) => ({ nome: a.nome, login: a.login, cursoId: a.cursoId ?? '', senha: '' })}
      vazio={{ titulo: 'Nenhum aluno cadastrado', texto: 'Cadastre alunos e vincule-os a um curso.' }}
    />
  )
}
