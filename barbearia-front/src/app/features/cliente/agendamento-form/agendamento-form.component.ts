import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/api.service';
import { Agendamento, Barbeiro, Servico } from '../../../core/models';

function pickArray<T>(r: Record<string, unknown>, keys: string[]): T[] {
  for (const k of keys) {
    const v = r[k];
    if (Array.isArray(v)) {
      return v as T[];
    }
  }
  return [];
}

function toIsoLocal(dtLocal: string): string {
  if (!dtLocal) {
    return '';
  }
  return dtLocal.length === 16 ? `${dtLocal}:00` : dtLocal;
}

@Component({
  selector: 'app-agendamento-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './agendamento-form.component.html',
  styleUrl: './agendamento-form.component.scss',
})
export class AgendamentoFormComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  barbeiros = signal<Barbeiro[]>([]);
  servicos = signal<Servico[]>([]);
  barbeiroId: number | null = null;
  servicoId: number | null = null;
  dataHora = '';

  readonly editId = signal<number | null>(null);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : NaN;
    if (Number.isFinite(id)) {
      this.editId.set(id);
    }

    this.api.formOptionsCliente().subscribe({
      next: (opts) => {
        this.barbeiros.set(pickArray<Barbeiro>(opts, ['barbeiros', 'Barbeiros']));
        this.servicos.set(pickArray<Servico>(opts, ['servicos', 'Servicos']));
        const eid = this.editId();
        if (eid != null) {
          this.api.agendamentoClientePorId(eid).subscribe({
            next: (raw) => this.preencherDeAgendamento(raw as unknown as Agendamento),
            error: () => this.erro.set('Não foi possível carregar o agendamento.'),
            complete: () => this.carregando.set(false),
          });
        } else {
          this.carregando.set(false);
        }
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar opções do formulário.');
      },
    });
  }

  private preencherDeAgendamento(a: Agendamento): void {
    this.barbeiroId = a.barbeiro?.id ?? null;
    this.servicoId = a.servico?.id ?? null;
    const dh = a.dataHora;
    if (dh && typeof dh === 'string') {
      this.dataHora = dh.length >= 16 ? dh.slice(0, 16) : dh;
    }
  }

  salvar(): void {
    const bid = this.barbeiroId;
    const sid = this.servicoId;
    if (bid == null || sid == null || !this.dataHora) {
      this.erro.set('Preencha barbeiro, serviço e data.');
      return;
    }
    this.erro.set(null);
    this.salvando.set(true);
    const body = { barbeiroId: bid, servicoId: sid, dataHora: toIsoLocal(this.dataHora) };
    const eid = this.editId();
    
    console.log('Enviando agendamento:', body);
    console.log('Edit ID:', eid);
    
    const req =
      eid != null ? this.api.editarAgendamento(eid, body) : this.api.criarAgendamento(body);
    req.subscribe({
      next: (res) => {
        this.salvando.set(false);
        const novoId = eid ?? (res as { id?: number })?.id;
        if (novoId != null) {
          void this.router.navigateByUrl(`/agendamentos/confirmacao/${novoId}`);
        } else {
          void this.router.navigateByUrl('/agendamentos');
        }
      },
      error: (err) => {
        this.salvando.set(false);
        console.error('Erro ao salvar agendamento:', err);
        const errorMsg = err.error?.message || err.message || 'Não foi possível salvar o agendamento.';
        this.erro.set(errorMsg);
      },
    });
  }
}
