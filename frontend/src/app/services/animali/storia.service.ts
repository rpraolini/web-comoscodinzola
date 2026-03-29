import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StoriaService {
  private apiUrl = environment.apiUrl + '/private/storia';

  constructor(private http: HttpClient) {}

  getEventi(idAnimale: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${idAnimale}`);
  }

  getTipi(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tipi`);
  }

  save(evento: any): Observable<any> {
    return this.http.post(this.apiUrl, evento);
  }

  update(idEvento: string, evento: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${idEvento}`, evento);
  }

  delete(idEvento: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${idEvento}`);
  }
}