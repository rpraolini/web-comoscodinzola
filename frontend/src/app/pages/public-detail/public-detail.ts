import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs'; // <--- IMPORTANTE


import { Animale, Carattere, Foto } from '../../models/animale.model';
import { PublicHeaderComponent } from '../../layout/public-header/public-header';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { GalleriaModule } from 'primeng/galleria'; // <--- NUOVO PER LE FOTO
import { TooltipModule } from 'primeng/tooltip';   // <--- NUOVO PER INFO
import { AdoptionService } from '../../services/animali/adoption.service';

@Component({
  selector: 'app-public-detail',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    TagModule,
    ProgressSpinnerModule,
    GalleriaModule,
    TooltipModule,
    PublicHeaderComponent
  ],
  templateUrl: './public-detail.html'
})
export class PublicDetailComponent implements OnInit {

  animale: Animale | null = null;
  caratteri: Carattere[] = [];
  galleryImages: Foto[] = [];
  
  // Variabile per la nota speciale (tipo 0)
  notaCarattere: string | null = null;

  loading: boolean = true;

  // Opzioni Galleria Responsive
  responsiveOptions: any[] = [
    { breakpoint: '1024px', numVisible: 5 },
    { breakpoint: '768px', numVisible: 3 },
    { breakpoint: '560px', numVisible: 1 }
  ];

  constructor(
    private route: ActivatedRoute,
    private adoptionService: AdoptionService,
    private cd: ChangeDetectorRef
  ) {}

  displayFullScreen: boolean = false;
  activeIndex: number = 0;
  fotoCorrente: any = null;



  selezionaFoto(fotoCliccata: any) {
      if (!fotoCliccata) return;
      const index = this.galleryImages.indexOf(fotoCliccata);
      if (index !== -1) {
          this.activeIndex = index; 
      }
  }

  openFullScreen() {
      this.displayFullScreen = true;
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.loading = true;

      // ESEGUIAMO LE 3 CHIAMATE IN PARALLELO
      forkJoin({
        animale: this.adoptionService.getAnimalById(id),
        caratteri: this.adoptionService.getCaratteri(id),
        foto: this.adoptionService.getFotoGallery(id)
      }).subscribe({
        next: (result) => {
          // 1. Animale
          this.animale = result.animale;

          // 2. Caratteri: Separiamo la "nota speciale" (tipo 0) dagli altri
          const notaObj = result.caratteri.find(c => c.id_tipo_carattere === '0');
          this.notaCarattere = notaObj ? notaObj.note || null : null;
          
          // Teniamo nella lista solo quelli "veri" (escludiamo tipo 0)
          this.caratteri = result.caratteri.filter(c => c.id_tipo_carattere !== '0');

          // 3. Foto: usiamo quelle della galleria; se la foto profilo non è già inclusa la aggiungiamo in testa
          this.galleryImages = result.foto || [];
          if (this.animale?.foto) {
            const giàPresente = this.galleryImages.some(f => f.id_foto === this.animale!.foto!.id_foto);
            if (!giàPresente) {
              this.galleryImages.unshift(this.animale.foto);
            } else {
              // Sposta la foto profilo in prima posizione
              const idx = this.galleryImages.findIndex(f => f.id_foto === this.animale!.foto!.id_foto);
              if (idx > 0) this.galleryImages.unshift(...this.galleryImages.splice(idx, 1));
            }
          }
          if (this.galleryImages.length > 0) {
              this.fotoCorrente = this.galleryImages[0];
          }

          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error("Errore caricamento dati:", err);
          this.loading = false;
        }
      });
    }
  }

  goBack() {
    window.history.back();
  }

  // Helper per trasformare "house.png" in un'icona PrimeNG
  getIconClass(iconName: string): string {
    if (!iconName) return 'pi pi-info-circle';
    if (iconName.includes('house')) return 'pi pi-home';
    if (iconName.includes('dog')) return 'pi pi-heart'; // O pi-github per un animale :)
    if (iconName.includes('cat')) return 'pi pi-star';
    if (iconName.includes('agility')) return 'pi pi-bolt';
    if (iconName.includes('family')) return 'pi pi-users';
    return 'pi pi-check-circle'; // Default
  }
}