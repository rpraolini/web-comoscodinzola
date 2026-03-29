import { Component } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common'; // Per *ngIf
import { FormsModule } from '@angular/forms';   // Per [(ngModel)]

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  errore = false;

  constructor(private http: HttpClient, private router: Router) {}

  effettuaLogin() {
    // 1. Prepariamo i dati come un form classico
    const formData = new FormData();
    formData.append('username', this.username);
    formData.append('password', this.password);

    // 2. Chiamiamo il backend
    this.http.post('/api/login', formData).subscribe({
      next: (response) => {
        console.log('Login riuscito!', response);
        // 3. Reindirizza alla dashboard privata
        this.router.navigate(['/private/dashboard']); 
      },
      error: (err) => {
        console.error('Errore login', err);
        this.errore = true;
      }
    });
  }
}