import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly message = signal('');
  readonly isVisible = signal(false);
  private resolve: ((value: boolean) => void) | null = null;

  confirm(msg: string): Promise<boolean> {
    this.message.set(msg);
    this.isVisible.set(true);
    return new Promise((resolve) => {
      this.resolve = resolve;
    });
  }

  confirmAction(result: boolean): void {
    this.resolve?.(result);
    this.resolve = null;
    this.isVisible.set(false);
  }
}
