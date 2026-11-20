import { useEffect, useState } from 'react'
import { Presentation, UserRound } from 'lucide-react'
import { api } from '../../api.js'
import { useCarregar } from '../../hooks.js'
import { useToast } from '../../components/Toast.jsx'
import {
  Badge,
  Empty,
  PageHeader,
  Progress,
  STATUS_OFERTA,
  Skeletons,
  Spot,
  dataCurta,
} from '../../components/ui.jsx'

export default function Turmas() {
  const toast = useToast()
  const { dados, carregando } = useCarregar(['/professor/ofertas'])
  const ofertas = dados?.[0] ?? []
  const [selecionada, setSelecionada] = useState(null)
  const [alunos, setAlunos] = useState(null)

  useEffect(() => {
    if (!selecionada && ofertas.length) setSelecionada(ofertas[0].id)
  }, [ofertas, selecionada])

  useEffect(() => {
    if (!selecionada) return
    setAlunos(null)
    api
      .get(`/professor/ofertas/${selecionada}/alunos`)
      .then(setAlunos)
      .catch((e) => toast.erro(e.message))
  }, [selecionada, toast])

  const atual = ofertas.find((o) => o.id === selecionada)

  return (
    <>
      <PageHeader
        eyebrow="Professor"
        titulo="Minhas turmas"
        descricao="Acompanhe quem está matriculado em cada disciplina que você ministra."
      />
      {carregando ? (
        <Skeletons n={2} />
      ) : ofertas.length === 0 ? (
        <Empty icone={Presentation} titulo="Você ainda não ministra disciplinas ofertadas">
          Quando a secretaria incluir uma disciplina sua no currículo de um semestre, ela aparece aqui.
        </Empty>
      ) : (
        <div className="split">
          <div className="list">
            {ofertas.map((o) => {
              const [tom, rotulo] = STATUS_OFERTA[o.status]
              return (
                <button
                  key={o.id}
                  className="list-item"
                  aria-current={o.id === selecionada}
                  onClick={() => setSelecionada(o.id)}
                >
                  <div className="row">
                    <span className="mono">
                      {o.disciplina.codigo} · {o.semestre}
                    </span>
                    <Badge tom={tom}>{rotulo}</Badge>
                  </div>
                  <strong>{o.disciplina.nome}</strong>
                  <Progress valor={o.inscritos} max={o.vagas} />
                  <div className="row">
                    <span>
                      {o.inscritos} de {o.vagas} vagas
                    </span>
                  </div>
                </button>
              )
            })}
          </div>

          <Spot className="panel">
            <h2>{atual?.disciplina.nome}</h2>
            <p className="sub">
              Alunos com matrícula ativa{atual ? ` em ${atual.semestre}` : ''}.
            </p>
            {alunos === null ? (
              <div className="skeleton" />
            ) : alunos.length === 0 ? (
              <Empty icone={UserRound} titulo="Nenhum aluno matriculado" />
            ) : (
              <div className="table-wrap" style={{ boxShadow: 'none' }}>
                <table>
                  <thead>
                    <tr>
                      <th>Aluno</th>
                      <th>Login</th>
                      <th>Matriculado em</th>
                    </tr>
                  </thead>
                  <tbody>
                    {alunos.map((a) => (
                      <tr key={a.id}>
                        <td>{a.nome}</td>
                        <td className="mono">{a.login}</td>
                        <td>{dataCurta(a.matriculadoEm)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Spot>
        </div>
      )}
    </>
  )
}
