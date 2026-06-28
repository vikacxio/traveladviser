import { Directive, ElementRef, Input, Output, EventEmitter, OnInit, OnDestroy, HostListener, Renderer2, OnChanges, SimpleChanges } from '@angular/core';
import { ImageCarouselService, CarouselImage } from '../../core/image-carousel.service';

@Directive({
  selector: '[appImageCarousel]',
  standalone: true
})
export class ImageCarouselDirective implements OnInit, OnDestroy, OnChanges {
  @Input() images: CarouselImage[] = [];
  @Input() currentIndex = 0;
  @Input() showArrows = true;
  @Input() showIndicators = true;
  @Input() showCounter = false;
  @Input() arrowSize: 'sm' | 'md' | 'lg' = 'md';
  @Input() indicatorPosition: 'bottom' | 'top' = 'bottom';

  @Output() currentIndexChange = new EventEmitter<number>();

  private carouselContainer?: HTMLElement;
  private counter?: HTMLElement;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    private carouselService: ImageCarouselService
  ) {}

  ngOnInit() {
    this.renderCarousel();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['images'] || changes['showArrows'] || changes['showCounter']) {
      this.renderCarousel();
    }

    if (changes['currentIndex']) {
      this.updateCounter();
    }
  }

  ngOnDestroy() {
    this.clearCarousel();
  }

  @HostListener('window:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent) {
    if (this.images.length <= 1) return;

    if (event.key === 'ArrowLeft') {
      event.preventDefault();
      this.previousImage();
    } else if (event.key === 'ArrowRight') {
      event.preventDefault();
      this.nextImage();
    }
  }

  nextImage() {
    const newIndex = this.carouselService.nextImage(this.currentIndex, this.images.length);
    this.setCurrentIndex(newIndex);
  }

  previousImage() {
    const newIndex = this.carouselService.previousImage(this.currentIndex, this.images.length);
    this.setCurrentIndex(newIndex);
  }

  private setCurrentIndex(index: number) {
    this.currentIndex = index;
    this.currentIndexChange.emit(index);
    this.updateCounter();
  }

  private renderCarousel() {
    this.clearCarousel();

    if (this.images.length <= 1) return;

    this.carouselContainer = this.renderer.createElement('div');
    this.renderer.addClass(this.carouselContainer, 'absolute');
    this.renderer.addClass(this.carouselContainer, 'inset-0');
    this.renderer.addClass(this.carouselContainer, 'pointer-events-none');

    if (this.showArrows) {
      this.addNavigationArrows();
    }

    if (this.showCounter) {
      this.addCounter();
    }

    this.renderer.appendChild(this.el.nativeElement, this.carouselContainer);
  }

  private addNavigationArrows() {
    const arrowBaseClasses = [
      'absolute',
      'top-1/2',
      '-translate-y-1/2',
      'rounded-full',
      'text-white',
      'bg-black/50',
      'p-2',
      'transition-colors',
      'hover:bg-black/70',
      'pointer-events-auto',
      'z-20'
    ];

    const createArrow = (direction: 'previous' | 'next', iconPath: string) => {
      const arrow = this.renderer.createElement('button');
      arrow.className = arrowBaseClasses.join(' ');
      this.renderer.addClass(arrow, direction === 'previous' ? 'left-2' : 'right-2');
      this.renderer.setAttribute(arrow, 'aria-label', `${direction} image`);

      const icon = this.renderer.createElement('svg');
      this.renderer.addClass(icon, 'w-4');
      this.renderer.addClass(icon, 'h-4');
      this.renderer.setAttribute(icon, 'fill', 'none');
      this.renderer.setAttribute(icon, 'stroke', 'currentColor');
      this.renderer.setAttribute(icon, 'viewBox', '0 0 24 24');

      const path = this.renderer.createElement('path');
      this.renderer.setAttribute(path, 'stroke-linecap', 'round');
      this.renderer.setAttribute(path, 'stroke-linejoin', 'round');
      this.renderer.setAttribute(path, 'stroke-width', '2');
      this.renderer.setAttribute(path, 'd', iconPath);
      this.renderer.appendChild(icon, path);
      this.renderer.appendChild(arrow, icon);

      return arrow;
    };

    const prevArrow = createArrow('previous', 'M15 19l-7-7 7-7');
    this.renderer.listen(prevArrow, 'click', (event) => {
      event.stopPropagation();
      this.previousImage();
    });

    const nextArrow = createArrow('next', 'M9 5l7 7-7 7');
    this.renderer.listen(nextArrow, 'click', (event) => {
      event.stopPropagation();
      this.nextImage();
    });

    this.renderer.appendChild(this.carouselContainer, prevArrow);
    this.renderer.appendChild(this.carouselContainer, nextArrow);
  }

  private addCounter() {
    this.counter = this.renderer.createElement('div');
    this.renderer.addClass(this.counter, 'absolute');
    this.renderer.addClass(this.counter, 'bottom-2');
    this.renderer.addClass(this.counter, 'right-2');
    this.renderer.addClass(this.counter, 'rounded-full');
    this.renderer.addClass(this.counter, 'bg-black/50');
    this.renderer.addClass(this.counter, 'px-2');
    this.renderer.addClass(this.counter, 'py-1');
    this.renderer.addClass(this.counter, 'text-xs');
    this.renderer.addClass(this.counter, 'text-white');
    this.renderer.addClass(this.counter, 'pointer-events-none');
    this.renderer.addClass(this.counter, 'z-20');

    this.updateCounter();
    this.renderer.appendChild(this.carouselContainer, this.counter);
  }

  private updateCounter() {
    if (this.counter) {
      this.renderer.setProperty(this.counter, 'textContent', `${this.currentIndex + 1}/${this.images.length}`);
    }
  }

  private clearCarousel() {
    if (this.carouselContainer) {
      this.renderer.removeChild(this.el.nativeElement, this.carouselContainer);
      this.carouselContainer = undefined;
      this.counter = undefined;
    }
  }
}