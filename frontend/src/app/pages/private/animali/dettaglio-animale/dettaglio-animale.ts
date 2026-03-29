import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Animale } from '../../../../models/animale.model';
import { AnimaleService } from '../../../../services/animali/animale.service';
import { TooltipModule } from 'primeng/tooltip';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { AvatarModule } from 'primeng/avatar';
import { DettaglioStateService } from './DettaglioStateService';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { BreadcrumbModule } from 'primeng/breadcrumb';

@Component({
  selector: 'app-dettaglio-animale',
  standalone: true, // Ormai standard nel 2026
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    ButtonModule,
    AvatarModule,
    TagModule,
    TooltipModule,
    ToastModule,
    ConfirmDialogModule,
    BreadcrumbModule
  ],
  providers: [DettaglioStateService, ConfirmationService, MessageService],
  templateUrl: './dettaglio-animale.html',
  styleUrls: ['./dettaglio-animale.css']
})
export class DettaglioAnimaleComponent implements OnInit {
  animale: Animale = new Animale();
  loading: boolean = true;
  activeStep: string = 'anagrafica'; // Per gestire lo stato del menu

  breadcrumbItems: MenuItem[] = [];
  breadcrumbHome: MenuItem = { icon: 'pi pi-home', routerLink: '/' };

  constructor(
    private stateService: DettaglioStateService,
    private route: ActivatedRoute,
    private animaleService: AnimaleService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.animaleService.getById(id).subscribe({
        next: (data) => {
          this.animale = data;

          // 2. "Il Viaggio di Ritorno": Trasformiamo la data per PrimeNG
          if (data.dt_nascita && typeof data.dt_nascita === 'string') {
              const p = data.dt_nascita.split('/');
              // Creiamo un oggetto Date reale per il p-datepicker
              data.dt_nascita = new Date(+p[2], +p[1] - 1, +p[0]);
          }
          this.stateService.setAnimale(data);
          this.loading = false;
          this.breadcrumbItems = [
            { label: 'Gestione', routerLink: '/private/gestione' },
            { label: 'Animali', routerLink: '/private/animali' },
            { label: this.animale.nome, styleClass: 'font-bold' } // L'animale corrente
          ];
        },
        error: (err) => {
          console.error("Errore nel caricamento", err);
          this.loading = false;
        }
      });
    }

  }

  salva() {
    const payload = this.stateService.getAnimaleSnapshot();

    if (payload) {
      this.animaleService.saveAnimale(payload).subscribe({
        next: (res) => {
          // Mostra un messaggio di successo con p-toast
          console.log("Salvataggio riuscito!", res);
          this.stateService.setClean();
        },
        error: (err) => {
          // Gestisci l'errore (es. il 1366 visto all'inizio!)
          console.error("Errore salvataggio", err);
        }
      });
    }
  }

  annulla(): void {
    const isDirty = this.stateService.getIsDirty();

    if (isDirty) {
      this.confirmationService.confirm({
        message: 'Ci sono modifiche non salvate. Vuoi davvero uscire?',
        header: 'Conferma uscita',
        icon: 'pi pi-exclamation-triangle',
        accept: () => this.router.navigate(['/private/animali'])
      });
    } else {
      // Nessuna modifica? Uscita immediata e silenziosa
      this.router.navigate(['/private/animali']);
    }
  }


}