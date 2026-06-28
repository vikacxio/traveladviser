import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PlaceDTO } from '../../core/models';
import { PlaceService } from '../../core/place.service';
import { PlaceCardComponent } from '../../shared/components/place-card.component';

@Component({
  standalone: true,
  selector: 'app-place-detail',
  imports: [CommonModule, PlaceCardComponent],
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.css']
})
export class PlaceDetailComponent implements OnInit {
  place: PlaceDTO | null = null;
  nearbyPlaces: PlaceDTO[] = [];
  currentLatitude: number | null = null;
  currentLongitude: number | null = null;
  errorMessage = '';
  loading = false;

  constructor(private route: ActivatedRoute, private placeService: PlaceService, private router: Router) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/home']);
      return;
    }

    this.loadCurrentLocation();
    this.loadPlace(id);
  }

  loadPlace(id: number): void {
    this.loading = true;
    this.placeService.getPlaceById(id).subscribe({
      next: (place) => {
        this.place = place;
        this.loading = false;
        this.loadNearbyForPlace();
      },
      error: () => {
        this.errorMessage = 'Unable to load place details.';
        this.loading = false;
      }
    });
  }

  loadNearbyForPlace(): void {
    if (!this.place) {
      return;
    }
    this.placeService.getNearbyPlaces(this.place.latitude, this.place.longitude, undefined, 6).subscribe({
      next: (places) => {
        this.nearbyPlaces = places.filter((item) => item.id !== this.place?.id);
      },
      error: () => {
        this.errorMessage = 'Unable to load nearby places for this location.';
      }
    });
  }

  loadCurrentLocation(): void {
    if (!navigator.geolocation) {
      return;
    }

    navigator.geolocation.getCurrentPosition((position) => {
      this.currentLatitude = position.coords.latitude;
      this.currentLongitude = position.coords.longitude;
    });
  }

  getDistanceFromUser(): string {
    if (!this.place || this.currentLatitude === null || this.currentLongitude === null) {
      return 'Unknown';
    }

    const distance = this.calculateDistance(
      this.currentLatitude,
      this.currentLongitude,
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

  goBack(): void {
    this.router.navigate(['/home']);
  }
}
