import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CronologiaService {
  private apiUrl = environment.apiUrl + '/private/cronologia';

  constructor(private http: HttpClient) {}

  getAttivita(idAnimale: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${idAnimale}/attivita`);
  }

  getProprietario(idAnimale: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${idAnimale}/proprietario`);
  }
}