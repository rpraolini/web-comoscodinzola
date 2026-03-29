import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  
  // URL base (es. /manorg/api/public/login)
  private loginUrl = environment.apiUrl + '/public/login';

  constructor(private http: HttpClient, private router: Router) { }

  login(credentials: any) {
    return this.http.post<any>(this.loginUrl, credentials).pipe(
      tap(response => {
        if (response && response.token) {
          // Salva il token nel LocalStorage del browser
          localStorage.setItem('token', response.token);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/']);
  }

  // Verifica se l'utente è loggato
  isLoggedIn(): boolean {
    return !!localStorage.getItem('token'); // True se il token esiste
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}