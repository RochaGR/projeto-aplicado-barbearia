import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Servico } from '../../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);

  readonly ano = new Date().getFullYear();
  readonly servicos = signal<Servico[]>([]);
  readonly setupNecessario = signal<boolean | null>(null);
  readonly carregando = signal(true);

  /** Imagens de vitrine (mesmas referências visuais do site Thymeleaf). */
  readonly imagensServico = [
    'https://i.pinimg.com/564x/33/30/d8/3330d88a6613fbb905ccfc5277cdc415.jpg', // Corte Side Fade
    'https://moda20.com.br/wp-content/uploads/2023/10/Luca-Lyra_Easy-Resize.com_.jpg', // Corte Undercut
    'https://i.pinimg.com/564x/5b/9d/01/5b9d01ccecdaab4b3e1c7e513e8c224d.jpg', // Corte Side Part
    'https://images.unsplash.com/photo-1622287162006-2aeac74df1ae?auto=format&fit=crop&q=80&w=900', // Barba Completa
    'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?ixlib=rb-1.2.1&auto=format&fit=crop&q=80&w=900', // Corte Degradado
    'https://images.unsplash.com/photo-1621605815971-fbc98d665033?auto=format&fit=crop&q=80&w=900', // Corte e Barba
    'https://images.unsplash.com/photo-1519376970917-efd549d5e034?auto=format&fit=crop&q=80&w=900', // Tratamento de Sobrancelha
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=900', // Corte Infantil
  ];

  readonly heroImg =
    'https://images.unsplash.com/photo-1621605815971-fbc98d665033?auto=format&fit=crop&q=80&w=1920';
  readonly sobreImg =
    'https://images.unsplash.com/photo-1585747860715-2ba37e788b70?ixlib=rb-1.2.1&auto=format&fit=crop&q=80&w=900';
  readonly equipeImg =
    'https://dipatrones.com.br/images/Equipe/Equipe-Barbearia-Dipatrones-06.jpeg';
  readonly unidadeImg =
    'https://gfdecor.vtexassets.com/arquivos/ids/156335/GFC-BARBER-02--foto-2-.jpg?v=637769028839000000';

  ngOnInit(): void {
    this.api.setupRequired().subscribe({
      next: (r) => this.setupNecessario.set(r.required),
      error: () => this.setupNecessario.set(null),
    });
    this.api.servicosPublicos().subscribe({
      next: (s) => {
        this.servicos.set(s);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  imagemParaServico(index: number): string {
    return this.imagensServico[index % this.imagensServico.length];
  }
}
