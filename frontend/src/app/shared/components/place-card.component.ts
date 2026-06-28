import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlaceDTO } from '../../core/models';
import { ImageCarouselService, CarouselImage } from '../../core/image-carousel.service';

@Component({
  standalone: true,
  selector: 'app-place-card',
  imports: [CommonModule],
  templateUrl: './place-card.component.html',
  styleUrls: ['./place-card.component.css']
})
export class PlaceCardComponent {
  @Input() place!: PlaceDTO;
  @Input() userLatitude?: number | null;
  @Input() userLongitude?: number | null;
  @Output() viewPlace = new EventEmitter<PlaceDTO>();

  currentImageIndex = 0;

  constructor(private carouselService: ImageCarouselService) {}

  getAllImages(): CarouselImage[] {
    if (!this.place) return [];
    return this.carouselService.getAllImages(
      this.place.primaryImageSourceUrl,
      this.place.primaryImageDownloadUrl,
      this.place.images
    );
  }

  getCurrentImageUrl(): string {
    const allImages = this.getAllImages();
    return this.carouselService.getCurrentImageUrl(allImages, this.currentImageIndex);
  }

  getTotalImages(): number {
    return this.getAllImages().length;
  }

  nextImage(event: Event): void {
    event.stopPropagation();
    const totalImages = this.getTotalImages();
    this.currentImageIndex = this.carouselService.nextImage(this.currentImageIndex, totalImages);
  }

  previousImage(event: Event): void {
    event.stopPropagation();
    const totalImages = this.getTotalImages();
    this.currentImageIndex = this.carouselService.previousImage(this.currentImageIndex, totalImages);
  }

  distance(): string {
    if (this.userLatitude == null || this.userLongitude == null) {
      return '';
    }

    const distance = this.calculateDistance(
      this.userLatitude,
      this.userLongitude,
      this.place.latitude,
      this.place.longitude
    );
    return `${distance.toFixed(1)} km`;
  }

  private calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const toRad = (value: number) => (value * Math.PI) / 180;
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
}
