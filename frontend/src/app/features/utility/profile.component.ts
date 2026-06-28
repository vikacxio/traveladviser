import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service';

@Component({
  standalone: true,
  selector: 'app-profile',
  imports: [CommonModule],
  template: `
    <section class="rounded-3xl bg-white p-8 shadow-sm">
      <h1 class="text-2xl font-semibold text-slate-900">User Profile</h1>
      <p class="mt-3 text-slate-600">
        Logged in as <span class="font-medium text-slate-900">{{ userName }}</span>
      </p>
    </section>
  `
})
export class ProfileComponent {
  constructor(private authService: AuthService) {}

  get userName(): string {
    return this.authService.getUserName() ?? 'User';
  }
}
