import { Routes } from '@angular/router';

import { AuthGuard } from './guards/auth.guard';
import { HomeComponent } from './pages/home/home';
import { PublicSearchComponent } from './pages/public-search/public-search';
import { LoginComponent } from './pages/login/login';
import { PrivateLayoutComponent } from './layout/private-layout/private-layout';
import { PublicDetailComponent } from './pages/public-detail/public-detail';
import { PublicLietiFineComponent } from './pages/public-lieti-fine/public-lieti-fine';
import { DashboardComponent } from './pages/private/dashboard/dashboard';
import { FotoVideo } from './pages/private/animali/components/foto-video/foto-video';
import { Salute } from './pages/private/animali/components/salute/salute';
import { Storia } from './pages/private/animali/components/storia/storia';
import { Cronologia } from './pages/private/animali/components/cronologia/cronologia';
import { Adozione } from './pages/private/animali/components/adozione/adozione';
import { Carattere } from './pages/private/animali/components/carattere/carattere';
import { DettaglioAnimaleComponent } from './pages/private/animali/dettaglio-animale/dettaglio-animale';
import { AnagraficaComponent } from './pages/private/animali/components/anagrafica/anagrafica';

export const routes: Routes = [

  // --- ROTTE PUBBLICHE ---

  // 1. HOME: Aggiungi pathMatch: 'full' per evitare conflitti!
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'lieti-fine', component: PublicLietiFineComponent },
  { path: 'cerca', component: PublicSearchComponent },
  { path: 'dettaglio/:id', component: PublicDetailComponent },
  { path: 'login', component: LoginComponent },
  

  // --- ROTTE PRIVATE (Protette) ---
  {
    path: '', // Questo path vuoto funge da prefisso per i figli
    component: PrivateLayoutComponent, // Il layout con la sidebar e topbar
    canActivate: [AuthGuard], // Protegge TUTTI i figli in un colpo solo
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'animali/dettaglio/:id', component: DettaglioAnimaleComponent,
        children: [
          { path: 'anagrafica', component: AnagraficaComponent },
          { path: 'foto', component: FotoVideo },
          { path: 'salute', component: Salute },
          { path: 'cronologia', component: Cronologia },
          { path: 'storia', component: Storia },
          { path: 'adozione', component: Adozione },
          { path: 'carattere', component: Carattere },
          { path: '', redirectTo: 'anagrafica', pathMatch: 'full' }
        ]
      },
      // { path: 'soci', component: SociComponent }, // Esempi futuri...
      // { path: 'cani', component: CaniComponent },
    ]
  },

  // --- FALLBACK ---
  // Se l'utente scrive un URL a caso, lo mandiamo alla dashboard
  // (La AuthGuard della dashboard controllerà se è loggato, altrimenti lo manda al login)
  { path: '**', redirectTo: 'dashboard' }
];