import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Service e Model

import { Animale } from '../../models/animale.model';

// PrimeNG Imports (v18)
import { DataViewModule } from 'primeng/dataview';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { SelectModule } from 'primeng/select'; 
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { RadioButtonModule } from 'primeng/radiobutton'; 
import { ToolbarModule } from 'primeng/toolbar';
import { PublicHeaderComponent } from '../../layout/public-header/public-header';
import { AdoptionService } from '../../services/animali/adoption.service';

@Component({
  selector: 'app-public-search',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    RouterModule,
    DataViewModule, 
    ButtonModule, 
    TagModule, 
    SelectModule, 
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    RadioButtonModule,
    ToolbarModule,
    PublicHeaderComponent
  ],
  templateUrl: './public-search.html',
  styleUrl: './public-search.css'
})
export class PublicSearchComponent implements OnInit {
  
  animali: Animale[] = [];
  loading: boolean = true;
  ayout: 'grid' = 'grid';
  
  // Filtri 
  filtri = {
    tipo: 'Tutti',
    sesso: 'Indifferente',
    taglia: null,
    eta: null,
    nazione: null,
    regione: null,
    provincia: null
  };

  // Opzioni Menu
  opzioniTipo = [
    { label: 'Gatto', value: '2' },
    { label: 'Cane', value: '1' },
    { label: 'Tutti', value: '' }
  ];

  opzioniSesso = [
    { label: 'Maschio', value: 'M' },
    { label: 'Femmina', value: 'F' },
    { label: 'Indifferente', value: '' }
  ];

  opzioniTaglia = [
    { label: 'Tutte', value: null },
    { label: 'MINI', value: 'MINI' },
    { label: 'PICCOLA', value: 'PICCOLA' },
    { label: 'MEDIO PICCOLA', value: 'MEDIO PICCOLA' },
    { label: 'MEDIA', value: 'MEDIA' },
    { label: 'MEDIO CONTENUTA', value: 'MEDIO CONTENUTA' },
    { label: 'MEDIA ABBONDANTE', value: 'MEDIA ABBONDANTE' },
    { label: 'GRANDE', value: 'GRANDE' }
  ];

  opzioniEta = [
    { label: 'Tutte', value: null },
    { label: 'CUCCIOLO', value: 'CUCCIOLO' },
    { label: 'ADULTO', value: 'ADULTO' },
    { label: 'ADULTO GIOVANE', value: 'ADULTO GIOVANE' },
    { label: 'ANZIANO', value: 'ANZIANO' }
  ];

  // Geo (Placeholder)
  nazioni = [{ label: 'Italia', value: 'IT' }, { label: 'Svizzera', value: 'CH' }];

  regioni: any[] = [];
  province: any[] = [];
  idRegioneSelezionata: string | null = null;
  idProvinciaSelezionata: string | null = null;

  constructor(private adoptionService: AdoptionService, private cd: ChangeDetectorRef) {}

  ngOnInit() {
    this.cerca(); // Carica i dati all'avvio
    this.caricaRegioni('IT');
  }

  caricaRegioni(nazione: string) {
    this.adoptionService.getRegioni(nazione).subscribe({
      next: (data) => {
        this.regioni = data;
      },
      error: (err) => console.error('Errore caricamento regioni', err)
    });
  }

  onNazioneChange(event: any) {
    const siglaNazione = event.value;

    // 1. Reset a cascata! Svuotiamo le selezioni e le liste sottostanti
    this.idRegioneSelezionata = null;
    this.regioni = [];
    this.idProvinciaSelezionata = null;
    this.province = [];

    // 2. Se ha selezionato una nazione valida, carichiamo le sue regioni
    if (siglaNazione) {
      this.caricaRegioni(siglaNazione);
    }
  }

  // Metodo scatenato quando l'utente sceglie una regione dalla tendina
  onRegioneChange(event: any) {
    // event.value contiene l'ID della regione selezionata
    const idRegione = event.value;

    // 1. Resettiamo la provincia selezionata e la lista delle province (UX fondamentale!)
    this.idProvinciaSelezionata = null;
    this.province = [];

    // 2. Se ha selezionato una regione valida, carichiamo le sue province
    if (idRegione) {
      this.adoptionService.getProvince(idRegione).subscribe({
        next: (data) => {
          this.province = data;
        },
        error: (err) => console.error('Errore caricamento province', err)
      });
    }
  }

cerca() {
    this.loading = true;

    // 1. PREPARAZIONE DEI PARAMETRI
    // Trasformiamo i valori "visivi" (es. 'Tutti') in valori "tecnici" per il Backend (es. stringa vuota)
    
    // Tipo: Se è 'Tutti' o null, inviamo stringa vuota
    const tipo = (this.filtri.tipo === 'Tutti' || !this.filtri.tipo) ? '' : this.filtri.tipo;
    
    // Sesso: Se è 'Indifferente' o null, inviamo stringa vuota
    const sesso = (this.filtri.sesso === 'Indifferente' || !this.filtri.sesso) ? '' : this.filtri.sesso;
    
    // Altri campi: Se sono null/undefined, inviamo stringa vuota
    const eta = this.filtri.eta || '';
    const taglia = this.filtri.taglia || '';
    
    // Geo: Se sono null, stringa vuota. 
    // (Nota: controlla se il tuo oggetto nazione/regione è un oggetto intero o solo il valore)
    // Se usi p-select con optionValue="value", qui avrai direttamente il valore (es. '15').
    const regione = this.idRegioneSelezionata || '';
    const provincia = this.idProvinciaSelezionata || '';

    console.log('Cerco con filtri:', { tipo, sesso, eta, taglia, regione, provincia }); // Debug utile

    // 2. CHIAMATA AL SERVICE
    this.adoptionService.ricercaAnimali(tipo, eta, taglia, sesso, regione, provincia)
      .subscribe({
        next: (data) => {
          this.animali = data;
          this.loading = false;
          console.log(`Trovati ${data.length} animali.`);
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error("Errore durante la ricerca:", err);
          this.loading = false;
          this.cd.detectChanges();
          // Opzionale: Mostra un messaggio utente con PrimeNG Toast
        }
      });
  }

  // Gestione colori badge in base allo stato reale del DB
  getSeverity(animale: Animale): "success" | "info" | "warn" | "danger" | "secondary" | "contrast" | undefined {
    // Il backend ci manda "descr_stato" (es. "Adottabile")
    const stato = animale.descr_stato ? animale.descr_stato.toLowerCase() : '';

    if (stato.includes('adottabile')) return 'success';
    if (stato.includes('smarrito')) return 'danger';
    if (stato.includes('stallo') || stato.includes('prenotato')) return 'warn';
    return 'info';
  }

  resetFiltri() {
      this.filtri = {
        tipo: 'Tutti',
        sesso: 'Indifferente',
        taglia: null,
        eta: null,
        nazione: null,
        regione: null,
        provincia: null
      };
      this.cerca();
  }
}