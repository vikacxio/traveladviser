import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatComponent } from './features/utility/chat.component';
import { ProfileComponent } from './features/utility/profile.component';
import { SplitExpensesComponent } from './features/splitexpense/split-expenses.component';

export const routes: Routes = [
  { path: '', redirectTo: 'auth', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then((m) => m.AuthModule)
  },
  {
    path: 'home',
    loadChildren: () => import('./features/travel/travel.module').then((m) => m.TravelModule)
  },
  {
    path: 'place',
    loadChildren: () => import('./features/travel/travel.module').then((m) => m.TravelModule)
  },
  { path: 'split-expenses', component: SplitExpensesComponent },
  { path: 'chat', component: ChatComponent },
  { path: 'profile', component: ProfileComponent },
  { path: '**', redirectTo: 'auth' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
