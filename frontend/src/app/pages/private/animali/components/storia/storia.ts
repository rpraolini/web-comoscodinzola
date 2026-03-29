import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { debounceTime, distinctUntilChanged, forkJoin, Subject, switchMap } from 'rxjs';

import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { TextareaModule } from 'primeng/textarea';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { TagModule } from 'primeng/tag';
import { StoriaService } from '../../../../../services/animali/storia.service';
import { InputNumberModule } from 'primeng/inputnumber';

import { AutoCompleteModule } from 'primeng/autocomplete';
import { ContattoService } from '../../../../../services/contatti/contatto.service';


@Component({
  selector: 'app-storia',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ButtonModule, DialogModule, SelectModule,
    DatePickerModule, TextareaModule, ToastModule, ConfirmDialogModule,
    TooltipModule, TagModule, InputNumberModule, AutoCompleteModule
  ],
  templateUrl: './storia.html',
  providers: [ConfirmationService, MessageService]
})
export class Storia implements OnInit {

  idAnimale!: string;
  eventi: any[] = [];
  tipiEvento: any[] = [];
  tipiEventoFiltrati: any[] = [];
  contattiSuggeriti: any[] = [];

  displayDialog = false;
  isModifica = false;
  saving = false;

  eventoCorrente: any = this.nuovoEvento();

  private searchSubject = new Subject<{query: string, idTipoEvento: string}>();

  constructor(
    private route: ActivatedRoute,
    private storiaService: StoriaService,
    private contattoService: ContattoService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.idAnimale = params['id'];
      this.caricaTutto();
    });

    // Debounce 300ms sulla ricerca contatti
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged((a, b) => a.query === b.query && a.idTipoEvento === b.idTipoEvento),
      switchMap(({ query, idTipoEvento }) =>
        this.contattoService.autocomplete(query, idTipoEvento)
      )
    ).subscribe(res => {
      this.contattiSuggeriti = res;
      this.cd.detectChanges();
    });

  }

cercaContatto(event: any): void {
  const query = event.query;
  if (!query || query.length < 2) {
    this.contattiSuggeriti = [];
    return;
  }
  const idTipoEvento = this.eventoCorrente.id_tipo_evento || '';
  this.searchSubject.next({ query, idTipoEvento });
}

  getDisplayContatto(contatto: any): string {
    if (!contatto) return '';
    if (contatto.rag_sociale) return contatto.rag_sociale;
    return `${contatto.cognome || ''} ${contatto.nome || ''}`.trim();
  }

  get mostraCosti(): boolean {
    return ['4', '5'].includes(this.eventoCorrente.id_tipo_evento);
  }

  caricaTutto(): void {
    forkJoin({
      eventi: this.storiaService.getEventi(this.idAnimale),
      tipi: this.storiaService.getTipi()
    }).subscribe(({ eventi, tipi }) => {
      this.eventi = (eventi || []).sort((a, b) => {
        return this.parseData(a.dt_da) - this.parseData(b.dt_da);
      });
      this.tipiEvento = tipi || [];
      this.aggiornaTipiFiltrati();
      this.cd.detectChanges();
    });
  }

  private parseData(data: string): number {
    if (!data) return 0;
    // Converte dd/MM/yyyy in timestamp per confronto corretto
    const [giorno, mese, anno] = data.split('/');
    return new Date(+anno, +mese - 1, +giorno).getTime();
  }

  aggiornaTipiFiltrati(idEventoInModifica?: string): void {
    const origineGiàPresente = this.eventi.some(
      e => e.id_tipo_evento === '1' && e.id_evento !== idEventoInModifica
    );
    this.tipiEventoFiltrati = origineGiàPresente
      ? this.tipiEvento.filter(t => t.id_tipo_evento !== '1')
      : [...this.tipiEvento];
  }

  nuovoEvento(): any {
    return {
      id_evento: null,
      id_animale: null,
      id_tipo_evento: null,
      dt_da: null,
      dt_a: null,
      note: '',
      ct_gg: '0',
      ct_mese: '0',
      contatto: null
    };
  }

  apriNuovo(): void {
    this.eventoCorrente = this.nuovoEvento();
    this.eventoCorrente.id_animale = this.idAnimale;
    this.isModifica = false;
    this.aggiornaTipiFiltrati(); // nessun id escluso → filtra Origine se già presente
    this.displayDialog = true;
  }

  apriModifica(evento: any): void {
    this.eventoCorrente = { ...evento };
    this.isModifica = true;
    this.aggiornaTipiFiltrati(evento.id_evento); // esclude se stesso → Origine visibile se è l'evento corrente
    this.displayDialog = true;
  }

  salva(): void {
    if (!this.eventoCorrente.id_tipo_evento || !this.eventoCorrente.dt_da) {
      this.messageService.add({
        severity: 'warn', summary: 'Attenzione',
        detail: 'Tipo evento e data inizio sono obbligatori'
      });
      return;
    }

    this.saving = true;

    // Conversione campi numerici in stringa per il BE
    const payload = { ...this.eventoCorrente };
    if (!this.mostraCosti) {
      payload.ct_gg = '0';
      payload.ct_mese = '0';
    } else {
      payload.ct_gg = String(payload.ct_gg ?? '0');
      payload.ct_mese = String(payload.ct_mese ?? '0');
    }



    const op$ = this.isModifica
      ? this.storiaService.update(payload.id_evento, payload)
      : this.storiaService.save(payload);

    op$.subscribe({
      next: () => {
        this.saving = false;
        this.displayDialog = false;
        this.messageService.add({ severity: 'success', summary: 'Salvato', detail: 'Evento salvato con successo' });
        this.caricaTutto();
      },
      error: (err) => {
        this.saving = false;
        const msg = err.error?.errore || 'Impossibile salvare l\'evento';
        this.messageService.add({ severity: 'error', summary: 'Errore', detail: msg });
      }
    });
  }

  elimina(evento: any): void {
    this.confirmationService.confirm({
      message: `Vuoi eliminare l'evento <strong>${evento.evento}</strong> del ${evento.dt_da}?`,
      header: 'Conferma Eliminazione',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.storiaService.delete(evento.id_evento).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Eliminato', detail: 'Evento rimosso' });
            this.caricaTutto();
          }
        });
      }
    });
  }
}