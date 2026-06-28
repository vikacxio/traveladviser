import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HomeComponent } from './home.component';
import { PlaceDetailComponent } from './place-detail.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: ':id', component: PlaceDetailComponent }
];

@NgModule({
  imports: [HomeComponent, PlaceDetailComponent, RouterModule.forChild(routes)]
})
export class TravelModule {}
