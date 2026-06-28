import { Injectable } from '@angular/core';

export interface CarouselImage {
  sourceUrl?: string;
  downloadUrl?: string;
  isPrimary?: boolean;
}

export interface ImageCarouselState {
  currentIndex: number;
  totalImages: number;
  hasMultipleImages: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ImageCarouselService {

  /**
   * Combines primary image with images array, ensuring primary comes first
   * and avoiding duplicates
   */
  getAllImages(primarySourceUrl?: string, primaryDownloadUrl?: string, images?: any[]): CarouselImage[] {
    const allImages: CarouselImage[] = [];

    // Add primary image first if it exists
    if (primarySourceUrl || primaryDownloadUrl) {
      allImages.push({
        sourceUrl: primarySourceUrl,
        downloadUrl: primaryDownloadUrl,
        isPrimary: true
      });
    }

    // Add other images from the images array, excluding the primary if it's already included
    if (images) {
      for (const image of images) {
        const imageUrl = image.sourceUrl || image.downloadUrl;
        const primaryUrl = primarySourceUrl || primaryDownloadUrl;

        // Skip if this image is the same as the primary image
        if (imageUrl && primaryUrl && imageUrl === primaryUrl) {
          continue;
        }

        allImages.push({
          ...image,
          isPrimary: false
        });
      }
    }

    return allImages;
  }

  /**
   * Gets the current image URL from the carousel
   */
  getCurrentImageUrl(images: CarouselImage[], currentIndex: number): string {
    if (images.length === 0 || currentIndex < 0 || currentIndex >= images.length) {
      return '';
    }

    const currentImage = images[currentIndex];
    return currentImage.sourceUrl || currentImage.downloadUrl || '';
  }

  /**
   * Gets the carousel state
   */
  getCarouselState(images: CarouselImage[], currentIndex: number): ImageCarouselState {
    return {
      currentIndex,
      totalImages: images.length,
      hasMultipleImages: images.length > 1
    };
  }

  /**
   * Navigates to the next image
   */
  nextImage(currentIndex: number, totalImages: number): number {
    if (totalImages <= 1) return currentIndex;
    return (currentIndex + 1) % totalImages;
  }

  /**
   * Navigates to the previous image
   */
  previousImage(currentIndex: number, totalImages: number): number {
    if (totalImages <= 1) return currentIndex;
    return currentIndex === 0 ? totalImages - 1 : currentIndex - 1;
  }

  /**
   * Goes to a specific image index
   */
  goToImage(index: number, totalImages: number): number {
    if (index < 0 || index >= totalImages) return 0;
    return index;
  }
}