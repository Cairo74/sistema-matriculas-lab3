import { Receipt } from 'lucide-react'
import { useCarregar } from '../../hooks.js'
import { Empty, PageHeader, Skeletons, Spot, brl, dataCurta } from '../../components/ui.jsx'

export default function Cobranca() {
  const { dados, carregando } = useCarregar(['/aluno/cobrancas'])
  const cobrancas = dados?.[0] ?? []

  return (
    <>
      <PageHeader
        eyebrow="Financeiro"
        titulo="Cobrança por semestre"
        descricao="O sistema de matrículas notifica o sistema de cobranças a cada matrícula ou cancelamento. Os valores abaixo refletem as disciplinas ativas."
      />
      {carregando ? (
        <Skeletons n={2} />
      ) : cobrancas.length === 0 ? (
        <Empty icone={Receipt} titulo="Sem cobranças geradas">
          A cobrança do semestre é criada assim que você se matricula na primeira disciplina.
        </Empty>
      ) : (
        <div className="grid-cards">
          {cobrancas.map((c) => (
            <Spot key={c.semestre} className="stat">
              <span>
                Semestre <b className="mono">{c.semestre}</b>
              </span>
              <strong>{brl(c.valor)}</strong>
              <span style={{ fontSize: 13 }}>
                {c.creditos} créditos · atualizado em {dataCurta(c.atualizadaEm)}
              </span>
            </Spot>
          ))}
        </div>
      )}
    </>
  )
}
