import {
  Component, Input, Output, EventEmitter,
  OnInit, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild, ElementRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { from, concatMap, switchMap } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { RadioButtonModule } from 'primeng/radiobutton';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { AdozioneService } from '../../../../../../../services/animali/adozione.service';
import { DocumentiService } from '../../../../../../../services/documenti/documento.service';

@Component({
  selector: 'app-consegna',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule,
    ButtonModule, DatePickerModule, SelectModule,
    TextareaModule, RadioButtonModule, ToastModule
  ],
  providers: [MessageService],
  templateUrl: './consegna.html',
  styleUrl: './consegna.css'
})
export class ConsegnaComponent implements OnInit {

  @Input() iter: any = null;
  @Input() pratica: any = null;       // serve per leggere l'adottante dall'iter adozione
  @Input() idPratica!: string;
  @Input() idAnimale!: string;

  @Output() salvato    = new EventEmitter<void>();
  @Output() annullato  = new EventEmitter<void>();

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  form: any = this.formVuoto();
  saving = false;

  // Selezione documento
  tipiDocumento: any[] = [];
  tipoDocSelezionato: any = null;
  fileSelezionato: File | null = null;

  // Coda locale: file da caricare al salvataggio
  fileDaAllegare: { tipo: any; file: File }[] = [];

  // Documenti già salvati sul server
  documentiIter: any[] = [];

  get isModifica(): boolean { return !!this.iter?.id_iter; }

  /** Adottante ereditato dall'iter di adozione della stessa pratica */
  get adottante(): any {
    return this.pratica?.iter?.find((i: any) => i.id_tipo_iter === '2')?.adottante ?? null;
  }

  get nomeAdottante(): string {
    const a = this.adottante;
    if (!a) return '—';
    return a.rag_sociale || `${a.cognome || ''} ${a.nome || ''}`.trim();
  }

  constructor(
    private adozioneService: AdozioneService,
    private documentiService: DocumentiService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.form = this.iter ? { ...this.iter } : this.formVuoto();
    this.documentiIter = this.iter?.documenti ? [...this.iter.documenti] : [];

    this.documentiService.getTipiDoc('G').subscribe({
      next: tipi => {
        this.tipiDocumento = tipi.filter((t: any) => t.id_tipo_documento === '6');
        if (this.tipiDocumento.length === 1) {
          this.tipoDocSelezionato = this.tipiDocumento[0];
        }
        this.cd.markForCheck();
      },
      error: () => {
        this.tipiDocumento = [];
        this.cd.markForCheck();
      }
    });
  }

  onFileScelto(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fileSelezionato = input.files?.[0] ?? null;
    this.cd.markForCheck();
  }

  /** Aggiunge il file alla coda locale — nessuna chiamata API */
  aggiungiDocumento(): void {
    if (!this.fileSelezionato) {
      this.messageService.add({ severity: 'warn', summary: 'Attenzione', detail: 'Seleziona un file' });
      return;
    }
    this.fileDaAllegare.push({ tipo: this.tipoDocSelezionato, file: this.fileSelezionato });
    this.fileSelezionato = null;
    if (this.fileInput) this.fileInput.nativeElement.value = '';
    this.cd.markForCheck();
  }

  rimuoviDaCoda(index: number): void {
    this.fileDaAllegare.splice(index, 1);
    this.cd.markForCheck();
  }

  scaricaFile(idFile: string, filename: string): void {
    this.documentiService.downloadFile(idFile).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  eliminaDoc(idDocumento: string): void {
    this.documentiService.deleteDocumento(idDocumento).subscribe({
      next: () => {
        this.documentiIter = this.documentiIter.filter(d => d.id_documento !== idDocumento);
        this.cd.markForCheck();
      },
      error: (err: any) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Errore',
          detail: err.error?.errore || 'Impossibile eliminare il documento'
        });
        this.cd.markForCheck();
      }
    });
  }

  salva(): void {
    if (!this.form.dt_consegna) {
      this.messageService.add({ severity: 'warn', summary: 'Attenzione', detail: 'La data consegna è obbligatoria' });
      return;
    }

    this.saving = true;
    const payload = {
      ...this.form,
      id_tipo_iter: '3',
      id_pratica: this.idPratica,
      id_animale: this.idAnimale,
      adottante: this.adottante
    };

    const op$ = this.isModifica
      ? this.adozioneService.aggiornaIter(this.form.id_iter, payload)
      : this.adozioneService.aggiungiIter(payload);

    op$.pipe(
      switchMap((res: any) => {
        if (this.fileDaAllegare.length === 0) return from([null]);
        const idIter = res?.id_iter ?? this.form.id_iter;
        return from(this.fileDaAllegare).pipe(
          concatMap(f => this.adozioneService.saveDocumentoIter(idIter, this.idAnimale, f.tipo, f.file))
        );
      })
    ).subscribe({
      next: () => {},
      error: (err: any) => {
        this.saving = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Errore',
          detail: err.error?.errore || 'Impossibile salvare la consegna'
        });
        this.cd.markForCheck();
      },
      complete: () => {
        this.saving = false;
        this.salvato.emit();
      }
    });
  }

  annulla(): void {
    this.annullato.emit();
  }

  private formVuoto(): any {
    return {
      id_iter: null,
      dt_consegna: null,
      contributo: null,
      note: ''
    };
  }
}
