import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { forkJoin, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { TagModule } from 'primeng/tag';
import { AdozioneService } from '../../../../../services/animali/adozione.service';
import { ContattoService } from '../../../../../services/contatti/contatto.service';
import { DocumentiService } from '../../../../../services/documenti/documento.service';
import { DettaglioStateService } from '../../dettaglio-animale/DettaglioStateService';
import { AnimaleService } from '../../../../../services/animali/animale.service';
import { PreaffidoComponent } from './iter/preaffido/preaffido';
import { AdozioneIterComponent } from './iter/adozione-iter/adozione-iter';
import { ConsegnaComponent } from './iter/consegna/consegna';
import { PassaggioComponent } from './iter/passaggio/passaggio';

@Component({
  selector: 'app-adozione',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule, FormsModule, ButtonModule, ToastModule, ConfirmDialogModule,
    TooltipModule, TagModule,
    PreaffidoComponent, AdozioneIterComponent, ConsegnaComponent, PassaggioComponent
  ],
  templateUrl: './adozione.html',
  providers: [ConfirmationService, MessageService]
})
export class Adozione implements OnInit {

  idAnimale!: string;
  pratiche: any[] = [];
  tipiIter: any[] = [];

  animale: any = null;

  // Tipi iter disponibili per la pratica corrente (filtrati per sequenza)
  tipiIterDisponibili: any[] = [];

  // Pannello iter inline aperto
  iterPanelAperto: { pratica: any; iter: any | null; tipo: string } | null = null;

  // Autocomplete
  adottantiSuggeriti: any[] = [];
  volontarieSuggerite: any[] = [];
  private searchAdottante$ = new Subject<string>();
  private searchVolontaria$ = new Subject<string>();

  readonly SEQUENZA = ['1', '2', '3', '4'];

  constructor(
    private route: ActivatedRoute,
    private adozioneService: AdozioneService,
    private contattoService: ContattoService,
    private animaleService: AnimaleService,
    private documentiService: DocumentiService,
    private stateService: DettaglioStateService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.idAnimale = params['id'];
      this.caricaTutto();
    });

    this.stateService.animale$.subscribe(a => {
      this.animale = a;
      this.cd.markForCheck();
    });

    this.searchAdottante$.pipe(
      debounceTime(300), distinctUntilChanged(),
      switchMap(q => this.contattoService.autocomplete(q, '1'))
    ).subscribe(res => {
      this.adottantiSuggeriti = res;
      this.cd.markForCheck(); // ← era detectChanges()
    });

    this.searchVolontaria$.pipe(
      debounceTime(300), distinctUntilChanged(),
      switchMap(q => this.contattoService.autocomplete(q, ''))
    ).subscribe(res => {
      this.volontarieSuggerite = res;
      this.cd.markForCheck(); // ← era detectChanges()
    });
  }

  get isAdottabile(): boolean {
    return this.animale?.id_stato === '2';
  }

  get isRevocaAdottabile(): boolean {
    return this.animale?.id_stato === '3' && !this.haPraticaAttiva;
  }

  rendiAdottabile(): void {
    this.confirmationService.confirm({
      message: `Vuoi rendere <strong>${this.animale?.nome}</strong> adottabile?`,
      header: 'Conferma',
      icon: 'pi pi-heart',
      acceptLabel: 'Sì',
      rejectLabel: 'Annulla',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.animaleService.rendiAdottabile(this.idAnimale).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Aggiornato', detail: 'Animale reso adottabile' });
            this.animaleService.getById(this.idAnimale).subscribe(a => {
              this.stateService.setAnimale(a);
            });
          },
          error: (err) => this.messageService.add({ severity: 'error', summary: 'Errore', detail: err.error?.errore })
        });
      }
    });
  }

  revocaAdottabile(): void {
    this.confirmationService.confirm({
      message: `Vuoi revocare l'adottabilità di <strong>${this.animale?.nome}</strong>?`,
      header: 'Conferma',
      icon: 'pi pi-times-circle',
      acceptLabel: 'Sì, revoca',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-warning',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.animaleService.revocaAdottabile(this.idAnimale).subscribe({
          next: () => {
            this.messageService.add({ severity: 'info', summary: 'Aggiornato', detail: 'Adottabilità revocata' });
            this.animaleService.getById(this.idAnimale).subscribe(a => {
              this.stateService.setAnimale(a);
            });
          },
          error: (err) => this.messageService.add({ severity: 'error', summary: 'Errore', detail: err.error?.errore })
        });
      }
    });
  }

  caricaTutto(): void {
    forkJoin({
      pratiche: this.adozioneService.getPratiche(this.idAnimale),
      tipi: this.adozioneService.getTipi()
    }).subscribe(({ pratiche, tipi }) => {
      this.pratiche = pratiche || [];
      this.tipiIter = tipi || [];
      this.cd.markForCheck();
      this.animaleService.getById(this.idAnimale).subscribe(a => {
        this.stateService.setAnimale(a);
      });
    });
  }

  nuovaPratica(): void {
    this.confirmationService.confirm({
      message: 'Vuoi aprire una nuova pratica di adozione?',
      header: 'Nuova Pratica',
      icon: 'pi pi-folder-open',
      acceptLabel: 'Sì, crea',
      rejectLabel: 'Annulla',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.adozioneService.nuovaPratica(this.idAnimale).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Pratica creata', detail: 'Nuova pratica aperta' });
            this.caricaTutto();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Errore', detail: err.error?.errore || 'Impossibile creare la pratica' });
          }
        });
      }
    });
  }

  chiudiPratica(pratica: any): void {
    this.confirmationService.confirm({
      message: `Vuoi chiudere la pratica <strong>${pratica.id_pratica}</strong>? Non sarà più possibile aggiungere iter.`,
      header: 'Chiudi Pratica',
      icon: 'pi pi-lock',
      acceptLabel: 'Sì, chiudi',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-warning',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.adozioneService.chiudiPratica(pratica.id_pratica).subscribe({
          next: () => {
            this.messageService.add({ severity: 'info', summary: 'Chiusa', detail: 'Pratica chiusa con successo' });
            this.caricaTutto();
          }
        });
      }
    });
  }

  eliminaPratica(pratica: any): void {
    this.confirmationService.confirm({
      message: `Vuoi eliminare definitivamente la pratica <strong>${pratica.id_pratica}</strong> e tutti i suoi iter?`,
      header: 'Elimina Pratica',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.adozioneService.eliminaPratica(pratica.id_pratica, this.idAnimale).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Eliminata', detail: 'Pratica eliminata' });
            this.caricaTutto();
          }
        });
      }
    });
  }

  apriNuovoIter(pratica: any): void {
    this.calcolaTipiDisponibili(pratica);
    const prossimoTipo = this.tipiIterDisponibili[0]?.id_tipo_iter;
    if (prossimoTipo) {
      this.iterPanelAperto = { pratica, iter: null, tipo: prossimoTipo };
    }
  }

  apriModificaIter(iter: any, pratica: any): void {
    this.iterPanelAperto = { pratica, iter, tipo: iter.id_tipo_iter };
  }

  chiudiIterPanel(): void {
    this.iterPanelAperto = null;
    this.cd.markForCheck();
  }

  onPreaffidoSalvato(): void {
    this.iterPanelAperto = null;
    this.cd.markForCheck();
    this.caricaTutto();
  }

  calcolaTipiDisponibili(pratica: any): void {
    const tipiPresenti = (pratica.iter || []).map((i: any) => i.id_tipo_iter);
    if (tipiPresenti.length === 0) {
      // Solo Preaffido disponibile come primo
      this.tipiIterDisponibili = this.tipiIter.filter(t => t.id_tipo_iter === '1');
    } else {
      const maxIdx = Math.max(...tipiPresenti.map((t: string) => this.SEQUENZA.indexOf(t)));
      const prossimo = this.SEQUENZA[maxIdx + 1];
      this.tipiIterDisponibili = prossimo
        ? this.tipiIter.filter(t => t.id_tipo_iter === prossimo)
        : [];
    }
  }

  eliminaIter(iter: any): void {
    this.confirmationService.confirm({
      message: `Vuoi eliminare l'iter <strong>${iter.tipo_iter}</strong>?`,
      header: 'Elimina Iter',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sì, elimina',
      rejectLabel: 'Annulla',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.adozioneService.eliminaIter(iter.id_iter, this.idAnimale).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Eliminato', detail: 'Iter rimosso' });
            this.caricaTutto();
          }
        });
      }
    });
  }

  cercaAdottante(event: any): void {
    if (event.query?.length >= 2) this.searchAdottante$.next(event.query);
  }

  cercaVolontaria(event: any): void {
    if (event.query?.length >= 2) this.searchVolontaria$.next(event.query);
  }

  getColoreIter(colore: string): string {
    return colore || '#6366f1';
  }

  getIconaIter(idTipo: string): string {
    const icone: Record<string, string> = {
      '1': 'pi pi-home',
      '2': 'pi pi-heart',
      '3': 'pi pi-box',
      '4': 'pi pi-arrow-right-arrow-left'
    };
    return icone[idTipo] ?? 'pi pi-tag';
  }

  getNomeContatto(c: any): string {
    if (!c) return '—';
    return c.rag_sociale || `${c.cognome || ''} ${c.nome || ''}`.trim();
  }

  get haPraticaAttiva(): boolean {
    return this.pratiche.some(p => p.isAperta);
  }

  get istruttoriaChiusa(): boolean {
    return this.animale?.id_stato === '98';
  }

  isUltimoIter(iter: any, pratica: any): boolean {
    if (!pratica.iter?.length) return false;
    const maxIdx = Math.max(...pratica.iter.map((i: any) =>
      this.SEQUENZA.indexOf(i.id_tipo_iter)
    ));
    return this.SEQUENZA.indexOf(iter.id_tipo_iter) === maxIdx;
  }

  get puoAprirePratica(): boolean {
    return this.animale?.id_stato === '3' && !this.haPraticaAttiva;
  }

  get hasMicrochip(): boolean {
    return !!this.animale?.num_microchip?.trim();
  }

  scaricaQuestionarioPdf(idIter: string, questKey: string): void {
    this.adozioneService.exportQuestionarioPdf(idIter, questKey).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 10000);
      },
      error: err => this.messageService.add({
        severity: 'error', summary: 'Errore',
        detail: err?.error?.errore || 'Impossibile generare il PDF del questionario'
      })
    });
  }

  apriFinestraPdf(idIter: string): void {
    this.adozioneService.exportAffidoPdf(idIter, this.idAnimale).subscribe({
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

  getPdfUrl(idIter: string): string {
    console.log(`/manorg/api/private/adozione/iter/${idIter}/passaggio/pdf?idAnimale=${this.idAnimale}`)
    return `/manorg/api/private/adozione/iter/${idIter}/passaggio/pdf?idAnimale=${this.idAnimale}`;
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



}