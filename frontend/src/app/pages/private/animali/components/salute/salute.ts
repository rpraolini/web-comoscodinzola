import { Component, OnInit, ChangeDetectorRef, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { forkJoin } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { TextareaModule } from 'primeng/textarea';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { TagModule } from 'primeng/tag';
import { AccordionModule } from 'primeng/accordion';
import { SaluteService } from '../../../../../services/animali/salute.service';
import { DocumentiService } from '../../../../../services/documenti/documento.service';

@Component({
  selector: 'app-salute',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ButtonModule, DialogModule, SelectModule,
    DatePickerModule, TextareaModule, ToastModule, ConfirmDialogModule,
    TooltipModule, TagModule, AccordionModule
  ],
  templateUrl: './salute.html',
  providers: [ConfirmationService, MessageService]
})
export class Salute implements OnInit {

  idAnimale!: string;
  eventi: any[] = [];
  tipiEvento: any[] = [];
  categorie: any[] = [];
  tipiDocumento: any[] = [];

  displayDialog = false;
  isModifica = false;
  saving = false;

  fileDocumento: File | null = null;
  tipoDocSelezionato: any = null;
  uploadingDoc = false;

  eventoCorrente: any = this.nuovoEvento();

  @ViewChild('fileDocInput') fileDocInput!: ElementRef<HTMLInputElement>;


  constructor(
    private route: ActivatedRoute,
    private saluteService: SaluteService,
    private confirmationService: ConfirmationService,
    private documentiService: DocumentiService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.idAnimale = params['id'];
      this.caricaTutto();
    });
  }

  onFileDocChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.fileDocumento = input.files[0];
      this.cd.detectChanges();
    }
  }

  uploadDocumento(): void {
    if (!this.fileDocumento || !this.tipoDocSelezionato) return;
    this.uploadingDoc = true;
    this.saluteService.uploadDocumento(
      this.eventoCorrente.id_evento,
      this.idAnimale,
      this.tipoDocSelezionato,
      this.fileDocumento
    ).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Caricato', detail: 'Documento aggiunto' });
        this.fileDocumento = null;
        this.tipoDocSelezionato = null;
        this.uploadingDoc = false;
        this.fileDocInput.nativeElement.value = '';

        // Ricarica tutto e poi aggiorna anche eventoCorrente nella dialog
        this.saluteService.getEventi(this.idAnimale).subscribe(eventi => {
          this.eventi = eventi || [];
          // Aggiorna eventoCorrente con i dati freschi (inclusi i nuovi documenti)
          const aggiornato = this.eventi.find(e => e.id_evento === this.eventoCorrente.id_evento);
          if (aggiornato) {
            this.eventoCorrente = { ...aggiornato };
          }
          this.cd.detectChanges();
        });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Impossibile caricare il documento' });
        this.uploadingDoc = false;
      }
    });
  }

  downloadDocumento(file: any): void {
    if (!file?.id_file) return;
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
      error: () => this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Impossibile scaricare il documento' })
    });
  }

  caricaTutto(): void {
    forkJoin({
      eventi: this.saluteService.getEventi(this.idAnimale),
      tipi: this.saluteService.getTipi(),
      categorie: this.saluteService.getCategorie(),
      tipiDoc: this.saluteService.getTipiDocumento()
    }).subscribe(({ eventi, tipi, categorie, tipiDoc }) => {
      this.eventi = eventi || [];
      this.tipiEvento = tipi || [];
      this.categorie = categorie || [];
      this.tipiDocumento = tipiDoc || [];
      this.cd.detectChanges();
    });
  }

  nuovoEvento(): any {
    return {
      id_evento: null,
      id_animale: null,
      id_tipo_evento: null,
      dt_evento: null,
      dt_richiamo: null,
      note: ''
    };
  }

  // Restituisce gli eventi di una determinata categoria
  eventiPerCategoria(idCategoria: string): any[] {
    return this.eventi.filter(e => e.id_tipo_evento_clinico === idCategoria);
  }

  apriNuovo(): void {
    this.eventoCorrente = this.nuovoEvento();
    this.eventoCorrente.id_animale = this.idAnimale;
    this.isModifica = false;
    this.displayDialog = true;
  }

  apriModifica(evento: any): void {
    this.eventoCorrente = { ...evento };
    this.isModifica = true;
    this.displayDialog = true;
  }

  salva(): void {
    if (!this.eventoCorrente.id_tipo_evento || !this.eventoCorrente.dt_evento) {
      this.messageService.add({
        severity: 'warn', summary: 'Attenzione',
        detail: 'Tipo evento e data sono obbligatori'
      });
      return;
    }

    this.saving = true;
    const op$ = this.isModifica
      ? this.saluteService.update(this.eventoCorrente.id_evento, this.eventoCorrente)
      : this.saluteService.save(this.eventoCorrente);

    op$.subscribe({
      next: () => {
        this.saving = false;
        this.displayDialog = false;
        this.messageService.add({ severity: 'success', summary: 'Salvato', detail: 'Evento salvato con successo' });
        this.caricaTutto();
      },
      error: (err) => {
        this.saving = false;
        this.messageService.add({ severity: 'error', summary: 'Errore', detail: err.error?.errore || 'Impossibile salvare' });
      }
    });
  }

  elimina(evento: any): void {
    this.confirmationService.confirm({
      message: `Vuoi eliminare l'evento <strong>${evento.evento}</strong>?`,
      header: 'Conferma Eliminazione',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.saluteService.delete(evento.id_evento).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Eliminato', detail: 'Evento rimosso' });
            this.caricaTutto();
          }
        });
      }
    });
  }


  eliminaDocumento(idDocumento: string): void {
    this.confirmationService.confirm({
      message: 'Vuoi eliminare questo documento?',
      header: 'Conferma',
      icon: 'pi pi-trash',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.saluteService.deleteDocumento(idDocumento).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Eliminato', detail: 'Documento rimosso' });
            this.caricaTutto();
          }
        });
      }
    });
  }

}
