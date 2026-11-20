import { useEffect, useState } from 'react'
import { CalendarRange, ListChecks, Play, Plus, Square } from 'lucide-react'
import { api } from '../../api.js'
import { useCarregar } from '../../hooks.js'
import { useToast } from '../../components/Toast.jsx'
import {
  Badge,
  Button,
  Empty,
  Modal,
  PageHeader,
  Progress,
  STATUS_OFERTA,
  STATUS_SEMESTRE,
  Skeletons,
  Spot,
} from '../../components/ui.jsx'

export default function Semestres() {
  const toast = useToast()
  const { dados, carregando, recarregar } = useCarregar(['/secretaria/semestres', '/secretaria/cursos', '/secretaria/disciplinas'])
  const semestres = dados?.[0] ?? []
  const cursos = dados?.[1] ?? []
  const disciplinas = dados?.[2] ?? []

  const [selecionado, setSelecionado] = useState(null)
  const [ofertas, setOfertas] = useState(null)
  const [novo, setNovo] = useState('')
  const [erroNovo, setErroNovo] = useState('')
  const [ocupado, setOcupado] = useState(false)
  const [curriculo, setCurriculo] = useState(null) // { cursoId, ids:Set }

  useEffect(() => {
    if (!selecionado && semestres.length) setSelecionado(semestres[0].rotulo)
  }, [semestres, selecionado])

  async function carregarOfertas(rotulo) {
    try {
      setOfertas(await api.get(`/secretaria/ofertas?semestre=${encodeURIComponent(rotulo)}`))
    } catch (e) {
      toast.erro(e.message)
    }
  }

  useEffect(() => {
    if (selecionado) {
      setOfertas(null)
      carregarOfertas(selecionado)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selecionado])

  const atual = semestres.find((s) => s.rotulo === selecionado)

  async function acao(fn, sucesso) {
    setOcupado(true)
    try {
      await fn()
      toast.ok(sucesso)
      await recarregar()
      if (selecionado) await carregarOfertas(selecionado)
    } catch (e) {
      toast.erro(e.message)
    } finally {
      setOcupado(false)
    }
  }

  async function criar(e) {
    e.preventDefault()
    setErroNovo('')
    try {
      const s = await api.post('/secretaria/semestres', { rotulo: novo.trim() })
      toast.ok(`Semestre ${s.rotulo} criado.`)
      setNovo('')
      setSelecionado(s.rotulo)
      await recarregar()
    } catch (err) {
      setErroNovo(err.message)
    }
  }

  const encerrar = () => {
    if (
      !window.confirm(
        `Encerrar as matrículas de ${selecionado}? Disciplinas com menos de 3 alunos serão canceladas e não poderão ser desfeitas.`,
      )
    )
      return
    acao(() => api.post(`/secretaria/semestres/${selecionado}/encerrar`), `Período de ${selecionado} encerrado.`)
  }

  const disciplinasDoCurso = disciplinas.filter((d) => d.cursoId === curriculo?.cursoId)

  function alternar(id) {
    setCurriculo((c) => {
      const ids = new Set(c.ids)
      if (ids.has(id)) ids.delete(id)
      else ids.add(id)
      return { ...c, ids }
    })
  }

  async function gerar(e) {
    e.preventDefault()
    await acao(
      () => api.post(`/secretaria/semestres/${selecionado}/curriculo`, { cursoId: curriculo.cursoId, disciplinaIds: [...curriculo.ids] }),
      'Currículo atualizado.',
    )
    setCurriculo(null)
  }

  return (
    <>
      <PageHeader
        eyebrow="Secretaria"
        titulo="Semestres e currículo"
        descricao="Gere o currículo de cada semestre, abra o período de matrículas e encerre-o para confirmar ou cancelar as disciplinas."
      >
        <form onSubmit={criar} style={{ display: 'flex', gap: 8, alignItems: 'start' }}>
          <div className="field">
            <input
              className="input"
              style={{ width: 150 }}
              placeholder="2027.1"
              aria-label="Rótulo do novo semestre"
              value={novo}
              onChange={(e) => setNovo(e.target.value)}
              required
            />
          </div>
          <Button type="submit" className="btn-primary">
            <Plus size={16} /> Semestre
          </Button>
        </form>
      </PageHeader>
      {erroNovo ? (
        <p className="error-text" role="alert" style={{ marginBottom: 16 }}>
          {erroNovo}
        </p>
      ) : null}

      {carregando ? (
        <Skeletons n={2} />
      ) : semestres.length === 0 ? (
        <Empty icone={CalendarRange} titulo="Nenhum semestre criado">
          Crie um semestre (ex.: 2027.1), gere o currículo e abra as matrículas.
        </Empty>
      ) : (
        <div className="split">
          <div className="list">
            {semestres.map((s) => {
              const [tom, rotulo] = STATUS_SEMESTRE[s.status]
              return (
                <button key={s.rotulo} className="list-item" aria-current={s.rotulo === selecionado} onClick={() => setSelecionado(s.rotulo)}>
                  <div className="row">
                    <strong className="mono" style={{ fontSize: 17, color: 'var(--ink)' }}>
                      {s.rotulo}
                    </strong>
                    <Badge tom={tom}>{rotulo}</Badge>
                  </div>
                  <div className="row">
                    <span>{s.totalOfertas} disciplinas no currículo</span>
                  </div>
                </button>
              )
            })}
          </div>

          <Spot className="panel">
            <h2>Semestre {atual?.rotulo}</h2>
            <p className="sub">
              {atual?.status === 'PLANEJADO' && 'Monte o currículo e, quando estiver pronto, abra as matrículas.'}
              {atual?.status === 'MATRICULAS_ABERTAS' && 'Alunos já podem se matricular. Encerre o período para fechar as turmas.'}
              {atual?.status === 'ENCERRADO' && 'Período encerrado. As turmas abaixo mostram o resultado final.'}
            </p>

            {ofertas === null ? (
              <div className="skeleton" />
            ) : ofertas.length === 0 ? (
              <Empty icone={ListChecks} titulo="Currículo vazio">
                Escolha um curso e as disciplinas que serão ofertadas neste semestre.
              </Empty>
            ) : (
              <div className="table-wrap" style={{ boxShadow: 'none' }}>
                <table>
                  <thead>
                    <tr>
                      <th>Disciplina</th>
                      <th>Curso</th>
                      <th style={{ width: 150 }}>Inscritos</th>
                      <th>Situação</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ofertas.map((o) => {
                      const [tom, rotulo] = STATUS_OFERTA[o.status]
                      return (
                        <tr key={o.id}>
                          <td>
                            <span className="mono" style={{ color: 'var(--blue-600)' }}>
                              {o.disciplina.codigo}
                            </span>{' '}
                            {o.disciplina.nome}
                          </td>
                          <td>{o.disciplina.cursoNome}</td>
                          <td>
                            <div style={{ display: 'grid', gap: 4 }}>
                              <span style={{ fontSize: 12 }}>
                                {o.inscritos} / {o.vagas}
                              </span>
                              <Progress valor={o.inscritos} max={o.vagas} />
                            </div>
                          </td>
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

            <div className="actions-row">
              {atual?.status === 'PLANEJADO' ? (
                <>
                  <Button onClick={() => setCurriculo({ cursoId: cursos[0]?.id ?? '', ids: new Set() })} disabled={!cursos.length}>
                    <ListChecks size={16} /> Gerar currículo
                  </Button>
                  <Button
                    className="btn-primary"
                    carregando={ocupado}
                    onClick={() => acao(() => api.post(`/secretaria/semestres/${selecionado}/abrir`), `Matrículas de ${selecionado} abertas.`)}
                  >
                    <Play size={15} /> Abrir matrículas
                  </Button>
                </>
              ) : null}
              {atual?.status === 'MATRICULAS_ABERTAS' ? (
                <Button className="btn-danger" onClick={encerrar} carregando={ocupado}>
                  <Square size={14} /> Encerrar período
                </Button>
              ) : null}
            </div>
          </Spot>
        </div>
      )}

      <Modal
        aberto={Boolean(curriculo)}
        titulo={`Currículo de ${selecionado ?? ''}`}
        descricao="Selecione o curso e as disciplinas ofertadas neste semestre."
        onFechar={() => setCurriculo(null)}
      >
        {curriculo ? (
          <form onSubmit={gerar} style={{ display: 'grid', gap: 16 }}>
            <div className="field">
              <label htmlFor="curso-curriculo">Curso</label>
              <select
                id="curso-curriculo"
                className="input"
                value={curriculo.cursoId}
                onChange={(e) => setCurriculo({ cursoId: e.target.value, ids: new Set() })}
              >
                {cursos.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nome}
                  </option>
                ))}
              </select>
            </div>
            <div className="checklist">
              {disciplinasDoCurso.length === 0 ? (
                <p style={{ color: 'var(--muted)' }}>Este curso ainda não tem disciplinas cadastradas.</p>
              ) : (
                disciplinasDoCurso.map((d) => (
                  <label key={d.id}>
                    <input type="checkbox" checked={curriculo.ids.has(d.id)} onChange={() => alternar(d.id)} />
                    <span className="mono">{d.codigo}</span>
                    <span style={{ flex: 1 }}>{d.nome}</span>
                    <Badge tom={d.obrigatoria ? '' : 'neutral'}>{d.obrigatoria ? 'Obrig.' : 'Optativa'}</Badge>
                  </label>
                ))
              )}
            </div>
            <div className="modal-foot" style={{ marginTop: 0 }}>
              <Button type="button" onClick={() => setCurriculo(null)}>
                Cancelar
              </Button>
              <Button type="submit" className="btn-primary" carregando={ocupado} disabled={curriculo.ids.size === 0}>
                Adicionar ao currículo
              </Button>
            </div>
          </form>
        ) : null}
      </Modal>
    </>
  )
}
