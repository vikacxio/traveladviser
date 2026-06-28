import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PlaceDTO, SearchPlacesRequest } from './models';

@Injectable({ providedIn: 'root' })
export class PlaceService {
  constructor(private api: ApiService) {}

  searchPlaces(request: SearchPlacesRequest): Observable<PlaceDTO[]> {
    return this.api.post<PlaceDTO[]>('/api/places/search', request);
  }

  searchPlacesByName(query: string, limit = 10): Observable<PlaceDTO[]> {
    const params = new HttpParams()
      .set('query', query)
      .set('limit', limit.toString());
    return this.api.get<PlaceDTO[]>('/api/places/search', params);
  }

  getNearbyPlaces(latitude: number, longitude: number, radiusKm = 500, limit = 10): Observable<PlaceDTO[]> {
    const params = new HttpParams()
      .set('latitude', latitude.toString())
      .set('longitude', longitude.toString())
      .set('radiusKm', radiusKm.toString())
      .set('limit', limit.toString());
    return this.api.get<PlaceDTO[]>('/api/places/nearby', params);
  }

  getWeatherRecommendations(latitude: number, longitude: number, limit = 10): Observable<PlaceDTO[]> {
    const params = new HttpParams()
      .set('latitude', latitude.toString())
      .set('longitude', longitude.toString())
      .set('limit', limit.toString());
    return this.api.get<PlaceDTO[]>('/api/places/recommendations/weather', params);
  }

  getPlaceById(id: number): Observable<PlaceDTO> {
    return this.api.get<PlaceDTO>(`/api/places/${id}`);
  }

  getTrendingPlaces(limit = 10): Observable<PlaceDTO[]> {
    const params = new HttpParams()
      .set('limit', limit.toString());
    return this.api.get<PlaceDTO[]>('/api/places/trending', params);
  }
}
