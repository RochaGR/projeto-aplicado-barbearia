import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';
import { AdminBarbeirosComponent } from './features/admin/barbeiros/barbeiros.component';
import { AdminDashboardComponent } from './features/admin/dashboard/dashboard.component';
import { AdminFidelidadeConfigComponent } from './features/admin/fidelidade-config/fidelidade-config.component';
import { AdminServicosComponent } from './features/admin/servicos/servicos.component';
import { TodosAgendamentosComponent } from './features/admin/todos-agendamentos/todos-agendamentos.component';
import { AgendamentosBarbeiroComponent } from './features/barbeiro/agendamentos-barbeiro/agendamentos-barbeiro.component';
import { CadastroComponent } from './features/auth/cadastro/cadastro.component';
import { ConfigInicialComponent } from './features/auth/config-inicial/config-inicial.component';
import { LoginComponent } from './features/auth/login/login.component';
import { OAuth2CallbackComponent } from './features/auth/oauth2-callback/oauth2-callback.component';
import { AgendamentoFormComponent } from './features/cliente/agendamento-form/agendamento-form.component';
import { ConfirmacaoComponent } from './features/cliente/confirmacao/confirmacao.component';
import { FidelidadeComponent } from './features/cliente/fidelidade/fidelidade.component';
import { HistoricoComponent } from './features/cliente/historico/historico.component';
import { ListaAgendamentosComponent } from './features/cliente/lista-agendamentos/lista-agendamentos.component';
import { HomeComponent } from './features/home/home.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'cadastro', component: CadastroComponent },
  { path: 'config-inicial', component: ConfigInicialComponent },
  { path: 'oauth2/callback', component: OAuth2CallbackComponent },
  {
    path: 'agendamentos/novo',
    component: AgendamentoFormComponent,
    canActivate: [authGuard, roleGuard(['CLIENTE'])],
  },
  {
    path: 'agendamentos/editar/:id',
    component: AgendamentoFormComponent,
    canActivate: [authGuard, roleGuard(['CLIENTE'])],
  },
  {
    path: 'agendamentos/confirmacao/:id',
    component: ConfirmacaoComponent,
    canActivate: [authGuard, roleGuard(['CLIENTE'])],
  },
  {
    path: 'agendamentos',
    component: ListaAgendamentosComponent,
    canActivate: [authGuard, roleGuard(['CLIENTE'])],
  },
  {
    path: 'cliente/fidelidade',
    component: FidelidadeComponent,
    canActivate: [authGuard, roleGuard(['CLIENTE'])],
  },
  {
    path: 'cliente/historico',
    component: HistoricoComponent,
    canActivate: [authGuard, roleGuard(['CLIENTE'])],
  },
  {
    path: 'barbeiro/agendamentos',
    component: AgendamentosBarbeiroComponent,
    canActivate: [authGuard, roleGuard(['BARBEIRO'])],
  },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },
  {
    path: 'admin/agendamentos',
    component: TodosAgendamentosComponent,
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },
  {
    path: 'admin/barbeiros',
    component: AdminBarbeirosComponent,
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },
  {
    path: 'admin/servicos',
    component: AdminServicosComponent,
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },
  {
    path: 'admin/fidelidade',
    component: AdminFidelidadeConfigComponent,
    canActivate: [authGuard, roleGuard(['ADMIN'])],
  },
  { path: '**', redirectTo: '' },
];
