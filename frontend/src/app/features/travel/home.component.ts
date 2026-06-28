import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PlaceDTO, SearchPlacesRequest } from '../../core/models';
import { PlaceService } from '../../core/place.service';
import { PlaceCardComponent } from '../../shared/components/place-card.component';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [CommonModule, FormsModule, PlaceCardComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  query = '';
  currentLatitude: number | null = null;
  currentLongitude: number | null = null;
  nearbyPlaces: PlaceDTO[] = [];
  searchResults: PlaceDTO[] = [];
  weatherRecommendations: PlaceDTO[] = [];
  trendingPlaces: PlaceDTO[] = [];
  searchSuggestions: PlaceDTO[] = [];
  showSuggestions = false;
  loadingNearby = false;
  loadingSearch = false;
  loadingWeather = false;
  loadingSuggestions = false;
  loadingTrending = false;
  errorMessage = '';

  constructor(private placeService: PlaceService, private router: Router) {}

  ngOnInit(): void {
    this.requestLocationPermission();
  }

  requestLocationPermission(): void {
    if (!navigator.geolocation) {
      this.errorMessage = 'Geolocation is not supported by your browser.';
      this.loadTrendingPlaces();
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.currentLatitude = position.coords.latitude;
        this.currentLongitude = position.coords.longitude;
        this.loadNearbyPlaces();
      },
      () => {
        this.errorMessage = 'Location permission denied. Showing trending places instead.';
        this.loadTrendingPlaces();
      }
    );
  }

  loadNearbyPlaces(): void {
    if (this.currentLatitude === null || this.currentLongitude === null) {
      return;
    }

    this.loadingNearby = true;
    this.placeService.getNearbyPlaces(this.currentLatitude, this.currentLongitude, undefined, 12).subscribe({
      next: (places) => {
        this.nearbyPlaces = places;
        this.loadingNearby = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load nearby places.';
        this.loadingNearby = false;
      }
    });
  }

  loadTrendingPlaces(): void {
    this.loadingTrending = true;
    this.placeService.getTrendingPlaces(12).subscribe({
      next: (places) => {
        this.trendingPlaces = places;
        this.loadingTrending = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load trending places.';
        this.loadingTrending = false;
      }
    });
  }

  searchDestination(): void {
    if (!this.query.trim()) {
      return;
    }

    this.loadingSearch = true;
    const request: SearchPlacesRequest = {
      city: this.query,
      limit: 12
    };

    this.placeService.searchPlaces(request).subscribe({
      next: (places) => {
        this.searchResults = places;
        this.loadingSearch = false;
      },
      error: () => {
        this.errorMessage = 'Unable to search destinations.';
        this.loadingSearch = false;
      }
    });
  }

  onSearchInput(): void {
    const query = this.query.trim();
    if (query.length < 1) {
      this.searchSuggestions = [];
      this.showSuggestions = false;
      return;
    }

    this.loadingSuggestions = true;
    this.placeService.searchPlacesByName(query, 8).subscribe({
      next: (places) => {
        this.searchSuggestions = places;
        this.showSuggestions = places.length > 0;
        this.loadingSuggestions = false;
      },
      error: () => {
        this.searchSuggestions = [];
        this.showSuggestions = false;
        this.loadingSuggestions = false;
      }
    });
  }

  selectSuggestion(place: PlaceDTO): void {
    this.query = place.name;
    this.showSuggestions = false;
    this.searchSuggestions = [];
    this.goToPlace(place);
  }

  hideSuggestions(): void {
    // Delay hiding to allow click events on suggestions
    setTimeout(() => {
      this.showSuggestions = false;
    }, 150);
  }

  getWeatherRecommendations(): void {
    if (this.currentLatitude === null || this.currentLongitude === null) {
      this.errorMessage = 'Please allow location access to show weather recommendations.';
      return;
    }

    this.loadingWeather = true;
    this.placeService.getWeatherRecommendations(this.currentLatitude, this.currentLongitude, 10).subscribe({
      next: (places) => {
        this.weatherRecommendations = places;
        this.loadingWeather = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load weather-based recommendations.';
        this.loadingWeather = false;
      }
    });
  }

  goToPlace(place: PlaceDTO): void {
    this.router.navigate(['/place', place.id]);
  }

  distanceToPlace(place: PlaceDTO): string {
    if (this.currentLatitude === null || this.currentLongitude === null) {
      return '';
    }

    const distance = this.calculateDistance(
      this.currentLatitude,
      this.currentLongitude,
      place.latitude,
      place.longitude
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
