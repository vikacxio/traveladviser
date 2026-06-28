export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  name: string;
  message: string;
  userId: number;
}

export interface PlaceImageDTO {
  id: number;
  contentType?: string;
  sourceUrl?: string;
  downloadUrl?: string;
}

export interface PlaceDTO {
  id: number;
  name: string;
  description: string;
  categoryName?: string;
  latitude: number;
  longitude: number;
  avgRating?: number;
  totalRatings?: number;
  priceLevel?: number;
  bestSeason?: string;
  city?: string;
  state?: string;
  country?: string;
  tags?: string[];
  primaryImageSourceUrl?: string;
  primaryImageDownloadUrl?: string;
  images?: PlaceImageDTO[];
}

export interface SearchPlacesRequest {
  city?: string;
  category?: string;
  budget?: number;
  minRating?: number;
  limit?: number;
}
