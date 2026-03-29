import { Injectable } from '@angular/core'; 

@Injectable({ 
  providedIn: 'root'
})
export class TenantService {
  
  // Il nome del tenant corrente (es. "asso")
  currentTenant: string = 'asso'; 

  constructor() {
    this.detectTenant();
  }

  private detectTenant() {
    const hostname = window.location.hostname; 
    
    if (hostname.includes('localhost') || hostname === '127.0.0.1') {
      this.currentTenant = 'asso'; 
    } else {
      this.currentTenant = hostname.split('.')[0];
    }
    
    console.log('Tenant rilevato:', this.currentTenant);
  }

  getLogoUrl(): string {
    return `assets/${this.currentTenant}/logo.png`;
  }
}