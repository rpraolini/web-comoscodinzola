import { ChangeDetectorRef, Component, ElementRef, Input, OnInit, ViewChild } from "@angular/core";
import { Animale, AssoFile } from "../../../../../models/animale.model";
import { AnimaleService } from "../../../../../services/animali/animale.service";
import { DettaglioStateService } from "../../dettaglio-animale/DettaglioStateService";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { InputTextModule } from "primeng/inputtext";
import { SelectModule } from "primeng/select";
import { InputMaskModule } from "primeng/inputmask";
import { ButtonModule } from "primeng/button";
import { DatePickerModule } from "primeng/datepicker";
import { InputNumberModule } from "primeng/inputnumber";
import { RadioButtonModule } from "primeng/radiobutton";
import { TextareaModule } from "primeng/textarea";
import { AvatarModule } from "primeng/avatar";
import { FileUploadModule } from 'primeng/fileupload';
import { ProgressSpinnerModule } from "primeng/progressspinner";
import { ChipModule } from 'primeng/chip';
import { DocumentiService } from "../../../../../services/documenti/documento.service";
import { ConfirmationService, MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { ConfirmDialogModule } from "primeng/confirmdialog";
import { DialogModule } from "primeng/dialog";
import { TooltipModule } from "primeng/tooltip";
import { Router } from "@angular/router";


@Component({
  selector: 'app-anagrafica',
  standalone: true, // Fondamentale per le versioni recenti
  imports: [
    CommonModule,
    FormsModule,
    // Moduli PrimeNG necessari per l'HTML
    ButtonModule,
    InputTextModule,
    SelectModule,
    DatePickerModule,
    InputNumberModule,
    InputMaskModule,
    RadioButtonModule,
    TextareaModule,
    AvatarModule,
    FileUploadModule,
    ProgressSpinnerModule,
    ChipModule,
    ToastModule,
    ConfirmDialogModule,
    DialogModule,
    TooltipModule,
  ],
  templateUrl: './anagrafica.html',
  styleUrls: ['./anagrafica.css'],
  providers: [ConfirmationService, MessageService]
})
export class AnagraficaComponent implements OnInit {
  @Input() animale: Animale = new Animale();

  // Liste reali dal DB
  listaColori: any[] = [];
  listaRazze: any[] = [];
  listaTags: any[] = [];
  listaTipiDoc: any[] = [];
  listaTipi = [{ label: 'Tutti', value: '' }, { label: 'Gatto', value: '2' }, { label: 'Cane', value: '1' }];

  tagSelezionata: string | null = null;
  documentoSelezionato: any = null;
  fileDaCaricare: File | null = null;
  @ViewChild('docFileInput') docFileInput!: ElementRef<HTMLInputElement>;

  isDragging: boolean = false;
  displayDecesso: boolean = false;
  dataDecesso: Date | null = null;

  constructor(
    private stateService: DettaglioStateService,
    private animaleService: AnimaleService,
    private documentiService: DocumentiService,
    private cd: ChangeDetectorRef,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // 1. Carichiamo le decodifiche
    this.caricaDecodifiche();

    // 2. Ci iscriviamo ai dati dell'animale che arrivano dal Parent
    this.stateService.animale$.subscribe(data => {
      if (data) {
        this.animale = data;
        // Gestione data per PrimeNG
        if (typeof this.animale.dt_nascita === 'string') {
          const p = this.animale.dt_nascita.split('/');
        }
        this.cd.detectChanges();
      }
    });
  }

  caricaDecodifiche() {
    this.animaleService.getColori().subscribe(res => this.listaColori = res);
    this.animaleService.getRazze().subscribe(res => this.listaRazze = res);
    this.animaleService.getTags().subscribe(res => this.listaTags = res);
    this.documentiService.getTipiDoc('A').subscribe(res => this.listaTipiDoc = res);
  }

  onTipoChange() {
    if (this.animale.id_tipo_animale) {
      this.animaleService.getRazze()
        .subscribe(res => this.listaRazze = res);
    }
  }

  onChanged() {
    this.stateService.markAsDirty();
  }

  refreshAnimale() {
    // Chiamiamo il service per recuperare i dati aggiornati dal backend
    this.animaleService.getById(this.animale.id_animale).subscribe({
      next: (updatedAnimale) => {
        // 1. Aggiorniamo l'oggetto locale del componente
        this.animale = updatedAnimale;

        // 2. Notifichiamo lo Stato Globale (DettaglioStateService)
        // Questo assicura che anche la Breadcrumb e l'Header vedano i cambiamenti
        this.stateService.setAnimale(updatedAnimale);
        this.cd.detectChanges();
        console.log('Scheda animale sincronizzata con successo');
      },
      error: (err) => {
        console.error('Errore durante il refresh dell\'animale', err);
      }
    });
  }

  assegnaTag() {
    if (this.tagSelezionata) {
      // Chiamata al service per salvare la relazione sul DB
      this.animaleService.assignTag(this.animale.id_animale, this.tagSelezionata).subscribe(() => {
        // Aggiorna la lista locale dopo il salvataggio
        this.refreshAnimale();
        this.tagSelezionata = null;
        this.cd.detectChanges();
      });
    }
  }

  rimuoviTag(idTag: string) {
    // Chiamata al service per eliminare la relazione
    this.animaleService.removeTag(this.animale.id_animale, idTag).subscribe(() => {
      this.refreshAnimale();
    });
  }

  onFileSelect(event: any) {
    this.fileDaCaricare = event.files[0];
  }

  caricaDocumento() {
    if (this.documentoSelezionato && this.fileDaCaricare) {
      this.documentiService.saveDocumentoAnimale(
        this.animale.id_animale,
        this.documentoSelezionato,
        this.fileDaCaricare
      ).subscribe({
        next: () => {
          this.fileDaCaricare = null;
          this.documentoSelezionato = null;
          if (this.docFileInput) this.docFileInput.nativeElement.value = '';
          this.refreshAnimale(); // Aggiorna la lista documenti
          console.log('Documento salvato con successo');
          this.cd.detectChanges();
        },
        error: (err) => console.error('Errore upload', err)
      });
    }
  }

  download(file?: AssoFile) { // Il punto di domanda permette 'undefined'
    if (!file || !file.id_file) {
      console.error("Nessun file disponibile per il download");
      return;
    }

    this.documentiService.downloadFile(file.id_file).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = file.filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
      },
      error: (err) => console.error('Errore download', err)
    });
  }

  eliminaDocumento(idDoc: string): void {
    this.confirmationService.confirm({
      message: 'Sei sicuro di voler eliminare definitivamente questo documento?',
      header: 'Conferma Eliminazione',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        // Logica di eliminazione
        this.documentiService.deleteDocumento(idDoc).subscribe({
          next: () => {
            this.refreshAnimale();
            this.messageService.add({
              severity: 'success',
              summary: 'Eliminato',
              detail: 'Documento rimosso con successo'
            });
            this.cd.detectChanges();
          },
          error: (err) => {
            console.error(err);
            this.messageService.add({
              severity: 'error',
              summary: 'Errore',
              detail: 'Impossibile eliminare il documento'
            });
          }
        });
      }
    });
  }


  onFileDrop(event: DragEvent) {
    event.preventDefault(); // Impedisce al browser di aprire il file
    this.isDragging = false;
    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      const file = event.dataTransfer.files[0];
      this.fileDaCaricare = file; // Assegna il file trascinato alla variabile
      console.log('File trascinato con successo:', file.name);
      this.cd.detectChanges();
    }
  }


  confermaDecesso(): void {
    if (!this.dataDecesso) {
        this.messageService.add({ 
            severity: 'warn', 
            summary: 'Attenzione', 
            detail: 'Inserire la data del decesso' 
        });
        return;
    }

    // Chiamata al service per aggiornare lo stato dell'animale
    this.animaleService.registraDecesso(this.animale.id_animale, this.dataDecesso).subscribe({
        next: () => {
            // 1. Chiudiamo la modale
            this.displayDecesso = false;
            
            // 2. Feedback all'utente
            this.messageService.add({ 
                severity: 'success', 
                summary: 'Registrato', 
                detail: 'Stato animale aggiornato correttamente' 
            });
            
            // 3. Ricarichiamo i dati dell'animale per "congelare" la scheda
            this.refreshAnimale();
        },
        error: (err) => {
            console.error('Errore registrazione decesso:', err);
            this.messageService.add({ 
                severity: 'error', 
                summary: 'Errore', 
                detail: 'Impossibile registrare il decesso a sistema' 
            });
        }
    });
}

salva(): void {
  this.animaleService.updateAnimale(this.animale.id_animale, this.animale).subscribe({
    next: () => {
      this.messageService.add({
        severity: 'success',
        summary: 'Salvato',
        detail: 'Scheda aggiornata con successo'
      });
      this.stateService.setClean(); // ← nome corretto dal tuo service
      this.refreshAnimale();
    },
    error: (err) => {
      const msg = err.error?.errore || 'Errore durante il salvataggio';
      this.messageService.add({
        severity: 'error',
        summary: 'Errore',
        detail: msg
      });
    }
  });
}

get isDeceduto(): boolean {
  return this.animale?.id_stato === '99';
}

elimina(): void {
  this.confirmationService.confirm({
    message: `Sei sicuro di voler eliminare definitivamente <strong>${this.animale.nome}</strong>? L'operazione non è reversibile.`,
    header: 'Conferma Eliminazione',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Sì, elimina',
    rejectLabel: 'Annulla',
    acceptButtonStyleClass: 'p-button-danger',
    rejectButtonStyleClass: 'p-button-text',
    accept: () => {
      this.animaleService.eliminaAnimale(this.animale.id_animale).subscribe({
        next: () => {
          this.router.navigate(['/animali']); // ← adatta al tuo path
        },
        error: (err) => {
          const msg = err.error?.errore || 'Impossibile eliminare l\'animale';
          this.messageService.add({
            severity: 'error',
            summary: 'Errore',
            detail: msg
          });
        }
      });
    }
  });
}


duplica(): void {
  this.confirmationService.confirm({
    message: `Vuoi duplicare la scheda di <strong>${this.animale.nome}</strong>?`,
    header: 'Conferma Duplicazione',
    icon: 'pi pi-copy',
    acceptLabel: 'Sì, duplica',
    rejectLabel: 'Annulla',
    rejectButtonStyleClass: 'p-button-text',
    accept: () => {
      // Costruiamo il nuovo animale copiando solo i campi desiderati
      const duplicato = {
        nome: this.animale.nome,
        dt_nascita: this.animale.dt_nascita,
        sesso: this.animale.sesso,
        id_razza: this.animale.id_razza,
        tipo_razza: this.animale.tipo_razza,
        id_colore: this.animale.id_colore,
        peso: this.animale.peso,
        num_microchip: null, // il microchip deve essere unico, azzeriamo
        caratteristiche: this.animale.caratteristiche,
        id_tipo_animale: this.animale.id_tipo_animale
        // id_animale assente: il backend lo tratta come nuovo inserimento
      };

      this.animaleService.saveAnimale(duplicato).subscribe({
        next: (res) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Duplicato',
            detail: 'Nuova scheda creata con successo'
          });
          // Navighiamo alla scheda del nuovo animale
          setTimeout(() => {
            this.router.navigate(['/animali', res.id]); // ← adatta al tuo path
          }, 500);
        },
        error: (err) => {
          const msg = err.error?.errore || 'Impossibile duplicare l\'animale';
          this.messageService.add({
            severity: 'error',
            summary: 'Errore',
            detail: msg
          });
        }
      });
    }
  });
}

reset(): void {
  this.confirmationService.confirm({
    message: 'Vuoi annullare le modifiche e ripristinare i dati salvati?',
    header: 'Conferma Reset',
    icon: 'pi pi-refresh',
    acceptLabel: 'Sì, ripristina',
    rejectLabel: 'Annulla',
    rejectButtonStyleClass: 'p-button-text',
    accept: () => {
      this.refreshAnimale();
      this.stateService.setClean();
      this.messageService.add({
        severity: 'info',
        summary: 'Ripristinato',
        detail: 'I dati sono stati ripristinati all\'ultimo salvataggio'
      });
    }
  });
}

valida(): void {
  this.confirmationService.confirm({
    message: `Vuoi validare i dati di <strong>${this.animale.nome}</strong>?`,
    header: 'Conferma Validazione',
    icon: 'pi pi-check-circle',
    acceptLabel: 'Sì, valida',
    rejectLabel: 'Annulla',
    rejectButtonStyleClass: 'p-button-text',
    accept: () => {
      this.animaleService.validaAnimale(this.animale.id_animale).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Validato',
            detail: 'Dati validati con successo'
          });
          this.refreshAnimale();
        },
        error: (err) => {
          this.messageService.add({
            severity: 'warn',
            summary: 'Attenzione',
            detail: err.error?.errore || 'Impossibile validare'
          });
        }
      });
    }
  });
}

get isValidabile(): boolean {
  return this.animale?.id_stato === '1';
}


}