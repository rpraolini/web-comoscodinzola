import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; // Per usare routerLink

// Moduli PrimeNG
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { CarouselModule } from 'primeng/carousel';
import { TagModule } from 'primeng/tag';

import { PublicHeaderComponent } from '../../layout/public-header/public-header';
import { TenantService } from '../../services/tenant';
import { AdoptionService } from '../../services/animali/adoption.service';
import { Animale } from '../../models/animale.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, ToolbarModule, ButtonModule,PublicHeaderComponent, CarouselModule, TagModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent {
  tenantService = inject(TenantService);
  logoImageUrl = 'assets/' + this.tenantService.currentTenant + '/header.jpg';


  animali: Animale[] = [];
  loading: boolean = true;
  
  // Configurazione Responsive del Carosello
  responsiveOptions = [
    { breakpoint: '1199px', numVisible: 3, numScroll: 1 },
    { breakpoint: '991px', numVisible: 2, numScroll: 1 },
    { breakpoint: '767px', numVisible: 1, numScroll: 1 }
  ];

  constructor(private adoptionService: AdoptionService, private cd: ChangeDetectorRef) {}

  ngOnInit() {
    this.adoptionService.getRandomAnimals().subscribe({
      next: (data) => {
        this.animali = data;
        this.loading = false;
        this.cd.detectChanges();
        //console.log("Animali caricati:", data);
      },
      error: (err) => {
        //console.error("Errore caricamento random:", err);
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }
}

