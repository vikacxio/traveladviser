import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { SignupComponent } from './signup.component';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'signup', component: SignupComponent }
];

@NgModule({
  imports: [LoginComponent, SignupComponent, RouterModule.forChild(routes)]
})
export class AuthModule {}
