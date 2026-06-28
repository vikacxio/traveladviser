import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlaceCardComponent } from './components/place-card.component';
import { ImageCarouselDirective } from './directives/image-carousel.directive';

@NgModule({
  imports: [CommonModule, PlaceCardComponent, ImageCarouselDirective],
  exports: [CommonModule, PlaceCardComponent, ImageCarouselDirective]
})
export class SharedComponentsModule { }
