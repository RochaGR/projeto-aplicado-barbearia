import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../../core/api.service';
import { ConfigHorario, Feriado } from '../../../core/models';

@Component({
  selector: 'app-admin-horarios',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './horarios.component.html',
  styleUrl: './horarios.component.scss',
})
export class AdminHorariosComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly horarios = signal<ConfigHorario[]>([]);
  readonly feriados = signal<Feriado[]>([]);
  readonly carregando = signal(true);
  readonly salvandoId = signal<number | null>(null);
  readonly erro = signal<string | null>(null);
  readonly okMsg = signal<string | null>(null);

  novoFeriadoData = '';
  novoFeriadoMotivo = '';
  criandoFeriado = signal(false);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.api.horariosAdmin().subscribe({
      next: (r) => { this.horarios.set(r.horarios ?? []); this.carregarFeriados(); },
      error: () => { this.carregando.set(false); this.erro.set('Erro ao carregar horários.'); },
    });
  }

  private carregarFeriados(): void {
    this.api.feriadosAdmin().subscribe({
      next: (r) => { this.feriados.set(r.feriados ?? []); this.carregando.set(false); },
      error: () => { this.carregando.set(false); },
    });
  }

  toggleAtivo(h: ConfigHorario): void {
    this.salvandoId.set(h.id);
    this.api.atualizarHorarioAdmin(h.id, { ativo: !h.ativo }).subscribe({
      next: () => {
        h.ativo = !h.ativo;
        this.salvandoId.set(null);
      },
      error: () => { this.salvandoId.set(null); this.erro.set('Erro ao salvar.'); },
    });
  }

  salvarHorario(h: ConfigHorario): void {
    this.salvandoId.set(h.id);
    this.api.atualizarHorarioAdmin(h.id, {
      ativo: h.ativo,
      abertura: h.abertura || undefined,
      fechamento: h.fechamento || undefined,
    } as Partial<ConfigHorario>).subscribe({
      next: () => { this.salvandoId.set(null); this.okMsg.set('Salvo!'); setTimeout(() => this.okMsg.set(null), 2000); },
      error: () => { this.salvandoId.set(null); this.erro.set('Erro ao salvar.'); },
    });
  }

  adicionarFeriado(): void {
    if (!this.novoFeriadoData || !this.novoFeriadoMotivo.trim()) return;
    this.criandoFeriado.set(true);
    this.api.criarFeriadoAdmin({ data: this.novoFeriadoData, motivo: this.novoFeriadoMotivo.trim() }).subscribe({
      next: () => {
        this.criandoFeriado.set(false);
        this.novoFeriadoData = '';
        this.novoFeriadoMotivo = '';
        this.carregarFeriados();
      },
      error: (err) => {
        this.criandoFeriado.set(false);
        this.erro.set(err.error?.message ?? 'Erro ao criar feriado.');
      },
    });
  }

  excluirFeriado(f: Feriado): void {
    if (!confirm(`Excluir feriado "${f.motivo}" (${f.data})?`)) return;
    this.api.excluirFeriadoAdmin(f.id).subscribe({
      next: () => this.carregarFeriados(),
      error: () => this.erro.set('Erro ao excluir feriado.'),
    });
  }
}
