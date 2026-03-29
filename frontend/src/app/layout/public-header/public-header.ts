import { Component, inject } from '@angular/core'; // Usa 'inject' che è moderno
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { TenantService } from '../../services/tenant';
import { MenuItem } from 'primeng/api';


@Component({
  selector: 'app-public-header',
  standalone: true,
  imports: [CommonModule, RouterModule, ToolbarModule, ButtonModule],
  templateUrl: './public-header.html',
  styleUrl: './public-header.css'
})
export class PublicHeaderComponent {
  // Iniettiamo il servizio
  tenantService = inject(TenantService);
  items: MenuItem[] | undefined;
  
  // Creiamo una variabile per l'HTML
  logoUrl = this.tenantService.getLogoUrl();

  ngOnInit() {
    this.items = [
      {
        label: 'Home',
        icon: 'pi pi-home',
        routerLink: '/'
      },
      {
        label: 'Cerca un amico',
        icon: 'pi pi-search',
        routerLink: '/cerca'
      },
      // AGGIUNGI QUESTO BLOCCO QUI:
      {
        label: 'Lieti Fine',
        icon: 'pi pi-heart-fill',
        routerLink: '/lieti-fine',
        styleClass: 'text-pink-500' // Un tocco di colore per farlo risaltare!
      },
      // ... eventuali altre voci (es. Login)
    ];
  }
}