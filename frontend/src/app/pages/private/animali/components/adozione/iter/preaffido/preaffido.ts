import {
  Component, Input, Output, EventEmitter,
  OnInit, ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { AdozioneService } from '../../../../../../../services/animali/adozione.service';
import { ContattoService } from '../../../../../../../services/contatti/contatto.service';

@Component({
  selector: 'app-preaffido',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule,
    ButtonModule, InputTextModule, AutoCompleteModule,
    DatePickerModule, SelectModule, TextareaModule, TooltipModule, ToastModule
  ],
  providers: [MessageService],
  templateUrl: './preaffido.html',
  styleUrl: './preaffido.css'
})
export class PreaffidoComponent implements OnInit {

  @Input() iter: any = null;          // null = nuovo iter
  @Input() idPratica!: string;
  @Input() idAnimale!: string;

  @Output() salvato   = new EventEmitter<void>();
  @Output() annullato = new EventEmitter<void>();

  form: any = this.formVuoto();
  saving = false;

  volontarieSuggerite: any[] = [];
  colloquioSuggeriti: any[] = [];
  private searchVolontaria$ = new Subject<string>();
  private searchColloquio$ = new Subject<string>();

  readonly opzioniEsito = [
    { label: 'Positivo',  value: 'P' },
    { label: 'Negativo',  value: 'N' }
  ];

  readonly opzioniQuestionario = [
    { label: 'Inviato',   value: 'I' },
    { label: 'Ritornato', value: 'R' }
  ];

  get isModifica(): boolean { return !!this.iter?.id_iter; }

  constructor(
    private adozioneService: AdozioneService,
    private contattoService: ContattoService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.form = this.iter ? { ...this.iter } : this.formVuoto();

    this.searchVolontaria$.pipe(
      debounceTime(300), distinctUntilChanged(),
      switchMap(q => this.contattoService.autocomplete(q, ''))
    ).subscribe(res => {
      this.volontarieSuggerite = res;
      this.cd.markForCheck();
    });

    this.searchColloquio$.pipe(
      debounceTime(300), distinctUntilChanged(),
      switchMap(q => this.contattoService.autocomplete(q, ''))
    ).subscribe(res => {
      this.colloquioSuggeriti = res;
      this.cd.markForCheck();
    });
  }

  cercaVolontaria(event: any): void {
    if (event.query?.length >= 2) this.searchVolontaria$.next(event.query);
  }

  cercaColloquio(event: any): void {
    if (event.query?.length >= 2) this.searchColloquio$.next(event.query);
  }

  getNomeContatto(c: any): string {
    if (!c) return '';
    return c.rag_sociale || `${c.cognome || ''} ${c.nome || ''}`.trim();
  }

  salva(): void {
    if (!this.form.nome?.trim()) {
      this.messageService.add({ severity: 'warn', summary: 'Attenzione', detail: 'Il campo Nome è obbligatorio' });
      return;
    }
    if (!this.form.localita?.trim()) {
      this.messageService.add({ severity: 'warn', summary: 'Attenzione', detail: 'Il campo Località è obbligatorio' });
      return;
    }

    this.saving = true;
    const payload = {
      ...this.form,
      id_tipo_iter: '1',
      id_pratica: this.idPratica,
      id_animale: this.idAnimale
    };

    const op$ = this.isModifica
      ? this.adozioneService.aggiornaIter(this.form.id_iter, payload)
      : this.adozioneService.aggiungiIter(payload);

    op$.subscribe({
      next: () => {
        this.saving = false;
        this.salvato.emit();
      },
      error: (err: any) => {
        this.saving = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Errore',
          detail: err.error?.errore || 'Impossibile salvare il preaffido'
        });
        this.cd.markForCheck();
      }
    });
  }

  apriQuestionarioPdf(): void {
    if (!this.iter?.id_iter || !this.iter?.quest_key) return;
    this.adozioneService.exportQuestionarioPdf(this.iter.id_iter, this.iter.quest_key).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 10000);
      },
      error: err => this.messageService.add({
        severity: 'error', summary: 'Errore',
        detail: err?.error?.errore || 'Impossibile generare il PDF'
      })
    });
  }

  annulla(): void {
    this.annullato.emit();
  }

  private formVuoto(): any {
    return {
      id_iter: null,
      nome: '',
      localita: '',
      email: '',
      telefono: '',
      quest_key: null,
      volontaria: null,
      colloquio_da: null,
      dt_colloquio: null,
      esito: null,
      note: ''
    };
  }
}
