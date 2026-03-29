import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';

import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { CronologiaService } from '../../../../../services/animali/cronologia.service';

@Component({
  selector: 'app-cronologia',
  standalone: true,
  imports: [CommonModule, TableModule, TagModule, TooltipModule],
  templateUrl: './cronologia.html'
})
export class Cronologia implements OnInit {

  idAnimale!: string;
  attivita: any[] = [];
  proprietario: any = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private cronologiaService: CronologiaService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.idAnimale = params['id'];
      this.caricaTutto();
    });
  }

  caricaTutto(): void {
    this.loading = true;
    forkJoin({
      attivita: this.cronologiaService.getAttivita(this.idAnimale),
      proprietario: this.cronologiaService.getProprietario(this.idAnimale)
    }).subscribe({
      next: ({ attivita, proprietario }) => {
        this.attivita = attivita || [];
        this.proprietario = proprietario;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  getNomeProprietario(): string {
    if (!this.proprietario) return '—';
    if (this.proprietario.rag_sociale) return this.proprietario.rag_sociale;
    return `${this.proprietario.cognome || ''} ${this.proprietario.nome || ''}`.trim();
  }
}