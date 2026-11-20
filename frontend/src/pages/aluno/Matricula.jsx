import { useState } from 'react'
import { motion } from 'motion/react'
import { CalendarX2, Check, Lock, Plus, User } from 'lucide-react'
import { api } from '../../api.js'
import { useCarregar } from '../../hooks.js'
import { useToast } from '../../components/Toast.jsx'
import { Badge, Button, CountUp, Empty, PageHeader, Progress, Skeletons, Spot } from '../../components/ui.jsx'

const LIMITE = { obrigatorias: 4, optativas: 2 }

function Oferta({ oferta, indice, bloqueada, motivo, onMatricular, onCancelar, ocupada }) {
  const d = oferta.disciplina
  const inscrito = Boolean(oferta.matriculaId)
  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: indice * 0.05, duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
    >
      <Spot className={`offer ${inscrito ? 'enrolled' : ''}`}>
        <div className="offer-top">
          <div>
            <span className="mono code">{d.codigo}</span>
            <h3>{d.nome}</h3>
          </div>
          {inscrito ? <Badge tom="ok">Matriculado</Badge> : null}
        </div>

        <div className="meta">
          <span>
            <User size={13} style={{ verticalAlign: -2 }} /> <b>{d.professorNome ?? 'A definir'}</b>
          </span>
          <span>
            <b>{d.creditos}</b> créditos
          </span>
          <span className="mono">{oferta.semestre}</span>
        </div>

        <div className="vagas">
          <div>
            <span>
              <b style={{ color: 'var(--ink-2)' }}>{oferta.inscritos}</b> de {oferta.vagas} vagas
            </span>
            <span>
              {oferta.inscritos >= oferta.minimo ? 'Turma confirmada' : `Mín. ${oferta.minimo} para abrir`}
            </span>
          </div>
          <Progress valor={oferta.inscritos} max={oferta.vagas} />
        </div>

        {inscrito ? (
          <Button className="btn-danger" onClick={() => onCancelar(oferta)} carregando={ocupada}>
            Cancelar matrícula
          </Button>
        ) : oferta.lotada ? (
          <Button disabled>
            <Lock size={15} /> Inscrições encerradas
          </Button>
        ) : (
          <Button
            className="btn-primary"
            onClick={() => onMatricular(oferta)}
            carregando={ocupada}
            disabled={bloqueada}
            title={motivo}
          >
            <Plus size={16} /> {bloqueada ? motivo : 'Matricular'}
          </Button>
        )}
      </Spot>
    </motion.div>
  )
}

export default function Matricula() {
  const toast = useToast()
  const { dados, carregando, recarregar } = useCarregar(['/aluno/ofertas', '/aluno/matriculas'])
  const [ocupada, setOcupada] = useState(null)

  const ofertas = dados?.[0] ?? []
  const matriculas = dados?.[1] ?? []

  const semestres = new Set(ofertas.map((o) => o.semestre))
  const ativas = matriculas.filter((m) => m.status === 'ATIVA' && semestres.has(m.semestre))
  const obrigatorias = ativas.filter((m) => m.tipo === 'OBRIGATORIA').length
  const optativas = ativas.filter((m) => m.tipo === 'OPTATIVA').length
  const creditos = ativas.reduce((soma, m) => soma + m.creditos, 0)

  async function executar(oferta, acao, sucesso) {
    setOcupada(oferta.id)
    try {
      await acao()
      toast.ok(sucesso)
      await recarregar()
    } catch (e) {
      toast.erro(e.message)
    } finally {
      setOcupada(null)
    }
  }

  const matricular = (o) =>
    executar(o, () => api.post('/aluno/matriculas', { ofertaId: o.id }), `Matrícula em ${o.disciplina.nome} confirmada.`)
  const cancelar = (o) =>
    executar(o, () => api.del(`/aluno/matriculas/${o.matriculaId}`), `Matrícula em ${o.disciplina.nome} cancelada.`)

  const bloqueio = (o) => {
    const obrig = o.disciplina.obrigatoria
    if (obrig && obrigatorias >= LIMITE.obrigatorias) return 'Limite de obrigatórias'
    if (!obrig && optativas >= LIMITE.optativas) return 'Limite de optativas'
    return null
  }

  const grupo = (obrigatoria) => ofertas.filter((o) => o.disciplina.obrigatoria === obrigatoria)

  return (
    <>
      <PageHeader
        eyebrow="Período de matrículas"
        titulo="Monte o seu semestre"
        descricao="Escolha até 4 disciplinas obrigatórias e 2 optativas. Você pode cancelar enquanto o período estiver aberto."
      />

      {carregando ? (
        <Skeletons />
      ) : ofertas.length === 0 ? (
        <Empty icone={CalendarX2} titulo="Nenhum período de matrículas aberto">
          Assim que a secretaria abrir as matrículas do próximo semestre, o currículo do seu curso aparece aqui.
        </Empty>
      ) : (
        <>
          <div className="stats">
            <Spot className="stat">
              <span>Obrigatórias</span>
              <strong>
                <CountUp value={obrigatorias} />
                <small>/ {LIMITE.obrigatorias}</small>
              </strong>
              <Progress valor={obrigatorias} max={LIMITE.obrigatorias} />
            </Spot>
            <Spot className="stat">
              <span>Optativas</span>
              <strong>
                <CountUp value={optativas} />
                <small>/ {LIMITE.optativas}</small>
              </strong>
              <Progress valor={optativas} max={LIMITE.optativas} />
            </Spot>
            <Spot className="stat">
              <span>Créditos no semestre</span>
              <strong>
                <CountUp value={creditos} />
                <small>créditos</small>
              </strong>
              <span style={{ fontSize: 12 }}>
                <Check size={12} style={{ verticalAlign: -1 }} /> Cobrança atualizada a cada alteração
              </span>
            </Spot>
          </div>

          {[
            ['Obrigatórias', true],
            ['Optativas', false],
          ].map(([titulo, obrig]) =>
            grupo(obrig).length ? (
              <section key={titulo}>
                <h2 className="section-title">
                  {titulo} <span>{grupo(obrig).length} disciplinas</span>
                </h2>
                <div className="grid-cards">
                  {grupo(obrig).map((o, i) => {
                    const motivo = bloqueio(o)
                    return (
                      <Oferta
                        key={o.id}
                        oferta={o}
                        indice={i}
                        bloqueada={Boolean(motivo)}
                        motivo={motivo}
                        ocupada={ocupada === o.id}
                        onMatricular={matricular}
                        onCancelar={cancelar}
                      />
                    )
                  })}
                </div>
              </section>
            ) : null,
          )}
        </>
      )}
    </>
  )
}
