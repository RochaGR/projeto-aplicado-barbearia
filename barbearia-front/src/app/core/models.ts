export interface UserInfo {
  username: string;
  roles: string[];
  telefone?: string;
  telefonePendente?: boolean;
}

export interface Servico {
  id: number;
  nome: string;
  descricao: string;
  preco: number;
  duracaoMinutos: number;
  ativo: boolean;
  imageUrl?: string;
}

export interface Barbeiro {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  ativo: boolean;
  senha?: string;
}

export interface Cliente {
  id?: number;
  nome: string;
  telefone: string;
  email: string;
  senha?: string;
}

export interface Agendamento {
  id: number;
  dataHora: string;
  status?: string;
  cliente?: Cliente;
  barbeiro?: Barbeiro;
  servico?: Servico;
  precoOriginal?: number;
  precoFinal?: number;
  valorDescontado?: number;
  percentualDesconto?: number | null;
  descontoAplicado?: boolean;
}

export interface HorarioDisponivel {
  horario: string;
  disponivel: boolean;
}

export interface DashboardStats {
  totalAgendamentos: number;
  totalClientes: number;
  totalBarbeiros: number;
  agendados: number;
  confirmados: number;
  concluidos: number;
  cancelados: number;
  agendamentosHoje: number;
  agendamentosSemana: number;
  agendamentosMes: number;
  receitaMes: number;
  barbeiroDestaque: string;
  servicoDestaque: string;
  taxaCancelamento: string;
  proximosAgendamentos?: Agendamento[];
}
