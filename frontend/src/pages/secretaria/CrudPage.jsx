import { useMemo, useState } from 'react'
import { Pencil, Plus, Search, Trash2 } from 'lucide-react'
import { api } from '../../api.js'
import { useCarregar } from '../../hooks.js'
import { useToast } from '../../components/Toast.jsx'
import { Button, Empty, Modal, PageHeader, Skeletons } from '../../components/ui.jsx'

/**
 * Tela de CRUD genérica usada pelos cadastros da secretaria.
 * `campos` pode ser uma função que recebe as opções carregadas (ex.: lista de cursos).
 */
export default function CrudPage({
  eyebrow,
  titulo,
  singular,
  descricao,
  icone,
  endpoint,
  colunas,
  campos,
  inicial,
  paraFormulario,
  opcoesEndpoints = {},
  vazio,
}) {
  const toast = useToast()
  const chavesOpcoes = Object.keys(opcoesEndpoints)
  const { dados, carregando, recarregar } = useCarregar([endpoint, ...chavesOpcoes.map((k) => opcoesEndpoints[k])])
  const itens = dados?.[0] ?? []
  const opcoes = useMemo(
    () => Object.fromEntries(chavesOpcoes.map((k, i) => [k, dados?.[i + 1] ?? []])),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [dados],
  )

  const [busca, setBusca] = useState('')
  const [edicao, setEdicao] = useState(null) // { id?, valores }
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  const lista = typeof campos === 'function' ? campos(opcoes) : campos
  const filtrados = itens.filter((i) => JSON.stringify(i).toLowerCase().includes(busca.toLowerCase()))

  const abrir = (item) => {
    setErro('')
    setEdicao(item ? { id: item.id, valores: paraFormulario(item) } : { valores: { ...inicial } })
  }
  const definir = (nome, valor) => setEdicao((e) => ({ ...e, valores: { ...e.valores, [nome]: valor } }))

  async function salvar(e) {
    e.preventDefault()
    setErro('')
    setSalvando(true)
    try {
      if (edicao.id) await api.put(`${endpoint}/${edicao.id}`, edicao.valores)
      else await api.post(endpoint, edicao.valores)
      toast.ok(edicao.id ? `${singular} atualizado.` : `${singular} cadastrado.`)
      setEdicao(null)
      await recarregar()
    } catch (err) {
      setErro(err.message)
    } finally {
      setSalvando(false)
    }
  }

  async function excluir(item) {
    if (!window.confirm(`Excluir "${item.nome ?? item.codigo}"? Esta ação não pode ser desfeita.`)) return
    try {
      await api.del(`${endpoint}/${item.id}`)
      toast.ok(`${singular} excluído(a).`)
      await recarregar()
    } catch (err) {
      toast.erro(err.message)
    }
  }

  return (
    <>
      <PageHeader eyebrow={eyebrow} titulo={titulo} descricao={descricao}>
        <Button className="btn-primary" onClick={() => abrir(null)}>
          <Plus size={16} /> Cadastrar {singular.toLowerCase()}
        </Button>
      </PageHeader>

      {carregando ? (
        <Skeletons n={1} />
      ) : itens.length === 0 ? (
        <Empty icone={icone} titulo={vazio.titulo}>
          {vazio.texto}
        </Empty>
      ) : (
        <>
          <div className="toolbar">
            <div style={{ position: 'relative', flex: 1, maxWidth: 320 }}>
              <Search size={16} style={{ position: 'absolute', left: 12, top: 13, color: 'var(--muted)' }} />
              <input
                className="input"
                style={{ paddingLeft: 36 }}
                placeholder="Buscar…"
                aria-label="Buscar"
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
            </div>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  {colunas.map((c) => (
                    <th key={c.rotulo}>{c.rotulo}</th>
                  ))}
                  <th />
                </tr>
              </thead>
              <tbody>
                {filtrados.map((item) => (
                  <tr key={item.id}>
                    {colunas.map((c) => (
                      <td key={c.rotulo} className={c.mono ? 'mono' : ''}>
                        {c.render ? c.render(item) : item[c.chave]}
                      </td>
                    ))}
                    <td className="actions">
                      <button className="btn btn-icon" onClick={() => abrir(item)} aria-label="Editar" title="Editar">
                        <Pencil size={15} />
                      </button>
                      <button
                        className="btn btn-icon btn-danger"
                        onClick={() => excluir(item)}
                        aria-label="Excluir"
                        title="Excluir"
                      >
                        <Trash2 size={15} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {filtrados.length === 0 ? (
              <p style={{ padding: 24, textAlign: 'center', color: 'var(--muted)' }}>Nada encontrado para “{busca}”.</p>
            ) : null}
          </div>
        </>
      )}

      <Modal
        aberto={Boolean(edicao)}
        titulo={edicao?.id ? `Editar ${singular.toLowerCase()}` : `Cadastrar ${singular.toLowerCase()}`}
        onFechar={() => setEdicao(null)}
      >
        {edicao ? (
          <form onSubmit={salvar}>
            <div className="form-grid">
              {lista.map((c) => (
                <div key={c.nome} className={`field ${c.cheio ? 'full' : ''}`}>
                  {c.tipo === 'checkbox' ? (
                    <label className="check">
                      <input
                        type="checkbox"
                        checked={Boolean(edicao.valores[c.nome])}
                        onChange={(e) => definir(c.nome, e.target.checked)}
                      />
                      {c.rotulo}
                    </label>
                  ) : (
                    <>
                      <label htmlFor={`f-${c.nome}`}>{c.rotulo}</label>
                      {c.tipo === 'select' ? (
                        <select
                          id={`f-${c.nome}`}
                          className="input"
                          required={c.obrigatorio}
                          value={edicao.valores[c.nome] ?? ''}
                          onChange={(e) => definir(c.nome, e.target.value)}
                        >
                          <option value="">{c.vazio ?? 'Selecione…'}</option>
                          {c.opcoes.map((o) => (
                            <option key={o.valor} value={o.valor}>
                              {o.rotulo}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <input
                          id={`f-${c.nome}`}
                          className="input"
                          type={c.tipo ?? 'text'}
                          min={c.min}
                          required={c.obrigatorio && !(c.somenteNaCriacao && edicao.id)}
                          autoComplete={c.tipo === 'password' ? 'new-password' : 'off'}
                          value={edicao.valores[c.nome] ?? ''}
                          onChange={(e) =>
                            definir(c.nome, c.tipo === 'number' ? Number(e.target.value) : e.target.value)
                          }
                        />
                      )}
                      {c.dica ? <span className="hint">{c.dica}</span> : null}
                    </>
                  )}
                </div>
              ))}
            </div>
            {erro ? (
              <p className="error-text" role="alert" style={{ marginTop: 16 }}>
                {erro}
              </p>
            ) : null}
            <div className="modal-foot">
              <Button type="button" onClick={() => setEdicao(null)}>
                Cancelar
              </Button>
              <Button type="submit" className="btn-primary" carregando={salvando}>
                Salvar
              </Button>
            </div>
          </form>
        ) : null}
      </Modal>
    </>
  )
}
