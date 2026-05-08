import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Barbeiro, DashboardStats, HorarioDisponivel, Servico } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  setupRequired(): Observable<{ required: boolean }> {
    return this.http.get<{ required: boolean }>('/api/setup/required');
  }

  primeiroAdmin(admin: { nome: string; email: string; telefone: string; senha: string }): Observable<unknown> {
    return this.http.post('/api/setup/primeiro-admin', admin);
  }

  cadastroCliente(c: { nome: string; telefone: string; email: string; senha: string }): Observable<unknown> {
    return this.http.post('/api/clientes/cadastro', c);
  }

  servicosPublicos(): Observable<Servico[]> {
    return this.http.get<Servico[]>('/api/servicos/publicos');
  }

  formOptionsCliente(): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>('/api/cliente/agendamentos/form-options');
  }

  horariosDisponiveis(barbeiroId: number, servicoId: number, data: string): Observable<{ horarios: HorarioDisponivel[] }> {
    return this.http.get<{ horarios: HorarioDisponivel[] }>('/api/cliente/agendamentos/horarios-disponiveis', {
      params: { barbeiroId, servicoId, data },
    });
  }

  agendamentoClientePorId(id: number): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`/api/cliente/agendamentos/${id}`);
  }

  criarAgendamento(body: { barbeiroId: number; servicoId: number; dataHora: string }): Observable<{ id: number }> {
    return this.http.post<{ id: number }>('/api/cliente/agendamentos', body);
  }

  editarAgendamento(
    id: number,
    body: { barbeiroId: number; servicoId: number; dataHora: string },
  ): Observable<unknown> {
    return this.http.put(`/api/cliente/agendamentos/${id}`, body);
  }

  confirmacaoAgendamento(id: number): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`/api/cliente/agendamentos/${id}/confirmacao`);
  }

  listarAgendamentosCliente(data?: string): Observable<Record<string, unknown>> {
    let params = new HttpParams();
    if (data) {
      params = params.set('data', data);
    }
    return this.http.get<Record<string, unknown>>('/api/cliente/agendamentos', { params });
  }

  cancelarAgendamentoCliente(id: number): Observable<unknown> {
    return this.http.post(`/api/cliente/agendamentos/${id}/cancelar`, {});
  }

  fidelidadeCliente(): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>('/api/cliente/fidelidade');
  }

  historicoCliente(q: { dataInicio?: string; dataFim?: string; status?: string }): Observable<Record<string, unknown>> {
    let params = new HttpParams();
    if (q.dataInicio) {
      params = params.set('dataInicio', q.dataInicio);
    }
    if (q.dataFim) {
      params = params.set('dataFim', q.dataFim);
    }
    if (q.status) {
      params = params.set('status', q.status);
    }
    return this.http.get<Record<string, unknown>>('/api/cliente/historico', { params });
  }

  agendamentosBarbeiro(data?: string): Observable<Record<string, unknown>> {
    let params = new HttpParams();
    if (data) {
      params = params.set('data', data);
    }
    return this.http.get<Record<string, unknown>>('/api/barbeiro/agendamentos', { params });
  }

  concluirAgendamentoBarbeiro(id: number): Observable<unknown> {
    return this.http.post(`/api/barbeiro/agendamentos/${id}/concluir`, {});
  }

  cancelarAgendamentoBarbeiro(id: number): Observable<unknown> {
    return this.http.post(`/api/barbeiro/agendamentos/${id}/cancelar`, {});
  }

  dashboardAdmin(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>('/api/admin/dashboard');
  }

  todosAgendamentosAdmin(data?: string, status?: string): Observable<Record<string, unknown>> {
    let params = new HttpParams();
    if (data) {
      params = params.set('data', data);
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<Record<string, unknown>>('/api/admin/agendamentos', { params });
  }

  confirmarAgAdmin(id: number): Observable<unknown> {
    return this.http.post(`/api/admin/agendamentos/${id}/confirmar`, {});
  }

  cancelarAgAdmin(id: number): Observable<unknown> {
    return this.http.post(`/api/admin/agendamentos/${id}/cancelar`, {});
  }

  barbeirosAdmin(): Observable<{ barbeiros: Barbeiro[] }> {
    return this.http.get<{ barbeiros: Barbeiro[] }>('/api/admin/barbeiros');
  }

  cadastrarBarbeiroAdmin(b: Partial<Barbeiro> & { senha: string }): Observable<unknown> {
    return this.http.post('/api/admin/barbeiros', b);
  }

  toggleBarbeiro(id: number): Observable<unknown> {
    return this.http.post(`/api/admin/barbeiros/${id}/toggle`, {});
  }

  excluirBarbeiro(id: number): Observable<unknown> {
    return this.http.delete(`/api/admin/barbeiros/${id}`);
  }

  servicosAdmin(): Observable<{ servicos: Servico[] }> {
    return this.http.get<{ servicos: Servico[] }>('/api/admin/servicos');
  }

  cadastrarServicoAdmin(s: Partial<Servico>): Observable<unknown> {
    return this.http.post('/api/admin/servicos', s);
  }

  excluirServico(id: number): Observable<unknown> {
    return this.http.delete(`/api/admin/servicos/${id}`);
  }

  fidelidadeConfigAdmin(): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>('/api/admin/fidelidade');
  }

  salvarFidelidadeAdmin(body: { percentualDesconto: number; cortesParaDesconto: number }): Observable<unknown> {
    return this.http.post('/api/admin/fidelidade', body);
  }
}
