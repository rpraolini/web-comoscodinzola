import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { ToastModule } from 'primeng/toast';
import { CarattereService } from '../../../../../services/animali/carattere.service';
import { AnimaleService } from '../../../../../services/animali/animale.service';

@Component({
  selector: 'app-carattere',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, SelectModule, TextareaModule, ToastModule],
  templateUrl: './carattere.html',
  providers: [MessageService]
})
export class Carattere implements OnInit {

  idAnimale!: string;
  note: string = '';
  descrBreve: string = '';
  descrLunga: string = '';

  // Ogni riga = una categoria con il suo carattere selezionato
  righe: {
    tipo: any;                  // TipoCarattere (icona, contesto)
    opzioni: any[];             // caratteri disponibili per questa categoria
    carattereSelezionato: any;  // Carattere scelto (o null)
    idCaratteri: string | null; // id_caratteri se già salvato sul DB
    noteLibere: string;
  }[] = [];

  loading = false;

  constructor(
    private route: ActivatedRoute,
    private carattereService: CarattereService,
    private animaleService: AnimaleService,
    private messageService: MessageService,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.idAnimale = params['id'];
      this.caricaTutto();
    });
  }

  caricaTutto(): void {
    this.carattereService.getTipiCarattere().subscribe(tipi => {
      this.carattereService.getCaratteriAnimale(this.idAnimale).subscribe(assegnati => {

        // Nota generale: prendo la prima nota disponibile tra i caratteri assegnati
        this.note = assegnati.length > 0 ? (assegnati[0].note || '') : '';

        // Costruisco una riga per ogni categoria
        this.righe = tipi.map(tipo => {
          const opzioni = [
            { carattere: 'Seleziona un carattere', id_carattere: null },
            ...(tipo.caratteri || [])
          ];

          const assegnato = assegnati.find(a => a.id_tipo_carattere === tipo.id_tipo_carattere);

          return {
            tipo,
            opzioni,
            carattereSelezionato: assegnato
              ? opzioni.find(o => o.id_carattere === assegnato.id_carattere) || null
              : null,
            idCaratteri: assegnato?.id_caratteri || null,
            noteLibere: assegnato?.note || ''  // ← campo per la riga libera
          };
        });
        this.righe.sort((a, b) => {
          if (a.tipo.id_tipo_carattere === '0') return -1;
          if (b.tipo.id_tipo_carattere === '0') return 1;
          return 0;
        });
        this.cd.detectChanges();
      });
    });
    this.animaleService.getById(this.idAnimale).subscribe(animale => {
      this.descrBreve = animale.descr_breve || '';
      this.descrLunga = animale.descr_lunga || '';
      this.cd.detectChanges();
    });
  }

  isRigaLibera(riga: any): boolean {
    return riga.tipo.id_tipo_carattere === '0';
  }

  getColoreCategoria(idTipo: string): string {
    const colori: Record<string, string> = {
      '0': '#8b5cf6', // SONO SPECIALE - viola
      '1': '#f59e0b', // GUINZAGLIO - ambra
      '2': '#3b82f6', // COI MASCHI - blu
      '3': '#ec4899', // CON LE FEMMINE - rosa
      '4': '#f97316', // COI GATTI - arancione
      '5': '#10b981', // COI BAMBINI - verde
      '6': '#6366f1', // TIPO DI FAMIGLIA - indigo
      '8': '#14b8a6', // TIPO DI ABITAZIONE - teal
      '9': '#ef4444', // COI CANI - rosso
    };
    return colori[idTipo] ?? '#64748b';
  }

  salva(): void {
    this.loading = true;

    this.animaleService.getById(this.idAnimale).subscribe(animale => {
      animale.descr_breve = this.descrBreve;
      animale.descr_lunga = this.descrLunga;

      this.animaleService.updateAnimale(this.idAnimale, animale).subscribe({
        error: () => this.messageService.add({
          severity: 'error', summary: 'Errore', detail: 'Impossibile salvare le descrizioni'
        })
      });
    });

    const operazioni = this.righe.map(riga => {

      // Riga libera (SONO SPECIALE PERCHE'): salva solo la nota
      if (this.isRigaLibera(riga)) {
        if (riga.idCaratteri) {
          return this.carattereService.aggiornaNota(riga.idCaratteri, '0', riga.noteLibere).toPromise();
        } else if (riga.noteLibere?.trim()) {
          return this.carattereService.aggiungi(this.idAnimale, {
            id_carattere: '0',
            note: riga.noteLibere
          }).toPromise();
        }
        return Promise.resolve();
      }

      const idCarattere = riga.carattereSelezionato?.id_carattere;

      if (riga.idCaratteri && !idCarattere) {
        return this.carattereService.elimina(riga.idCaratteri).toPromise();
      } else if (idCarattere && !riga.idCaratteri) {
        return this.carattereService.aggiungi(this.idAnimale, {
          id_carattere: idCarattere,
          note: ''
        }).toPromise();
      } else if (idCarattere && riga.idCaratteri) {
        return this.carattereService.aggiornaNota(riga.idCaratteri, idCarattere, '').toPromise();
      }
      return Promise.resolve();
    });

    Promise.all(operazioni).then(() => {
      this.loading = false;
      this.messageService.add({ severity: 'success', summary: 'Salvato', detail: 'Carattere aggiornato con successo' });
      this.caricaTutto();
    }).catch(() => {
      this.loading = false;
      this.messageService.add({ severity: 'error', summary: 'Errore', detail: 'Impossibile salvare' });
    });
  }

  reset(): void {
    this.caricaTutto();
  }
}