import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CarattereService {
  private apiUrl = environment.apiUrl + '/private/carattere';

  constructor(private http: HttpClient) {}

  getCaratteriAnimale(idAnimale: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${idAnimale}`);
  }

  getTipiCarattere(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tipi`);
  }

  aggiungi(idAnimale: string, carattere: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/${idAnimale}`, carattere);
  }

  aggiornaNota(idCaratteri: string, idCarattere: string, note: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${idCaratteri}/note`, { id_carattere: idCarattere, note });
  }

  elimina(idCaratteri: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${idCaratteri}`);
  }
}