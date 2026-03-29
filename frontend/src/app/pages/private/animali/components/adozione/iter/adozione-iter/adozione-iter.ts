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
import { TextareaModule } from 'primeng/textarea';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { AdozioneService } from '../../../../../../../services/animali/adozione.service';
import { ContattoService } from '../../../../../../../services/contatti/contatto.service';

@Component({
  selector: 'app-adozione-iter',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule,
    ButtonModule, InputTextModule, AutoCompleteModule,
    TextareaModule, ToastModule
  ],
  providers: [MessageService],
  templateUrl: './adozione-iter.html',
  styleUrl: './adozione-iter.css'
})
export class AdozioneIterComponent implements OnInit {

  @Input() iter: any = null;
  @Input() idPratica!: string;
  @Input() idAnimale!: string;

  @Output() salvato    = new EventEmitter<void>();
  @Output() annullato  = new EventEmitter<void>();

  form: any = this.formVuoto();
  saving = false;

  adottantiSuggeriti: any[] = [];
  volontarieSuggerite: any[] = [];
  private searchAdottante$ = new Subject<string>();
  private searchVolontaria$ = new Subject<string>();

  get isModifica(): boolean { return !!this.iter?.id_iter; }

  constructor(
    private adozioneService: AdozioneService,
    private contattoService: ContattoService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.form = this.iter ? { ...this.iter } : this.formVuoto();

    this.searchAdottante$.pipe(
      debounceTime(300), distinctUntilChanged(),
      switchMap(q => this.contattoService.autocomplete(q, '1'))
    ).subscribe(res => {
      this.adottantiSuggeriti = res;
      this.cd.markForCheck();
    });

    this.searchVolontaria$.pipe(
      debounceTime(300), distinctUntilChanged(),
      switchMap(q => this.contattoService.autocomplete(q, ''))
    ).subscribe(res => {
      this.volontarieSuggerite = res;
      this.cd.markForCheck();
    });
  }

  cercaAdottante(event: any): void {
    if (event.query?.length >= 2) this.searchAdottante$.next(event.query);
  }

  cercaVolontaria(event: any): void {
    if (event.query?.length >= 2) this.searchVolontaria$.next(event.query);
  }

  getNomeContatto(c: any): string {
    if (!c) return '';
    return c.rag_sociale || `${c.cognome || ''} ${c.nome || ''}`.trim();
  }

  salva(): void {
    if (!this.form.adottante?.id_contatto) {
      this.messageService.add({ severity: 'warn', summary: 'Attenzione', detail: 'Il campo Adottante è obbligatorio' });
      return;
    }
    if (!this.form.volontaria?.id_contatto) {
      this.messageService.add({ severity: 'warn', summary: 'Attenzione', detail: 'Il campo Volontaria è obbligatorio' });
      return;
    }

    this.saving = true;
    const payload = {
      ...this.form,
      id_tipo_iter: '2',
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
          detail: err.error?.errore || 'Impossibile salvare'
        });
        this.cd.markForCheck();
      }
    });
  }

  annulla(): void {
    this.annullato.emit();
  }

  private formVuoto(): any {
    return {
      id_iter: null,
      adottante: null,
      volontaria: null,
      note: ''
    };
  }
}
