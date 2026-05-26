import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface Opcao {
  id: number;
  nome: string;
  preco?: string;
  duracao?: string;
}

export interface ChatResponse {
  resposta: string;
  servicos?: Opcao[];
  barbeiros?: Opcao[];
  servicoSelecionadoId?: number;
  barbeiroSelecionadoId?: number;
}

export interface AgendarRequest {
  servicoId: number;
  barbeiroId: number;
  data: string;
  horario: string;
}

export interface DisponibilidadeResponse {
  disponivel: boolean;
  sugestoes: string[];
}

export interface AgendarResponse {
  id: number;
  mensagem: string;
  servico: string;
  barbeiro: string;
  dataHora: string;
  preco: string;
  desconto?: string;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);

  iniciar(): Observable<{ servicos: Opcao[]; barbeiros: Opcao[] }> {
    return this.http.get<{ servicos: Opcao[]; barbeiros: Opcao[] }>('/api/chat/iniciar');
  }

  enviarMensagem(mensagem: string, historico: ChatMessage[]): Observable<ChatResponse> {
    const historicoRecente = historico.slice(-10).map(m => ({
      role: m.role,
      content: m.content,
    }));
    return this.http.post<ChatResponse>('/api/chat/mensagem', {
      mensagem,
      historico: historicoRecente,
    });
  }

  agendar(dados: AgendarRequest): Observable<AgendarResponse> {
    return this.http.post<AgendarResponse>('/api/chat/agendar', dados);
  }

  verificarDisponibilidade(dados: { barbeiroId: number; data: string; horario: string }): Observable<DisponibilidadeResponse> {
    return this.http.post<DisponibilidadeResponse>('/api/chat/disponibilidade', dados);
  }

  getHorariosDisponiveis(barbeiroId: number, data: string): Observable<string[]> {
    return this.http.get<string[]>(`/api/chat/horarios-disponiveis?barbeiroId=${barbeiroId}&data=${data}`);
  }
}
