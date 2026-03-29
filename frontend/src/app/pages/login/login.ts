import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// Moduli PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { PublicHeaderComponent } from '../../layout/public-header/public-header';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
    PublicHeaderComponent
  ],
  // NOTA BENE: Qui uso i nomi dei file che hai generato tu
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  // Variabili collegate ai campi di input (ngModel)
  username = '';
  password = '';
  
  loading = false;
  errorMessage: string = '';

  constructor(private router: Router, private authService: AuthService, private cd: ChangeDetectorRef) {}

  login() {
    // 1. Resetta errori precedenti
    this.loading = true;
    this.errorMessage = '';

    // 2. Crea l'oggetto con i dati veri presi dagli input
    const credentials = {
      username: this.username,
      password: this.password
    };

    console.log("Tentativo di login con:", credentials); // Debug: controlla in console cosa invii!

    // 3. Chiamata API reale
    this.authService.login(credentials).subscribe({
      next: (response) => {
        console.log("Login OK:", response);
        this.loading = false;
        this.cd.detectChanges();
        // Login successo -> Vai alla dashboard privata
        this.router.navigate(['/dashboard']); 
      },
      error: (err) => {
        console.error("Errore login:", err);
        this.loading = false;
        
        // Gestione messaggio errore user-friendly
        if (err.status === 401) {
            this.errorMessage = 'Username o password errati.';
        } else {
            this.errorMessage = 'Errore del server. Riprova più tardi.';
        }
        this.cd.detectChanges();
      }
    });
  }
}