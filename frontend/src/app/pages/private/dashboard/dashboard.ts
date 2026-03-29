import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToolbarModule } from 'primeng/toolbar';
import { TagModule } from 'primeng/tag';
import { SelectModule } from 'primeng/select';

import { AnimaleService } from '../../../services/animali/animale.service';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';
import { RadioButtonModule } from 'primeng/radiobutton';
import { TextareaModule } from 'primeng/textarea';
import { SelectButtonModule } from 'primeng/selectbutton'; //
import { InputGroupModule } from 'primeng/inputgroup';
import { InputMaskModule } from 'primeng/inputmask';
import { ToastModule } from 'primeng/toast';
import { Router } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, FormsModule, TableModule, ButtonModule, 
    InputTextModule, ToolbarModule, TagModule, SelectModule, DialogModule,
    DatePickerModule,RadioButtonModule, TextareaModule,  MessageModule,
    InputNumberModule,SelectButtonModule, InputGroupModule, InputMaskModule,ToastModule,ConfirmDialogModule  
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit { // Nome standard

  displayNuovoDialog: boolean = false;
  erroriValidazione: string[] = [];
  // Modello per il nuovo animale (inizializzato vuoto)
  nuovoAnimale: any = {
    id_tipo_animale: '',
    nome: '',
    dt_nascita: null,
    num_microchip: '',
    id_colore: '',
    peso: null,
    sesso: '',
    id_razza: '',
    tipo_razza: 'Pura', // Valore di default
    caratteristiche: ''
  };

  animali: any[] = [];
  loading: boolean = false;
  totalRecords: number = 0;
  rows: number = 10;
  mostraFiltriAvanzati: boolean = false;

  filtri = {
    search: '',
    sesso: '',
    eta: '',
    stato: '',
    tipoAnimale: '',
    regione: '',
    provincia: '',
    tag: '',
    vaccini: '',
    limit: '0',
    offset: '10'
  };

  // Opzioni per le p-select
  opzioniTipo = [{ label: 'Tutti', value: '' }, { label: 'Gatto', value: '2' }, { label: 'Cane', value: '1' }];
  opzioniSesso = [{ label: 'Indifferente', value: '' }, { label: 'Maschio', value: 'M' }, { label: 'Femmina', value: 'F' }];
  opzioniEta = [
      { label: 'Tutte', value: null },
      { label: 'CUCCIOLO', value: 'CUCCIOLO' },
      { label: 'ADULTO GIOVANE', value: 'ADULTO GIOVANE' },
      { label: 'ADULTO', value: 'ADULTO' },
      { label: 'ANZIANO', value: 'ANZIANO' }
    ];

  opzioniVaccini = [
    { label: 'Nessuno', value: null },
    { label: '1', value: '1' },
    { label: '2', value: '2' },
    { label: '>2', value: '>2' }
  ];
  // Geo (Placeholder)
  nazioni = [{ label: 'Italia', value: 'IT' }, { label: 'Svizzera', value: 'CH' }];
  // Metadata dal DB
  listaTags: any[] = [];
  listaStati: any[] = [];
  regioni: any[] = [];
  province: any[] = [];
  idRegioneSelezionata: string | null = null;
  idProvinciaSelezionata: string | null = null;
  listaColori: any[] = [];
  listaRazze: any[] = [];

  oggi: Date = new Date();

  constructor(
    private animaleService: AnimaleService, 
    private cd: ChangeDetectorRef,
    private messageService: MessageService,
    private router: Router,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit() {
    // Carichiamo SOLO le tabelle di decodifica. 
    // La tabella si caricherà da sola tramite onLazyLoad!
    this.caricaMetadata();
  }

  onLazyLoad(event: TableLazyLoadEvent) {
    this.filtri.limit = (event.first ?? 0).toString();
    this.filtri.offset = (event.rows ?? 10).toString();
    this.eseguiRicerca();
  }

  eseguiRicerca() {
    this.loading = true;
    this.animaleService.cercaCompleta(this.filtri).subscribe({
      next: (res: any) => {
        this.animali = res.animali;
        this.totalRecords = res.totale;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error("Errore ricerca:", err);
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  caricaMetadata() {
    this.animaleService.getTags().subscribe(data => this.listaTags = data);
    this.animaleService.getStati().subscribe(data => this.listaStati = data);
    this.animaleService.getRegioni().subscribe(data => this.regioni = data);
    this.animaleService.getColori().subscribe(data => { this.listaColori = data; this.cd.detectChanges();});
    this.animaleService.getRazze().subscribe(data => { this.listaRazze = data; this.cd.detectChanges();});
  }

  onRegioneChange(event: any) {
    const idRegione = event.value;
    this.filtri.provincia = ''; // Reset filtro provincia
    this.province = [];
    if (idRegione) {
      this.animaleService.getProvince(idRegione).subscribe(data => this.province = data);
    }
  }

  toggleFiltri() {
    this.mostraFiltriAvanzati = !this.mostraFiltriAvanzati;
  }

  resetFiltri() {
    this.filtri = { search: '', sesso: '', eta: '', stato: '', tipoAnimale: '', regione: '', provincia: '', tag: '', vaccini: '', limit: '0', offset: '10' };
    this.eseguiRicerca();
  }

  apriNuovo() {
    this.nuovoAnimale = { sesso: '', id_tipo_animale: '', dt_nascita: null }; // Reset
    this.displayNuovoDialog = true;
  }

  validazioneForm(): boolean {
    this.erroriValidazione = [];
    if (!this.nuovoAnimale.id_tipo_animale) this.erroriValidazione.push("Il tipo di animale è un dato obbligatorio!");
    if (!this.nuovoAnimale.nome) this.erroriValidazione.push("Il nome è un dato obbligatorio!");
    if (!this.nuovoAnimale.sesso) this.erroriValidazione.push("Il sesso è un dato obbligatorio!");
    if (!this.nuovoAnimale.dt_nascita) {
        this.erroriValidazione.push("La data di nascita è un dato obbligatorio!");
    } else {
        // 2. Controllo data futura
        // Convertiamo in oggetto Date se non lo è già
        const dataNascita = new Date(this.nuovoAnimale.dt_nascita);
        
        if (dataNascita > this.oggi) {
            this.erroriValidazione.push("La data di nascita non può essere nel futuro!");
        }
    }
    // Controllo specifico per il Microchip
    if (this.nuovoAnimale.num_microchip) {
        // Rimuoviamo eventuali spazi bianchi
        const mc = this.nuovoAnimale.num_microchip.trim();
        
        if (mc.length > 0 && mc.length !== 15) {
            this.erroriValidazione.push("Il microchip deve essere composto esattamente da 15 cifre!");
        }
        
        // Controllo se sono tutti numeri (nel caso di copia-incolla)
        if (mc.length > 0 && !/^\d+$/.test(mc)) {
            this.erroriValidazione.push("Il microchip può contenere solo numeri!");
        }
    }
    
    return this.erroriValidazione.length === 0;
  }

  salvaNuovo() {
    if (this.validazioneForm()) {
      this.loading = true;
      const payload = { ...this.nuovoAnimale };
      if (payload.dt_nascita instanceof Date) {
        const d = payload.dt_nascita;
        console.log("Data di nascita selezionata:", d);
        // Usiamo i metodi "get" locali (non UTC) per evitare il problema delle 23:00
        const giorno = String(d.getDate()).padStart(2, '0');
        const mese = String(d.getMonth() + 1).padStart(2, '0');
        const anno = d.getFullYear();
        
        payload.dt_nascita = `${giorno}/${mese}/${anno}`; // Risultato: "01/02/2026"
        console.log("Data di nascita selezionata formattata:", payload.dt_nascita);
      }
      this.animaleService.saveAnimale(payload).subscribe({
        next: (res) => {
          
          this.displayNuovoDialog = false;
          this.eseguiRicerca(); // Ricarica la tabella per vedere il nuovo inserimento
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Operazione Completata', 
            detail: 'L\'animale è stato registrato con successo!',
            life: 3000 // Scompare dopo 3 secondi
          });
          this.loading = false;

        },
        error: (err) => {
          console.error("Errore salvataggio:", err);
          this.messageService.add({ 
            severity: 'error', 
            summary: 'Errore di Sistema', 
            detail: 'Non è stato possibile salvare i dati. Riprova più tardi.',
            life: 5000 
          });
          this.loading = false;
        }
      });
    }
  }

  resetForm() {
    this.nuovoAnimale = { tipo_razza: 'Pura', sesso: '' };
    this.erroriValidazione = [];
  }

  // ... dentro DashboardComponent

calcolaEtaDallaData(dataSelezionata: Date) {
    if (!dataSelezionata) return;

    const oggi = new Date();
    // Calcolo dei mesi di differenza
    let mesi = (oggi.getFullYear() - dataSelezionata.getFullYear()) * 12;
    mesi += oggi.getMonth() - dataSelezionata.getMonth();

    // Se il giorno del mese corrente è precedente al giorno di nascita, togliamo un mese
    if (oggi.getDate() < dataSelezionata.getDate()) {
        mesi--;
    }

    // Assegnazione categoria basata sulla logica del Database
    if (mesi <= 11) {
        this.nuovoAnimale.eta = 'CUCCIOLO';
    } else if (mesi <= 60) {
        this.nuovoAnimale.eta = 'ADULTO GIOVANE';
    } else if (mesi <= 120) {
        this.nuovoAnimale.eta = 'ADULTO';
    } else {
        this.nuovoAnimale.eta = 'ANZIANO';
    }

    this.cd.detectChanges(); // Forza l'aggiornamento della UI
}
  vaiAlDettaglio(id: string) {
    // Naviga verso l'URL configurato nel routing passando l'ID
    this.router.navigate(['/animali/dettaglio', id]);
  }

  confermaEliminazione(animale: any) {
  this.confirmationService.confirm({
    message: `Sei sicuro di voler eliminare <strong>${animale.nome}</strong>? L'operazione è irreversibile.`,
    header: 'Conferma Eliminazione',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Sì, elimina',
    rejectLabel: 'Annulla',
    acceptButtonStyleClass: 'p-button-danger',
    rejectButtonStyleClass: 'p-button-text',
    accept: () => {
      this.animaleService.eliminaAnimale(animale.id_animale).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Eliminato',
            detail: `${animale.nome} è stato eliminato con successo.`,
            life: 3000
          });
          this.eseguiRicerca(); // ricarica la tabella
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Errore',
            detail: 'Non è stato possibile eliminare l\'animale.',
            life: 5000
          });
        }
      });
    }
  });
}


}