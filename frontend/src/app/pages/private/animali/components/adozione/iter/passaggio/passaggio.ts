import {
  Component, Input, Output, EventEmitter,
  OnInit, OnChanges, SimpleChanges, ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { from, concatMap, switchMap } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { AdozioneService } from '../../../../../../../services/animali/adozione.service';
import { DocumentiService } from '../../../../../../../services/documenti/documento.service';
import { CronologiaService } from '../../../../../../../services/animali/cronologia.service';

interface SlotDoc {
  idTipo: string;
  label: string;
  tipoObj: any;           // oggetto tipo dal backend
  fileInput?: HTMLInputElement;
}

@Component({
  selector: 'app-passaggio',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule,
    ButtonModule, TextareaModule, TooltipModule, ToastModule
  ],
  providers: [MessageService],
  templateUrl: './passaggio.html',
  styleUrl: './passaggio.css'
})
export class PassaggioComponent implements OnInit, OnChanges {

  @Input() iter: any = null;
  @Input() pratica: any = null;
  @Input() animale: any = null;
  @Input() idPratica!: string;
  @Input() idAnimale!: string;
  @Input() cardMode = false;

  @Output() salvato   = new EventEmitter<void>();
  @Output() annullato = new EventEmitter<void>();

  note = '';
  saving = false;

  /** Slot dei 3 documenti caricabili */
  readonly slots: SlotDoc[] = [
    { idTipo: '11', label: 'PASSAGGIO PROPRIETA ADOTTANTE',      tipoObj: null },
    { idTipo: '13', label: 'PASSAGGIO PROPRIETA ASL ORIGINE',    tipoObj: null },
    { idTipo: '12', label: 'PASSAGGIO PROPRIETA ASL DESTINAZIONE', tipoObj: null },
  ];

  /** File in coda per ogni slot (usato in panelMode prima del salvataggio) */
  filePending: { [idTipo: string]: File | null } = { '11': null, '12': null, '13': null };

  /** Stato di upload per slot (cardMode) */
  uploading: { [idTipo: string]: boolean } = { '11': false, '12': false, '13': false };

  /** Documenti già salvati, indicizzati per id_tipo_documento */
  documentiPerTipo: { [idTipo: string]: any } = {};

  get isNuovo(): boolean { return !this.iter?.id_iter; }

  get adottante(): any {
    return this.pratica?.iter?.find((i: any) => i.id_tipo_iter === '2')?.adottante ?? null;
  }

  private _proprietarioCaricato: any = null;

  get proprietario(): any {
    return this.iter?.proprietario ?? this.animale?.proprietario ?? this._proprietarioCaricato ?? null;
  }

  get nomeAdottante(): string { return this._nome(this.adottante); }
  get nomeProprietario(): string { return this._nome(this.proprietario); }

  get ultimoSlotConDocumento(): string | null {
    for (let i = this.slots.length - 1; i >= 0; i--) {
      if (this.documentiPerTipo[this.slots[i].idTipo]) return this.slots[i].idTipo;
    }
    return null;
  }

  get adottanteHaCartaIdentita(): boolean {
    return this.adottante?.documenti?.some((d: any) => d.id_tipo_documento === '1' && d.num_documento) ?? false;
  }

  get proprietarioHaCartaIdentita(): boolean {
    return this.proprietario?.documenti?.some((d: any) => d.id_tipo_documento === '1' && d.num_documento) ?? false;
  }

  apriPdf(): void {
    if (!this.iter?.id_iter) return;
    this.adozioneService.exportPassaggioPdf(this.iter.id_iter, this.idAnimale).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 10000);
      },
      error: err => this._errore(err)
    });
  }

  constructor(
    private adozioneService: AdozioneService,
    private documentiService: DocumentiService,
    private cronologiaService: CronologiaService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['iter']) {
      this.note = this.iter?.note ?? '';
      // Resetta documentiPerTipo solo se il server ritorna documenti;
      // altrimenti preserva lo stato locale aggiornato da aggiungi()
      if (this.iter?.documenti?.length > 0) {
        this._indicizzaDocumenti(this.iter.documenti);
      }
    }
  }

  ngOnInit(): void {
    this.note = this.iter?.note ?? '';
    this._indicizzaDocumenti(this.iter?.documenti ?? []);

    if (!this.proprietario && this.idAnimale) {
      this.cronologiaService.getProprietario(this.idAnimale).subscribe({
        next: p => { this._proprietarioCaricato = p; this.cd.markForCheck(); }
      });
    }

    this.documentiService.getTipiDoc('G').subscribe({
      next: tipi => {
        this.slots.forEach(s => {
          s.tipoObj = tipi.find((t: any) => t.id_tipo_documento === s.idTipo) ?? null;
        });
        this.cd.markForCheck();
      }
    });
  }

  // ── File handling ──────────────────────────────────────────────

  onFileScelto(event: Event, idTipo: string): void {
    const input = event.target as HTMLInputElement;
    this.filePending[idTipo] = input.files?.[0] ?? null;
    this.cd.markForCheck();
  }

  /** cardMode: upload immediato. panelMode: mette in coda */
  aggiungi(slot: SlotDoc): void {
    const file = this.filePending[slot.idTipo];
    if (!file) return;

    if (this.cardMode && this.iter?.id_iter) {
      this.uploading[slot.idTipo] = true;
      this.adozioneService.saveDocumentoIter(this.iter.id_iter, this.idAnimale, slot.tipoObj, file)
        .subscribe({
          next: (doc: any) => {
            this.uploading[slot.idTipo] = false;
            this.filePending[slot.idTipo] = null;
            if (doc) this.documentiPerTipo[slot.idTipo] = doc;
            this.cd.markForCheck();
            this.salvato.emit();
          },
          error: (err: any) => {
            this.uploading[slot.idTipo] = false;
            this._errore(err);
          }
        });
    }
    // panelMode: il file rimane in filePending, viene caricato da salva()
  }

  rimuoviPending(idTipo: string, inputEl: HTMLInputElement): void {
    this.filePending[idTipo] = null;
    inputEl.value = '';
    this.cd.markForCheck();
  }

  scaricaFile(idFile: string, filename: string): void {
    this.documentiService.downloadFile(idFile).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = filename; a.click();
      URL.revokeObjectURL(url);
    });
  }

  eliminaDoc(idDocumento: string, idTipo: string): void {
    this.documentiService.deleteDocumento(idDocumento).subscribe({
      next: () => {
        delete this.documentiPerTipo[idTipo];
        this.cd.markForCheck();
      },
      error: (err: any) => this._errore(err)
    });
  }

  // ── Salvataggio iter + upload in coda ─────────────────────────

  salva(): void {
    this.saving = true;
    const payload = {
      id_iter: this.iter?.id_iter ?? null,
      id_tipo_iter: '4',
      id_pratica: this.idPratica,
      id_animale: this.idAnimale,
      adottante: this.adottante,
      proprietario: this.proprietario,
      note: this.note
    };

    const op$ = this.isNuovo
      ? this.adozioneService.aggiungiIter(payload)
      : this.adozioneService.aggiornaIter(this.iter.id_iter, payload);

    const fileDaAllegare = this.slots
      .filter(s => this.filePending[s.idTipo] && s.tipoObj)
      .map(s => ({ tipo: s.tipoObj, file: this.filePending[s.idTipo]! }));

    op$.pipe(
      switchMap((res: any) => {
        if (fileDaAllegare.length === 0) return from([null]);
        const idIter = res?.id_iter ?? this.iter?.id_iter;
        return from(fileDaAllegare).pipe(
          concatMap(f => this.adozioneService.saveDocumentoIter(idIter, this.idAnimale, f.tipo, f.file))
        );
      })
    ).subscribe({
      next: () => {},
      error: (err: any) => { this.saving = false; this._errore(err); },
      complete: () => { this.saving = false; this.salvato.emit(); }
    });
  }

  annulla(): void { this.annullato.emit(); }

  onChiudiIstruttoria(): void {
    this.adozioneService.chiudiIstruttoria(this.idAnimale).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Chiusa', detail: 'Istruttoria chiusa con successo' });
        this.salvato.emit();
      },
      error: err => this._errore(err)
    });
  }

  // ── Helpers ───────────────────────────────────────────────────

  private _indicizzaDocumenti(docs: any[]): void {
    this.documentiPerTipo = {};
    for (const doc of docs) {
      const idTipo = doc.tipoDocumento?.id_tipo_documento ?? doc.id_tipo_documento;
      if (idTipo) this.documentiPerTipo[idTipo] = doc;
    }
  }

  private _nome(c: any): string {
    if (!c) return '—';
    return c.rag_sociale || `${c.cognome || ''} ${c.nome || ''}`.trim();
  }

  private _errore(err: any): void {
    this.messageService.add({
      severity: 'error', summary: 'Errore',
      detail: err?.error?.errore || 'Operazione fallita'
    });
    this.cd.markForCheck();
  }
}
