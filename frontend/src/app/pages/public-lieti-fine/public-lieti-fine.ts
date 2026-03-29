import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdoptionService } from '../../services/animali/adoption.service';
import { Animale } from '../../models/animale.model';
import { PublicHeaderComponent } from '../../layout/public-header/public-header';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-public-lieti-fine',
  standalone: true,
  imports: [CommonModule, RouterModule, PublicHeaderComponent, ProgressSpinnerModule],
  templateUrl: './public-lieti-fine.html'
})
export class PublicLietiFineComponent implements OnInit {

  totaleAdozioni: string = '0';
  statisticheAnni: any[] = [];
  animali: Animale[] = [];
  annoSelezionato: string = '';
  loading: boolean = false;

  constructor(private adoptionService: AdoptionService, private cd: ChangeDetectorRef) {}

  ngOnInit() {
    this.caricaDatiIniziali();
  }

  caricaDatiIniziali() {
    // 1. Carica il numero totale magico!
    this.adoptionService.getLietiFineCount().subscribe({
      next: (totale) => {
        this.totaleAdozioni = totale;
        this.cd.detectChanges();
      }
    });

    // 2. Carica gli anni e i relativi conteggi
    this.adoptionService.getLietiFineCountByAnno().subscribe({
      next: (stats) => {
        this.statisticheAnni = stats;
        
        if (this.statisticheAnni.length > 0) {
          // value_2 è l'anno. Prendiamo l'ultimo elemento dell'array (il più recente)
          const annoPiuRecente = this.statisticheAnni[this.statisticheAnni.length - 1].value_2; 
          this.selezionaAnno(annoPiuRecente);
        }
      }
    });
  }

  // Metodo richiamato al click su un anno
  selezionaAnno(anno: string) {
    this.annoSelezionato = anno;
    this.loading = true;
    
    this.adoptionService.getLietiFine(anno).subscribe({
      next: (data) => {
        this.animali = data;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error("Errore nel caricamento adozioni", err);
        this.loading = false;
      }
    });
  }

  // Helper per mostrare il testo corretto sulla foto
  getTestoAdozione(item: Animale): string {
    const nome = item.nome || 'Amico';
    const sesso = item.sesso === 'F' ? 'stata adottata' : 'stato adottato';
    const luogo = item.location ? ` a ${item.location}` : '';
    
    return `... e sono ${sesso}${luogo}.`;
  }

  mostraModaleFoto: boolean = false;
  fotoIngrandita: string = '';

  // Metodo per aprire la foto
  apriFoto(url: string | undefined) {
      if (url) {
          this.fotoIngrandita = url;
          this.mostraModaleFoto = true;
      }
  }

  // Metodo per chiudere la foto
  chiudiFoto() {
      this.mostraModaleFoto = false;
      this.fotoIngrandita = '';
  }
}