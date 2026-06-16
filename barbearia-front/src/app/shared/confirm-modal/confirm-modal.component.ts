import { Component, inject } from '@angular/core';
import { ConfirmService } from './confirm.service';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  template: `
    @if (service.isVisible()) {
      <div class="confirm-overlay" (click)="service.confirmAction(false)">
        <div class="confirm-box" (click)="$event.stopPropagation()">
          <p>{{ service.message() }}</p>
          <div class="confirm-actions">
            <button class="btn-ghost" (click)="service.confirmAction(false)">Cancelar</button>
            <button class="btn-brand" (click)="service.confirmAction(true)">Confirmar</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .confirm-overlay {
      position: fixed; inset: 0;
      background: rgba(0,0,0,0.65);
      display: flex; align-items: center; justify-content: center;
      z-index: 9999;
    }
    .confirm-box {
      background: #1a1d25;
      border: 1px solid rgba(255,255,255,0.15);
      border-radius: 12px;
      padding: 1.5rem;
      max-width: 420px;
      width: 90%;
      color: #fff;
    }
    .confirm-box p {
      margin: 0 0 1.25rem;
      white-space: pre-line;
      line-height: 1.5;
    }
    .confirm-actions {
      display: flex; gap: 0.5rem; justify-content: flex-end;
    }
    .btn-ghost {
      background: rgba(255,255,255,0.08);
      border: 1px solid rgba(255,255,255,0.15);
      color: var(--bb-muted);
      padding: 0.45rem 1rem;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.85rem;
    }
    .btn-ghost:hover {
      background: rgba(255,255,255,0.14);
      color: #fff;
    }
    .btn-brand {
      background: var(--bb-accent);
      border: none;
      color: #fff;
      padding: 0.45rem 1rem;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.85rem;
      font-weight: 600;
    }
    .btn-brand:hover {
      filter: brightness(1.1);
    }
  `],
})
export class ConfirmModalComponent {
  readonly service = inject(ConfirmService);
}
