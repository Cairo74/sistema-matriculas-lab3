import { ScrollText } from 'lucide-react'
import { useCarregar } from '../../hooks.js'
import { Badge, Empty, PageHeader, Skeletons, STATUS_MATRICULA, dataCurta } from '../../components/ui.jsx'

export default function Historico() {
  const { dados, carregando } = useCarregar(['/aluno/matriculas'])
  const matriculas = dados?.[0] ?? []

  return (
    <>
      <PageHeader
        eyebrow="Histórico"
        titulo="Minhas matrículas"
        descricao="Todas as inscrições feitas, inclusive as canceladas e as disciplinas que não atingiram o mínimo de alunos."
      />
      {carregando ? (
        <Skeletons n={1} />
      ) : matriculas.length === 0 ? (
        <Empty icone={ScrollText} titulo="Nenhuma matrícula ainda">
          Quando você se matricular em uma disciplina, ela aparece aqui.
        </Empty>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Semestre</th>
                <th>Disciplina</th>
                <th>Tipo</th>
                <th>Créditos</th>
                <th>Data</th>
                <th>Situação</th>
              </tr>
            </thead>
            <tbody>
              {matriculas.map((m) => {
                const [tom, rotulo] = STATUS_MATRICULA[m.status]
                return (
                  <tr key={m.id}>
                    <td className="mono">{m.semestre}</td>
                    <td>
                      <span className="mono" style={{ color: 'var(--blue-600)' }}>
                        {m.disciplinaCodigo}
                      </span>{' '}
                      {m.disciplinaNome}
                    </td>
                    <td>{m.tipo === 'OBRIGATORIA' ? 'Obrigatória' : 'Optativa'}</td>
                    <td>{m.creditos}</td>
                    <td>{dataCurta(m.criadaEm)}</td>
                    <td>
                      <Badge tom={tom}>{rotulo}</Badge>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
